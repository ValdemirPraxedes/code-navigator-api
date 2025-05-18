package com.codenavigator.code_navigator_api.controller;

import java.io.IOException;
import java.util.HashMap;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.codenavigator.code_navigator_api.dominio.Classe;
import com.codenavigator.code_navigator_api.dominio.SpringRestAnnotationsEnum;
import com.codenavigator.code_navigator_api.service.ArtifactAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/analysis")
public class ArtifactAnalysisController {


	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> getArtifactList(@RequestParam("artifact") MultipartFile artifact) throws IOException {


		if (isInvalidZip(artifact)) {
			return ResponseEntity.badRequest().body("The file must be a non-empty .zip containing .java files.");
		}


		ArtifactAnalysisService analyzer = new ArtifactAnalysisService();


		HashMap<String, Classe> classes = analyzer.analyzeFromZip(artifact, "code-analysis-temp");

		if (classes.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		var json = new ObjectMapper().writeValueAsString(classes);

		return ResponseEntity.ok(json);
	}

	@PostMapping(path = "/classes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> getArtifactClassList(@RequestParam("artifact") MultipartFile artifact)
			throws IOException {
		if (isInvalidZip(artifact)) {
			return ResponseEntity.badRequest().body("The file must be a non-empty .zip containing .java files.");
		}

		ArtifactAnalysisService analyzer = new ArtifactAnalysisService();


		JsonNode json = analyzer.analizeFromZipComplete(artifact, "code-analysis-temp");

		return ResponseEntity.ok(json.toPrettyString());

	}
	
	@PostMapping(path = "/controller", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> getArtifactController(@RequestParam("artifact") MultipartFile artifact)
			throws IOException {
		if (isInvalidZip(artifact)) {
			return ResponseEntity.badRequest().body("The file must be a non-empty .zip containing .java files.");
		}

		ArtifactAnalysisService analyzer = new ArtifactAnalysisService();


		JsonNode json = analyzer.analizeFromZipComplete(artifact, "code-analysis-temp", SpringRestAnnotationsEnum.REST_CONTROLLER.value);

		return ResponseEntity.ok(json.toPrettyString());

	}




	private boolean isInvalidZip(MultipartFile artifact) {
		return artifact.isEmpty() || !artifact.getOriginalFilename().endsWith(".zip");
	}

}
