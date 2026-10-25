# AVC 2026/2 — Técnicas de Programação — Tema 2: Sistema de Inscrição em Oficinas

Engenharia de Software 6A · Professora: Ingrid Batista · Entrega: 03/11/2026

API REST em Java (Spring Boot + JDBC puro + PostgreSQL) que controla inscrições em oficinas com vagas
limitadas: registrar inscrição consome uma vaga, cancelar devolve a vaga, sempre de forma transacional.

## 1. Integrantes e responsabilidades

> **[PREENCHER]** o sobrenome completo (se faltar) e o RA de cada integrante. A coluna "Parte" já está pronta.

| Nº | Nome completo | RA | Parte (arquivos principais) |
|---|---|---|---|
| 1 | Carlos Henrique | [PREENCHER] | Configuração do projeto, conexão JDBC, credenciais externas, exceções |
| 2 | Jeferson Santos | [PREENCHER] | Domínio (`Participante`, `Oficina`, `Inscricao`) e diagrama UML |
| 3 | Lucas Vianna | [PREENCHER] | Participante (repository, service, controller) |
| 4 | Luigi Lima | [PREENCHER] | Oficina — repository e service (CRUD e regras sobre histórico) |
| 5 | Matheus Madeira | [PREENCHER] | Oficina — controller, DTOs e tratamento global de exceções (HTTP) |
| 6 | Mileny | [PREENCHER] | Inscrição — repository e documento de transação/concorrência |
| 7 | Pedro Guimarães | [PREENCHER] | Registrar inscrição (service e rotas `POST`/`GET /inscricoes`) |
| 8 | Pedro Mendonça | [PREENCHER] | Cancelar inscrição (service e rota `PATCH`), testes e refatoração |
| 9 | Ulisses | [PREENCHER] | Módulo de algoritmos, cenário próprio do Postman, README |

O histórico de commits e os Pull Requests de cada integrante estão no repositório Git do grupo.

## 2. Como executar

Instruções completas em [`docs/COMO_EXECUTAR.md`](docs/COMO_EXECUTAR.md). Resumo:

```
# 1. carregar o banco (oficial da professora) e configurar config/local.properties
#    (copiar de config/local.properties.example; o arquivo real não é versionado)
# 2. subir a API
mvn spring-boot:run
# 3. rodar os testes JUnit
mvn test
```

Requisitos: JDK 17+, Maven 3.9+, PostgreSQL. A API sobe em `http://localhost:8080`.

## 3. Arquitetura

`Postman/HTTP → Controller → Service → Repository (JDBC) → PostgreSQL`

| Camada | Pacote | Responsabilidade |
|---|---|---|
| Controller | `controller` | recebe HTTP, valida formato, chama o Service, devolve status HTTP; sem SQL e sem regra |
| Service | `service` | regras de negócio e coordenação do caso de uso; controla a transação |
| Repository | `repository` | SQL com `PreparedStatement`; sem regra de negócio |
| Domínio / DTOs | `dominio`, `dto` | entidades com invariantes protegidas; dados de entrada/saída |
| Algoritmos | `algoritmos` | módulo independente das rotas (busca e ordenação em memória) |
| Infra | `config`, `excecao` | fábrica de conexões, exceções do sistema |

Diagramas UML (classes e camadas): [`docs/diagrama-classes.md`](docs/diagrama-classes.md)
(imagem: `docs/diagrama-classes.png`).

## 4. Contrato da API e códigos HTTP

| Método | Rota | Sucesso | Erros possíveis |
|---|---|---|---|
| GET | `/participantes` | 200 | 503 |
| GET | `/participantes/{id}` | 200 | 400 (id inválido), 404, 503 |
| POST | `/oficinas` | 201 | 400, 503 |
| GET | `/oficinas` | 200 | 503 |
| GET | `/oficinas/{id}` | 200 | 400, 404, 503 |
| PUT | `/oficinas/{id}` | 200 | 400, 404, 409 (capacidade com histórico), 503 |
| DELETE | `/oficinas/{id}` | 204 | 404, 409 (tem histórico), 503 |
| POST | `/inscricoes` | 201 | 400, 404 (participante/oficina), 409 (lotada/duplicidade), 503 |
| GET | `/inscricoes` | 200 | 503 |
| PATCH | `/inscricoes/{id}/cancelamento` | 200 | 404, 409 (já cancelada), 503 |

Erros seguem o formato `{"status": 409, "erro": "Conflict", "mensagem": "..."}`.
O mapeamento exceção → HTTP está centralizado em `TratadorGlobalExcecoes`.

## 5. Decisões técnicas essenciais

1. **JDBC puro, sem JPA.** Todo SQL é parametrizado (`PreparedStatement`) e fica nos repositories; recursos são
   fechados com `try-with-resources`.
2. **Credenciais fora do código.** `ConnectionFactory` lê `DB_URL`, `DB_USER` e `DB_PASSWORD` de variáveis de
   ambiente ou de `config/local.properties` (no `.gitignore`).
3. **Transação no Service, SQL no Repository.** Os repositories recebem a `Connection`; o Service faz
   `setAutoCommit(false)`, `commit` e `rollback`. Assim, "inscrever + consumir vaga" e "cancelar + devolver vaga"
   ocorrem na mesma transação. Detalhes e experimento em
   [`docs/transacao-e-concorrencia.md`](docs/transacao-e-concorrencia.md).
