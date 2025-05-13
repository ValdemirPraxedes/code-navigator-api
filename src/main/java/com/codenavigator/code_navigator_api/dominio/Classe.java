package com.codenavigator.code_navigator_api.dominio;

import java.util.HashMap;

public class Classe {
	
		private String assinatura;
		private String tipo;
		private String url;
		
		private HashMap<String, Metodo> metodos = new HashMap<String, Metodo>();

		public String getAssinatura() {
			return assinatura;
		}

		public void setAssinatura(String assinatura) {
			this.assinatura = assinatura;
		}

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public HashMap<String, Metodo> getMetodos() {
			return metodos;
		}

		public void setMetodos(HashMap<String, Metodo> metodos) {
			this.metodos = metodos;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
}
