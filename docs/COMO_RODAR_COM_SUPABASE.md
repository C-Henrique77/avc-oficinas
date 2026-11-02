# Como rodar o projeto com o banco no Supabase

O Supabase é um PostgreSQL hospedado na nuvem. O código do projeto não muda: só mudam a URL, o usuário e a
senha de conexão, que ficam fora do repositório (`config/local.properties`).

## 1. Criar o banco

1. Criar um projeto no Supabase e anotar a senha do banco.
2. Abrir o **SQL Editor**, colar o conteúdo de `sql/02_inscricao_oficinas.sql` e executar.
3. Conferir no **Table Editor** as tabelas `participantes` (5 linhas), `oficinas` (5 linhas) e `inscricoes` (vazia).

## 2. Configurar a conexão

No botão **Connect** do projeto, usar o **Session pooler** (porta 5432). Não usar o Transaction pooler (6543),
porque ele atrapalha o `PreparedStatement` e o controle de transação do projeto.

Copiar `config/local.properties.example` para `config/local.properties` e preencher:

```
DB_URL=jdbc:postgresql://<host-do-session-pooler>:5432/postgres?sslmode=require
DB_USER=postgres.<codigo-do-projeto>
DB_PASSWORD=<senha-do-banco>
```

Esse arquivo está no `.gitignore` e não deve ser versionado.

## 3. Rodar

```
mvn test                 # 42 testes JUnit (não dependem do banco)
mvn spring-boot:run      # sobe a API em http://localhost:8080
```

Requisitos: JDK 17 e Maven 3.9.

## 4. Postman

Importar `postman/cenario-proprio.postman_collection.json` e `postman/colecao-contrato.postman_collection.json`
e rodar cada coleção pelo **Runner**, com a API no ar e o banco recém-carregado.

Para voltar o banco ao estado inicial entre as execuções, no SQL Editor:

```
DROP TABLE IF EXISTS inscricoes;
DROP TABLE IF EXISTS oficinas;
DROP TABLE IF EXISTS participantes;
```

e depois executar de novo `sql/02_inscricao_oficinas.sql` inteiro.

## 5. Conferir as vagas no banco

`sql/verificacao.sql` mostra, para cada oficina, `vagas_disponiveis` e `vagas_esperadas` (capacidade menos as
inscrições ativas). Os dois valores precisam ser iguais em todas as linhas.

## 6. Evidências

Em `docs/evidencias/`:

- `mvn-test.png`: 42 testes, 0 falhas, `BUILD SUCCESS`.
- `postman-cenario-proprio.png`: cenário próprio, 15 de 15 testes passando.
- `postman-colecao-oficial.png`: coleção do contrato do Tema 2, 56 testes, 0 erros.
