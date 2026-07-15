# AI Document Research Agent

An autonomous AI research assistant built with Spring Boot and Spring AI, integrating Claude to research questions across a private document collection, synthesize findings, and produce structured, cited reports.

**Status: Early in progress** — actively under development. See [Current Progress](#current-progress) below for what's implemented today versus what's planned.

---

## Overview

This project explores what it takes to build a genuinely *autonomous* AI agent — one that decides its own tool-use path in pursuit of a goal, rather than following a hard-coded workflow. Given a research question, the agent should be able to:

- Search a private document collection via retrieval-augmented generation (RAG)
- Decide whether it has enough information, or needs to search again with refined queries
- Synthesize an answer, citing which documents it drew from
- Save its findings for later retrieval
- Support natural follow-up questions within a session

The project is also a deliberate exercise in modern backend architecture — Spring Boot, PostgreSQL, vector search, containerization, and CI/CD — applied to a genuinely current problem domain (AI agent design) rather than a toy example.

---

## Architecture

- **Backend:** Java 21, Spring Boot
- **AI Integration:** Spring AI, Anthropic Claude
- **Database:** PostgreSQL (relational data + vector storage via PGVector)
- **Embeddings:** Generated in-house via a lightweight version of the BAAI General Embedding model
- **Containerization:** Docker (planned)

### Data Model

Original uploaded documents are retained in full (not just their embeddings), with derived chunk-level embeddings stored separately in the vector index. This keeps the vector store as a rebuildable artifact rather than the sole source of truth — if the chunking strategy or embedding approach changes, the original documents remain available to re-process. See the design document *(link once written)* for the full reasoning behind this choice.

---

## Embeddings

This project generates embeddings in-house rather than calling an external embedding API. This was a deliberate choice to better understand the mechanics of embedding generation rather than treating it as an opaque external call.
I am not including the model files in this repository.  See [Getting Started](#getting-started) for more details.

---

## Current Progress

** Implemented:**
- [x] Document upload REST controller
- [x] Database schema for storing documents and vector embeddings
- [x] Document chunking pipeline
- [x] In-house embedding generation

** Partially Implemented:**

**Planned:**
- [ ] Vector similarity search / retrieval

- [ ] Autonomous research agent (ReAct loop via Spring AI `ChatClient`, with tool calling)
- [ ] Tool set: document search, document lookup by ID, save report, retrieve saved report
- [ ] Report persistence and retrieval
- [ ] Conversational memory across follow-up questions
- [ ] Guardrails: iteration limits, input validation on write-capable tools
- [ ] Unit and integration tests

- [ ] CI pipeline (GitHub Actions)
- [ ] Dockerization and `docker-compose` setup
- [ ] Design document covering architecture decisions, autonomy/reliability tradeoffs, and guardrail placement

---

## Getting Started

*Setup instructions will be added as the project stabilizes. At minimum you will need:*

- Java 21
- PostgreSQL (with the PGVector extension)
- An Anthropic API key

```bash
git clone https://github.com/bobstrickland/AIDocumentResearchAgent.git
cd AIDocumentResearchAgent
# further setup instructions coming soon
```

To download the model to you machine, here is what I did, you should be able to do something similar:

```
python3 -m venv onnx-export-env
source onnx-export-env/bin/activate

python3 -m pip install --upgrade pip
python3 -m pip install "optimum[onnxruntime]" torch transformers

python3 -m optimum.exporters.onnx --model BAAI/bge-base-en-v1.5 --task feature-extraction --optimize O2 bge-base-onnx/
```

---

## Why This Project

This project doubles as a hands-on way to close the gap between deep enterprise Java experience and current, production-relevant technologies — cloud-native architecture, containerization, vector search, and autonomous AI agent design. It's built to be a genuine, working system rather than a tutorial copy, including the real debugging and architectural decisions that come with that.

---

## License

MIT

## Built With

- [Spring AI](https://spring.io/projects/spring-ai)
- [PGVector](https://github.com/pgvector/pgvector)
- [BGE embeddings](https://huggingface.co/BAAI/bge-base-en-v1.5) (BAAI)
