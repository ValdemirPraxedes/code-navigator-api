package com.codenavigator.code_navigator_api.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.codenavigator.code_navigator_api.utils.JavaCodeAnalyzer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.codenavigator.code_navigator_api.dominio.*;
import com.codenavigator.code_navigator_api.infrastructure.DirectoryTempCreator;
import com.codenavigator.code_navigator_api.infrastructure.JavaSourceRootResolver;
import com.codenavigator.code_navigator_api.infrastructure.ZipExtractionDirectoryCreatorAdapter;

@RestController
@RequestMapping("/analysis")
public class ArtifactAnalysisController {

	private HashMap<String, Classe> classFactory( List<CompilationUnit> units) {
		 HashMap<String, Classe> classes = new HashMap<String, Classe>();
	        
	        
	        units.forEach(cu -> {
	        	Classe classe = new Classe();
	        	
	        	cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
	        	    try {
	        	    	if(clazz == null) return;
	        	        var resolvedClass = clazz.resolve();
	        	        Optional<AnnotationExpr> annotationByName = clazz.getAnnotationByName("RestController");
	        	        
	        	        if(annotationByName.isPresent()) {

	        	        	Optional<AnnotationExpr> annotationByName2 = clazz.getAnnotationByName("RequestMapping");
	        	        	classe.setTipo("Controlador Rest");
	        	        	if(annotationByName2.isPresent()) {
	        	        		
	        	        		Expression memberValue = annotationByName2.get().asSingleMemberAnnotationExpr().getMemberValue();           	        		
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
	            	if(method == null) return;
	            	Metodo metodo = new Metodo();
	            	try {
	            		Optional<AnnotationExpr> annotationByName = method.getAnnotationByName("GetMapping");
	                    
	            		
	                    if(annotationByName.isPresent()) {
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

	
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String>  getArtifactList(@RequestParam("artifact") MultipartFile artifact) throws IOException {
		
		
		//validação
        if (artifactIsNotZipOrIsEmpty(artifact)) {
            return ResponseEntity.badRequest().body("The file must be a non-empty .zip containing .java files.");
        }
        
        //instanciação de classe
        HashMap<String, Classe> classes = null;
        
        //criação de directorio temporario
        try(ZipExtractionDirectoryCreatorAdapter directoryTempCreator = ZipExtractionDirectoryCreatorAdapter.from(artifact.getInputStream(),"code-analysis-temp")) {
        
        
        //instanciação de classe
        JavaCodeAnalyzer analyzer = new JavaCodeAnalyzer();
        
        // analise de arquivo zip com a pasta temporaria como dependencia
        List<CompilationUnit> units = analyzer.analyzeFromZip(artifact, JavaSourceRootResolver.resolveSourceRoot(directoryTempCreator.getPath()));
        
        //transformação de dados List<CompilationUnit> em HashMap<String, Classe>
         classes = classFactory(units);
        
        
        
        }
        //instaciacao de classe 
        ObjectMapper objectMapper = new ObjectMapper();
        
        //Conversão do hashMap em um json String
        String json = objectMapper.writeValueAsString(classes);
        
        
        //Retorno do endpoint com status 200 
        return ResponseEntity.ok(json);
	}
	
	
	@PostMapping(path ="/classes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String>  getArtifactClassList(@RequestParam("artifact") MultipartFile artifact) throws IOException {
        if (artifactIsNotZipOrIsEmpty(artifact)) {
            return ResponseEntity.badRequest().body("The file must be a non-empty .zip containing .java files.");
        }
        
        Path tempDir = Files.createTempDirectory("code-analysis-temp");
        
        JavaCodeAnalyzer analyzer = new JavaCodeAnalyzer();
        
        
        
        List<CompilationUnit> units = analyzer.analyzeFromZip(artifact, tempDir);
        
        HashMap<String, Classe> classes = classFactory(units);
        
        deleteTempDirectory(tempDir);
        
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
        		if(setMetodo == null) return;
        		String assinatura = setMetodo.getValue().getAssinatura();
        		if(assinatura == null) return;
        		
        		ObjectNode nodeMetodo = mapper.createObjectNode();
        		
        		nodeMetodo.put("assinatura",  assinatura);
        		nodeMetodo.put("corpo",  setMetodo.getValue().getCorpo());
        		ArrayNode arrayNodeChamadasFeitas = mapper.createArrayNode();
        		for(Chamada chamada: setMetodo.getValue().getChamadas()) {
        			    			
        			ArrayList<Metodo> listaChamadas = new ArrayList<Metodo>();
        			
        			
        			
        			resolveMethodSignature(classes, chamada,listaChamadas);
        			
        			
        			for(Metodo ch: listaChamadas) {
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

        return ResponseEntity.ok(json.toPrettyString());
		
	}
	
	private List<String> getClassesFromZip(MultipartFile artifact) throws IOException {
	       List<String> javaFiles = new ArrayList<>();
	       Path tempDir = Files.createTempDirectory("code-analyzer");
	        try (ZipInputStream zis = new ZipInputStream(artifact.getInputStream())) {
	            ZipEntry entry;

	            while ((entry = getFileOrFolderFromZip(zis)) != null) {
	                if (!entry.isDirectory() && isFileJava(entry)) {
	                	createFileTemp(tempDir, entry, zis);
	                    javaFiles.add(entry.getName());
	                }
	                zis.closeEntry();
	            }
	        }
	        
	        return javaFiles;
	}
	
	private void createFileTemp(Path tempDir, ZipEntry entry, ZipInputStream zis) throws IOException {
		Path filePath = tempDir.resolve(entry.getName());
        Files.createDirectories(filePath.getParent());
        Files.copy(zis, filePath);
	}
	
	private ZipEntry getFileOrFolderFromZip(ZipInputStream zis) throws IOException {
		return zis.getNextEntry();
	}
	
	private boolean isFileJava(ZipEntry entry) {
		return entry.getName().endsWith(".java");
	}
	
	private boolean artifactIsNotZipOrIsEmpty(MultipartFile artifact) {
		return artifact.isEmpty() || !artifact.getOriginalFilename().endsWith(".zip");
	}
	
	private void resolveMethodSignature(HashMap<String, Classe> classes, Chamada chamada, List<Metodo> listaChamadas) {
	    String nomeMetodo = chamada.getChamada();
	    String assinatura = chamada.getAssinatura();

	    String nomeClasse = assinatura.substring(0, assinatura.indexOf(nomeMetodo) - 1);

	    Classe classe = classes.get(nomeClasse);
	    if (classe == null) return;

	    Metodo metodo = classe.getMetodos().get(assinatura);
	    if (metodo == null) return;

	    listaChamadas.add(metodo);

	    for (Chamada chamadaInterna : metodo.getChamadas()) {
	        resolveMethodSignature(classes, chamadaInterna, listaChamadas);
	    }
	}
	
    private void deleteTempDirectory(Path directory) {
        try {
            Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        System.err.println("Erro ao deletar: " + p + " - " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            System.err.println("Erro ao limpar diretório temporário: " + e.getMessage());
        }
    }

}
