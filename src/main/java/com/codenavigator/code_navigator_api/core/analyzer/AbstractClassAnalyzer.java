package com.codenavigator.code_navigator_api.core.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.codenavigator.code_navigator_api.dominio.Chamada;
import com.codenavigator.code_navigator_api.dominio.Classe;
import com.codenavigator.code_navigator_api.dominio.Metodo;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

public abstract class AbstractClassAnalyzer {

	private final List<CompilationUnit> units;
	
	
	public AbstractClassAnalyzer(List<CompilationUnit> units) {
		this.units = units;
	}
	
	protected abstract void handleClass(ClassOrInterfaceDeclaration clazz, Classe classe);
	protected abstract void handleMethod(MethodDeclaration method, Metodo metodo);
	
    public HashMap<String, Classe> analyze() {
        HashMap<String, Classe> classes = new HashMap<>();

        for (CompilationUnit cu : units) {
            Classe classe = new Classe();

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                try {
                    if (clazz == null) return;

                    handleClass(clazz, classe);

                } catch (Exception e) {
                    System.out.println("Classe não resolvida: " + clazz.getNameAsString());
                }
            });

            cu.findAll(MethodDeclaration.class).forEach(method -> {
                if (method == null) return;
                Metodo metodo = new Metodo();

                try {
                    handleMethod(method, metodo);

                    List<Chamada> chamadasReais = extractCalls(method);
                    metodo.setChamadas(new ArrayList<>(chamadasReais));

                    if (metodo.getAssinatura() != null) {
                        classe.getMetodos().put(metodo.getAssinatura(), metodo);
                    }
                } catch (Exception e) {
                }
            });

            classes.put(classe.getAssinatura(), classe);
        }

        return classes;
    }
    
    private List<Chamada> extractCalls(MethodDeclaration method) {
        List<Chamada> chamadasReais = new ArrayList<>();
        List<MethodCallExpr> chamadas = method.findAll(MethodCallExpr.class);

        for (MethodCallExpr chamada : chamadas) {
            Chamada c = new Chamada();
            c.setChamada(chamada.getNameAsString());
            chamada.getScope().ifPresent(scope -> c.setEscopo(scope.toString()));
            try {
                c.setAssinatura(chamada.resolve().getQualifiedSignature());
                chamadasReais.add(c);
            } catch (Exception e) {
                // chamada não resolvida
            }
        }

        return chamadasReais;
    }
}
