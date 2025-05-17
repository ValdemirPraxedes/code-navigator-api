package com.codenavigator.code_navigator_api.core.analyzer;

import java.util.List;

import com.codenavigator.code_navigator_api.dominio.Classe;
import com.codenavigator.code_navigator_api.dominio.Metodo;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;

public class SpringClassAnalyzer extends AbstractClassAnalyzer {

    public SpringClassAnalyzer(List<CompilationUnit> units) {
		super(units);
	}

	@Override
    protected void handleClass(ClassOrInterfaceDeclaration clazz, Classe classe) {
        var resolvedClass = clazz.resolve();
        classe.setAssinatura(resolvedClass.getQualifiedName());

        if (clazz.getAnnotationByName("RestController").isPresent()) {
            classe.setTipo("Controlador Rest");

            clazz.getAnnotationByName("RequestMapping").ifPresent(annotation -> {
                try {
                    Expression value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
                    classe.setUrl(value.toString());
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
        if (method.getAnnotationByName("GetMapping").isPresent()) {
            metodo.setUrl("GET /");
        }

        metodo.setAssinatura(method.resolve().getQualifiedSignature());

        method.getBody().ifPresent(body -> {
            metodo.setCorpo(body.toString());
        });
    }
}