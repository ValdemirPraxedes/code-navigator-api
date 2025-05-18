package com.codenavigator.code_navigator_api.core.analyzer;

import java.util.List;
import java.util.Optional;

import com.codenavigator.code_navigator_api.dominio.Classe;
import com.codenavigator.code_navigator_api.dominio.Metodo;
import com.codenavigator.code_navigator_api.dominio.SpringMethodAnnotationsEnum;
import com.codenavigator.code_navigator_api.dominio.SpringRestAnnotationsEnum;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;

public class SpringClassAnalyzer extends AbstractClassAnalyzer {

	public SpringClassAnalyzer(List<CompilationUnit> units) {
		super(units);
	}

	@Override
	protected void handleClass(ClassOrInterfaceDeclaration clazz, Classe classe) {
		var resolvedClass = clazz.resolve();
		classe.setAssinatura(resolvedClass.getQualifiedName());

		if (clazz.getAnnotationByName(SpringRestAnnotationsEnum.REST_CONTROLLER.annotation).isPresent()) {
			classe.setTipo(SpringRestAnnotationsEnum.REST_CONTROLLER.value);

			clazz.getAnnotationByName(SpringRestAnnotationsEnum.REQUEST_MAPPING.annotation).ifPresent(annotation -> {
				try {
										
					if (annotation.isSingleMemberAnnotationExpr()) {
				        Expression value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
				        classe.setUrl(value.toString().replace("\"", ""));
				    }
					
					else  if(annotation.isNormalAnnotationExpr()) {
						  annotation.asNormalAnnotationExpr().getPairs().stream()
		                    .filter(pair -> pair.getNameAsString().equals("value"))
		                    .findFirst()
		                    .ifPresent(pair -> {
		                        String value = pair.getValue().toString().replace("\"", "");
		                        classe.setUrl(value.toString());
		                    });
						  
					}
				} catch (Exception e) {
					// não é singleMember
				}
			});
		} else {
			classe.setTipo("Normal");
		}
	}

	@Override
	protected void handleMethod(MethodDeclaration method, Metodo metodo) {

		for (SpringMethodAnnotationsEnum springMethodAnnotationsEnum : SpringMethodAnnotationsEnum.values()) {
			Optional<AnnotationExpr> annotationOfMethod = method
					.getAnnotationByName(springMethodAnnotationsEnum.annotation);
			if (annotationOfMethod.isPresent()) {
				AnnotationExpr annotation = annotationOfMethod.get();
				
				metodo.setMetodo(springMethodAnnotationsEnum.value);
				metodo.setTipo("ENDPOINT");
				
				
				
				if (annotation.isSingleMemberAnnotationExpr()) {
			        Expression value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
			        metodo.setUrl(value.toString());
			    }
				
				else  if(annotation.isNormalAnnotationExpr()) {
					  annotation.asNormalAnnotationExpr().getPairs().stream()
	                    .filter(pair -> pair.getNameAsString().equals("value"))
	                    .findFirst()
	                    .ifPresent(pair -> {
	                        String value = pair.getValue().toString().replace("\"", "");
	                        metodo.setUrl(value.toString());
	                    });
					  
				}
				
				

			}

		}
		metodo.setAssinatura(method.resolve().getQualifiedSignature());

		method.getBody().ifPresent(body -> {
			metodo.setCorpo(body.toString());
		});
	}
}