4. **Concorrência.** `SELECT ... FOR UPDATE` trava a oficina durante a transação, evitando que duas inscrições
   simultâneas consumam a mesma última vaga.
5. **Regras no domínio/service.** `Oficina` protege `0 <= vagasDisponiveis <= capacidade`
   (`reservarVaga`/`devolverVaga`); `Inscricao.cancelar()` impede cancelamento repetido.
6. **Histórico preservado.** Cancelar não apaga: muda a situação para `CANCELADA`. Oficina com histórico não pode ser
   excluída nem ter a capacidade alterada (só o título). Nova inscrição após cancelamento gera novo registro.
7. **Esquema do banco.** O código usa o banco oficial da professora (`sql/02_inscricao_oficinas.sql`), com as tabelas
   `participantes`, `oficinas` e `inscricoes`; todo SQL fica nos repositories.

## 6. Algoritmos (módulo `algoritmos`)

Coleção em memória de oficinas (gerada de forma reproduzível por `gerarOficinasDeExemplo`). Demonstração
executável: `DemoAlgoritmos`. Sem novas rotas na API.

| Algoritmo | Complexidade | Observação |
|---|---|---|
| Busca linear por ID | O(n) | funciona com a lista em qualquer ordem |
| Busca binária por ID | O(log n) | **exige a lista ordenada pela mesma chave (o ID)**; em lista desordenada pode não achar um elemento que existe |
| Ordenação por inserção (por ID ou por vagas disponíveis) | O(n²) pior caso/médio; O(n) se já ordenada | simples e estável; suficiente para coleções pequenas |

Ordenar custa O(n²), mas se compensa quando há muitas buscas na mesma coleção: uma ordenação seguida de
várias buscas binárias O(log n) supera várias buscas lineares O(n). Para poucas buscas, a linear basta.

## 7. Testes e evidências

- **JUnit** (`mvn test`): regras do domínio, regras e transações dos services (com dublês), mapeamento
  exceção → HTTP e algoritmos.
- **Postman — coleção oficial:** executada pelo Runner.
- **Postman — cenário próprio:** `postman/cenario-proprio.postman_collection.json`.

> **[ANEXAR]** em `docs/evidencias/`: print do `mvn test` com todos os testes passando, print do Runner da
> coleção oficial e print do Runner do cenário próprio. Veja `docs/evidencias/LEIA-ME.md`.

### Cenário próprio no Postman e por que é relevante

Cria uma oficina de 1 vaga, inscreve um participante, tenta inscrever outro (lotada), cancela, tenta cancelar de
novo e tenta excluir a oficina com histórico. Em **cada passo** confere o número de vagas. É relevante porque
prova, de ponta a ponta, que as vagas nunca ficam negativas nem acima da capacidade, que inscrever+consumir e
cancelar+devolver são atômicos e que uma operação rejeitada não deixa alteração parcial.

## 8. Refatoração registrada

> **[CONFERIR e preencher o hash do commit]** — este texto só vale se a refatoração foi realmente aplicada
> (instruções no guia de divisão do grupo, passo "Refatoração").

- **Problema encontrado:** o bloco "abrir conexão → `setAutoCommit(false)` → executar → `commit` / `rollback`"
  estava copiado em quatro métodos (`InscricaoService.registrar`, `CancelamentoService.cancelar`,
  `OficinaService.atualizar` e `OficinaService.excluir`). Duplicação: corrigir um detalhe exigia mexer em quatro lugares
  e era fácil esquecer um `rollback`.
- **Alteração:** extração da classe `ExecutorTransacional`, que recebe a operação (lambda) e cuida de abrir a conexão,
  `commit`, `rollback` e fechamento. Os services passam a conter só a regra de negócio. Commit: **[HASH]**.
- **Teste que validou:** `InscricaoServiceTest`, `CancelamentoServiceTest` e `OficinaServiceTest` (verificam
  `commit` no sucesso e `rollback` nos erros) passaram antes e depois, sem alteração nos testes.

## 9. Uso de Git

Cada integrante trabalhou em uma branch própria, com o seu nome (ex.: `03-lucas-vianna`), abriu Pull Request para a `main` e teve o PR
revisado por outro integrante. Os commits de cada pessoa estão identificados pelo nome/e-mail de cada um.

## 10. Declaração de uso de IA

Utilizamos uma ferramenta de IA generativa como **apoio** ao desenvolvimento, em partes do código, dos testes, da
estrutura do projeto, da documentação e do roteiro da demonstração. A divisão das tarefas, a revisão, a execução e a
validação foram feitas pelo grupo: cada integrante leu, executou e testou a sua parte e é capaz de explicar o que ela
faz e por quê. As decisões finais sobre o que entrou no projeto foram nossas.

> **[CONFIRMAR]** antes de entregar: esta declaração precisa ser verdadeira para todos os integrantes. Se alguém
> ainda não entende a sua parte, estude (cada `LEIA-ME-PESSOA-XX.md` tem uma cola de estudo) antes de gravar o vídeo.
