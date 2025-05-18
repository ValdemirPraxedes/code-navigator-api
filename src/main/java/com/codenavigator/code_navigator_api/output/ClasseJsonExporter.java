package com.codenavigator.code_navigator_api.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.codenavigator.code_navigator_api.dominio.Chamada;
import com.codenavigator.code_navigator_api.dominio.Classe;
import com.codenavigator.code_navigator_api.dominio.Metodo;
import com.codenavigator.code_navigator_api.dominio.SpringMethodAnnotationsEnum;
import com.codenavigator.code_navigator_api.dominio.SpringRestAnnotationsEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ClasseJsonExporter {

	private final ObjectMapper mapper = new ObjectMapper();

	public JsonNode exportAsJson(HashMap<String, Classe> classes) {
		return exportAsJson(classes, null);
	}
	
	public JsonNode exportAsJson(HashMap<String, Classe> classes, String filter) {
		ObjectNode root = mapper.createObjectNode();
		ArrayNode arrayClasses = mapper.createArrayNode();

		HashMap<String, Classe> classesFiltrada = (filter != null) ?  classes.entrySet().stream().filter(classe -> classe.getValue().getTipo().equals(filter))
																			.collect(Collectors.toMap(
																					Map.Entry::getKey, Map.Entry::getValue,
																					(a, b) -> b,
																					HashMap::new)): classes;
		
		for (Entry<String, Classe> entry : classesFiltrada.entrySet()) {
			ObjectNode nodeClasse = mapper.createObjectNode();
			nodeClasse.put("classe", entry.getKey());
			nodeClasse.put("tipo", entry.getValue().getTipo());

			if (entry.getValue().getTipo().equals(SpringRestAnnotationsEnum.REST_CONTROLLER.value)) {
				nodeClasse.put("url", entry.getValue().getUrl());
			}

			ArrayNode arrayMetodos = mapper.createArrayNode();
			for (Entry<String, Metodo> entryMetodo : entry.getValue().getMetodos().entrySet()) {
				if (entryMetodo == null || entryMetodo.getValue().getAssinatura() == null)
					continue;

				ObjectNode nodeMetodo = mapper.createObjectNode();
				nodeMetodo.put("assinatura", entryMetodo.getValue().getAssinatura());
				nodeMetodo.put("corpo", entryMetodo.getValue().getCorpo());
				if (entryMetodo.getValue().getTipo() != null && entryMetodo.getValue().getTipo().equals("ENDPOINT")) {
					nodeMetodo.put("tipo", entryMetodo.getValue().getTipo());
					nodeMetodo.put("metodo", entryMetodo.getValue().getMetodo());
					String url = entryMetodo.getValue().getUrl() != null ? entry.getValue().getUrl()+entryMetodo.getValue().getUrl(): entry.getValue().getUrl(); 
					nodeMetodo.put("url", url); 
				}

				ArrayNode arrayChamadas = mapper.createArrayNode();
				for (Chamada chamada : entryMetodo.getValue().getChamadas()) {
					List<Metodo> listaChamadas = new ArrayList<>();
					resolveMethodSignature(classes, chamada, listaChamadas);

					for (Metodo chamadaMetodo : listaChamadas) {
						ObjectNode chamadaNode = mapper.createObjectNode();
						chamadaNode.put("assinatura", chamadaMetodo.getAssinatura());
						chamadaNode.put("corpo", chamadaMetodo.getCorpo());
						arrayChamadas.add(chamadaNode);
					}
				}

				nodeMetodo.set("chamadas", arrayChamadas);
				arrayMetodos.add(nodeMetodo);
			}

			nodeClasse.set("metodos", arrayMetodos);
			arrayClasses.add(nodeClasse);
		}

		root.set("classes", arrayClasses);
		return root;
	}

	private void resolveMethodSignature(HashMap<String, Classe> classes, Chamada chamada, List<Metodo> listaChamadas) {
		String nomeMetodo = chamada.getChamada();
		String assinatura = chamada.getAssinatura();

		String nomeClasse = assinatura.substring(0, assinatura.indexOf(nomeMetodo) - 1);

		Classe classe = classes.get(nomeClasse);
		if (classe == null)
			return;

		Metodo metodo = classe.getMetodos().get(assinatura);
		if (metodo == null)
			return;

		listaChamadas.add(metodo);

		for (Chamada chamadaInterna : metodo.getChamadas()) {
			resolveMethodSignature(classes, chamadaInterna, listaChamadas);
		}
	}

}
