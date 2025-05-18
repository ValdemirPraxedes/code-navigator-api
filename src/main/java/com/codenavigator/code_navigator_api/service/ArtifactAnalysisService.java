package com.codenavigator.code_navigator_api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import com.codenavigator.code_navigator_api.core.analyzer.AbstractClassAnalyzer;
import com.codenavigator.code_navigator_api.core.analyzer.SpringClassAnalyzer;
import com.codenavigator.code_navigator_api.dominio.Classe;
import com.codenavigator.code_navigator_api.dominio.SpringRestAnnotationsEnum;
import com.codenavigator.code_navigator_api.factory.JavaParserFactory;
import com.codenavigator.code_navigator_api.infrastructure.ZipExtractionDirectoryCreatorAdapter;
import com.codenavigator.code_navigator_api.output.ClasseJsonExporter;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;

public class ArtifactAnalysisService {

	public HashMap<String, Classe> analyzeFromZip(MultipartFile zipFile, String name) throws IOException {

		try (ZipExtractionDirectoryCreatorAdapter directoryTempCreator = ZipExtractionDirectoryCreatorAdapter
				.from(zipFile.getInputStream(), name)) {
			return createHashMap(directoryTempCreator);
		}
		
	}
	
	public JsonNode analizeFromZipComplete(MultipartFile zipFile, String name) throws IOException{
		return analizeFromZipComplete(zipFile, name, null);
	}
	
	public JsonNode analizeFromZipComplete(MultipartFile zipFile, String name, String filter) throws IOException {

		try (ZipExtractionDirectoryCreatorAdapter directoryTempCreator = ZipExtractionDirectoryCreatorAdapter
				.from(zipFile.getInputStream(), name)) {
			
			 HashMap<String, Classe> classes = createHashMap(directoryTempCreator);
			 			 
				
			return new ClasseJsonExporter().exportAsJson(classes, filter);
		}
		
	}
	
	
	private HashMap<String, Classe> createHashMap(ZipExtractionDirectoryCreatorAdapter directoryTempCreator) {
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

		AbstractClassAnalyzer analyzer = new SpringClassAnalyzer(units);
		return analyzer.analyze();
	}


}
