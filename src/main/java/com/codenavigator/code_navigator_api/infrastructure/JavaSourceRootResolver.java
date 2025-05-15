package com.codenavigator.code_navigator_api.infrastructure;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.stream.Stream;

import com.codenavigator.code_navigator_api.exceptions.InfrastructureException;

public class JavaSourceRootResolver {

    public static Path resolveSourceRoot(Path baseDirectory) {
        try (Stream<Path> files = Files.walk(baseDirectory)) {
            return files
                .filter(p -> p.toString().endsWith(".java"))
                .flatMap(p -> findRootPathForJavaFile(p).stream())
                .findFirst().orElseThrow(() -> new InfrastructureException("path to source code folder not found"));
        } catch (IOException e) {
            throw new InfrastructureException("Error resolving source code path", e);
        }
    }
    
    private static Optional<Path> findRootPathForJavaFile(Path javaFilePath) {
        try {
            var lines = Files.readAllLines(javaFilePath);
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("package ")) {
                    String packageName = extractPackageName(line);
                    int depth = packageName.split("\\.").length;

                    Path current = javaFilePath.getParent(); 
                    for (int i = 0; i < depth; i++) {
                        if (current != null) {
                            current = current.getParent();
                        } else {
                            return Optional.empty(); 
                        }
                    }

                    return Optional.ofNullable(current);
                }
            }
        } catch (IOException ignored) {}
        return Optional.empty();
    }


    private static String extractPackageName(String line) {
        return line.replace("package", "").replace(";", "").trim();
    }
}
