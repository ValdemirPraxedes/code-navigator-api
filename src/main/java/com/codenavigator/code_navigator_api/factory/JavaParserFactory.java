package com.codenavigator.code_navigator_api.factory;

import java.nio.file.Path;

import com.codenavigator.code_navigator_api.infrastructure.JavaSourceRootResolver;
import com.github.javaparser.JavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class JavaParserFactory {

	private JavaParserFactory() {
		
	}
	
	public static JavaParser create(Path directorySource) {
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		typeSolver.add(new ReflectionTypeSolver());
		typeSolver.add(new JavaParserTypeSolver(JavaSourceRootResolver.resolveSourceRoot(directorySource)));

		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		

		JavaParser javaParser = new JavaParser();
		
		javaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
		
		return 	javaParser;
	}
}
