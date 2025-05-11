# Code Navigator API

Uma API para análise e organização de código-fonte por endpoints, facilitando a leitura e visualização de estruturas, métodos e dependências do projeto.

## ✨ Objetivo

Fornecer uma interface REST que permita:
- Analisar projetos Java e gerar um mapeamento por endpoint.

## 🚀 Funcionalidades previstas

- Upload de projeto.
- Análise de código com JavaParser e JavaSymbolSolver.
- Endpoints REST para:
  - Buscar anotações (ex: `@RestController`, `@RequestMapping`)
- Estrutura navegável via JSON

## 🛠️ Tecnologias

- Java 21
- Spring Boot 3.4.5
- JavaParser
- JavaSymbolSolver
- Maven

## 🔧 Requisitos

- Java 21+
- Maven 3.9+

## 📦 Como rodar

```bash
git clone https://github.com/seu-usuario/code-navigator-api.git
cd code-navigator-api
./mvnw spring-boot:run
