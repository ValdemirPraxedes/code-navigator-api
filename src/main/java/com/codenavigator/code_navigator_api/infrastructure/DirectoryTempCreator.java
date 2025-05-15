package com.codenavigator.code_navigator_api.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import com.codenavigator.code_navigator_api.exceptions.InfrastructureException;

public final class DirectoryTempCreator implements AutoCloseable {

	private final Path path;

	private DirectoryTempCreator(Path path) {
		this.path = path;
	}

	public static DirectoryTempCreator create(String name) {
		try {
			Path path = Files.createTempDirectory(name);
			return new DirectoryTempCreator(path);
		} catch (IOException e) {
			throw new InfrastructureException("Error creating directory: " + name, e);
		}
	}


	@Override
	public void close() throws InfrastructureException {
		try {
			Files.walk(this.path).sorted(Comparator.reverseOrder()).forEach(p -> {
				try {
					Files.delete(p);
				} catch (IOException e) {
					throw new InfrastructureException("Error deleting: " + e.getMessage(), e);

				}
			});
		} catch (IOException e) {
			throw new InfrastructureException("Error cleaning temporary directory: " + e.getMessage());
		}

	}
	
	public Path getPath() {
		return path;
	}

}
