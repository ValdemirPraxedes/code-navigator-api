package com.codenavigator.code_navigator_api.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JavaCodeAnalyzer {

    public List<CompilationUnit> analyzeFromZip(MultipartFile zipFile) throws IOException {
        // 1. Criar diretório temporário
        Path tempDir = Files.createTempDirectory("code-analysis-temp");

        // 2. Extrair arquivos .java
        unzipJavaFiles(zipFile, tempDir);

        // 3. Configurar SymbolSolver
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(tempDir));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        // 4. Analisar todos os arquivos .java da pasta
        List<CompilationUnit> units = new ArrayList<>();
        Files.walk(tempDir)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(path);
                        units.add(cu);
                    } catch (IOException e) {
                        System.err.println("Erro ao parsear " + path + ": " + e.getMessage());
                    }
                });

        // 5. (Opcional) Apagar os arquivos temporários
        deleteTempDirectory(tempDir);

        return units;
    }

    private void unzipJavaFiles(MultipartFile zipFile, Path destination) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
                    Path filePath = destination.resolve(entry.getName());
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
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
