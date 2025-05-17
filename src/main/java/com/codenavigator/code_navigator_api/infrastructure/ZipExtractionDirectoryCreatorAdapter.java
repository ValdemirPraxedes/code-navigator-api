package com.codenavigator.code_navigator_api.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.codenavigator.code_navigator_api.exceptions.InfrastructureException;

public class ZipExtractionDirectoryCreatorAdapter implements AutoCloseable {

    private final DirectoryTempCreator directoryTempCreator;

    private ZipExtractionDirectoryCreatorAdapter(DirectoryTempCreator directoryTempCreator) {
        this.directoryTempCreator = directoryTempCreator;
    }

    public static ZipExtractionDirectoryCreatorAdapter from(InputStream zipStream, String tempDirPrefix) {
        DirectoryTempCreator tempCreator = DirectoryTempCreator.create(tempDirPrefix);
        try (ZipInputStream zis = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = tempCreator.getPath().resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
            return new ZipExtractionDirectoryCreatorAdapter(tempCreator);
        } catch (IOException e) {
            tempCreator.close(); // garante cleanup se der erro
            throw new InfrastructureException("Erro ao extrair zip", e);
        }
    }
    
	public void walkFile(Consumer<Path> file) {
	    directoryTempCreator.walkFile(path -> {
	        if (Files.isRegularFile(path) && path.toString().endsWith(".java")) {
	            file.accept(path);
	        }
	    });
	}

    public Path getPath() {
        return directoryTempCreator.getPath();
    }

    @Override
    public void close() {
        directoryTempCreator.close();
    }
}
