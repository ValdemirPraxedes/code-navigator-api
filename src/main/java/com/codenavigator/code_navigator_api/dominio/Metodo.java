package com.codenavigator.code_navigator_api.dominio;

import java.util.ArrayList;
import java.util.List;

public class Metodo {
	
		private String assinatura;
		private String corpo;
		private String url;
		private String tipo;
		private String metodo;
		
		private List<Chamada> chamadas = new ArrayList<Chamada>();

		public String getAssinatura() {
			return assinatura;
		}

		public void setAssinatura(String assinatura) {
			this.assinatura = assinatura;
		}

		public String getCorpo() {
			return corpo;
		}

		public void setCorpo(String corpo) {
			this.corpo = corpo;
		}

		public List<Chamada> getChamadas() {
			return chamadas;
		}

		public void setChamadas(List<Chamada> chamadas) {
			this.chamadas = chamadas;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public String getMetodo() {
			return metodo;
		}

		public void setMetodo(String metodo) {
			this.metodo = metodo;
		}
}
