package com.codenavigator.code_navigator_api.service;

import com.codenavigator.code_navigator_api.dominio.Chamada;
import com.codenavigator.code_navigator_api.dominio.Classe;
import com.codenavigator.code_navigator_api.dominio.Metodo;
import com.codenavigator.code_navigator_api.factory.JavaParserFactory;
import com.codenavigator.code_navigator_api.infrastructure.ZipExtractionDirectoryCreatorAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import ch.qos.logback.core.joran.conditional.IfAction;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArtifactAnalysisService {

	public HashMap<String, Classe> analyzeFromZip(MultipartFile zipFile, String name) throws IOException {

		try (ZipExtractionDirectoryCreatorAdapter directoryTempCreator = ZipExtractionDirectoryCreatorAdapter
				.from(zipFile.getInputStream(), name)) {
			return logic(directoryTempCreator);
		}
		
	}
	
	public JsonNode analizeFromZipComplete(MultipartFile zipFile, String name) throws IOException {

		try (ZipExtractionDirectoryCreatorAdapter directoryTempCreator = ZipExtractionDirectoryCreatorAdapter
				.from(zipFile.getInputStream(), name)) {
			
			 HashMap<String, Classe> classes = logic(directoryTempCreator);
			 
				ObjectMapper mapper = new ObjectMapper();

				ObjectNode node = mapper.createObjectNode();
				ArrayNode arrayNode = mapper.createArrayNode();

				Set<Entry<String, Classe>> entrySet = classes.entrySet();
				entrySet.forEach(entry -> {
					ObjectNode nodeClasse = mapper.createObjectNode();

					nodeClasse.put("classe", entry.getKey());

					Set<Entry<String, Metodo>> entrySetMetodos = entry.getValue().getMetodos().entrySet();
					ArrayNode arrayNodeMetodos = mapper.createArrayNode();
					entrySetMetodos.forEach(setMetodo -> {
						if (setMetodo == null)
							return;
						String assinatura = setMetodo.getValue().getAssinatura();
						if (assinatura == null)
							return;

						ObjectNode nodeMetodo = mapper.createObjectNode();

						nodeMetodo.put("assinatura", assinatura);
						nodeMetodo.put("corpo", setMetodo.getValue().getCorpo());
						ArrayNode arrayNodeChamadasFeitas = mapper.createArrayNode();
						for (Chamada chamada : setMetodo.getValue().getChamadas()) {

							ArrayList<Metodo> listaChamadas = new ArrayList<Metodo>();

							resolveMethodSignature(classes, chamada, listaChamadas);

							for (Metodo ch : listaChamadas) {
								ObjectNode nodeMetodoChamadaFeitas = mapper.createObjectNode();
								nodeMetodoChamadaFeitas.put("assinatura", ch.getAssinatura());
								nodeMetodoChamadaFeitas.put("corpo", ch.getCorpo());
								arrayNodeChamadasFeitas.add(nodeMetodoChamadaFeitas);
							}

						}

						nodeMetodo.put("chamadas", arrayNodeChamadasFeitas);
						arrayNodeMetodos.add(nodeMetodo);
						nodeClasse.put("metodos", arrayNodeMetodos);

					});

					arrayNode.add(nodeClasse);
				});
				node.put("classes", arrayNode);
				JsonNode json = (JsonNode) node;
				
				return json;
		}
		
	}
	
	
	private HashMap<String, Classe> logic(ZipExtractionDirectoryCreatorAdapter directoryTempCreator) {
		JavaParser javaParser = JavaParserFactory.create(directoryTempCreator.getPath());
		
		List<CompilationUnit> units = new ArrayList<>();

		directoryTempCreator.walkFile((path) -> {

			try {
				ParseResult<CompilationUnit> parseResult = javaParser.parse(path);
				
				Optional<CompilationUnit> result = parseResult.getResult();

				if (result.isPresent()) {
					CompilationUnit cu = result.get();
					units.add(cu);
				}
				
			} catch (IOException e) {
				
			}


		});

		return classFactory(units);
	}

	
	private HashMap<String, Classe> classFactory(List<CompilationUnit> units) {
		HashMap<String, Classe> classes = new HashMap<String, Classe>();

		units.forEach(cu -> {
			Classe classe = new Classe();

			cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
				try {
					if (clazz == null)
						return;
					var resolvedClass = clazz.resolve();
					Optional<AnnotationExpr> annotationByName = clazz.getAnnotationByName("RestController");

					if (annotationByName.isPresent()) {

						Optional<AnnotationExpr> annotationByName2 = clazz.getAnnotationByName("RequestMapping");
						classe.setTipo("Controlador Rest");
						if (annotationByName2.isPresent()) {

							Expression memberValue = annotationByName2.get().asSingleMemberAnnotationExpr()
									.getMemberValue();
							classe.setUrl(memberValue.toString());

						}
					} else {
						classe.setTipo("Normal");
					}
					classe.setAssinatura(resolvedClass.getQualifiedName());
				} catch (Exception e) {
					System.out.println("Classe não resolvida: " + clazz.getNameAsString());
				}
			});

			cu.findAll(MethodDeclaration.class).forEach(method -> {
				if (method == null)
					return;
				Metodo metodo = new Metodo();
				try {
					Optional<AnnotationExpr> annotationByName = method.getAnnotationByName("GetMapping");

					if (annotationByName.isPresent()) {
						annotationByName.get().getNameAsString();
						metodo.setUrl("GET /");
					}
					String methodOrigin = method.resolve().getQualifiedSignature();
					metodo.setAssinatura(methodOrigin);
					method.getBody().ifPresentOrElse(body -> {

						metodo.setCorpo(body.toString());

					}, () -> {

					});

				} catch (Exception e) {

				}
				ArrayList<Chamada> chamadasReais = new ArrayList<Chamada>();
				List<MethodCallExpr> chamadas = method.findAll(MethodCallExpr.class);

				for (MethodCallExpr chamada : chamadas) {
					Chamada c = new Chamada();
					c.setChamada(chamada.getNameAsString());
					chamada.getScope().ifPresent(scope -> {
						c.setEscopo(scope.toString());
					});

					try {
						var resolved = chamada.resolve();
						c.setAssinatura(resolved.getQualifiedSignature());
						chamadasReais.add(c);
					} catch (Exception e) {

					}
				}
				metodo.setChamadas(chamadasReais);
				if (metodo.getAssinatura() != null) {
					classe.getMetodos().put(metodo.getAssinatura(), metodo);
				}
			});

			classes.put(classe.getAssinatura(), classe);
		});

		return classes;
	}
	
	private void resolveMethodSignature(HashMap<String, Classe> classes, Chamada chamada, List<Metodo> listaChamadas) {
		String nomeMetodo = chamada.getChamada();
		String assinatura = chamada.getAssinatura();

		String nomeClasse = assinatura.substring(0, assinatura.indexOf(nomeMetodo) - 1);

		Classe classe = classes.get(nomeClasse);
		if (classe == null)
			return;

		Metodo metodo = classe.getMetodos().get(assinatura);
		if (metodo == null)
			return;

		listaChamadas.add(metodo);

		for (Chamada chamadaInterna : metodo.getChamadas()) {
			resolveMethodSignature(classes, chamadaInterna, listaChamadas);
		}
	}

}
