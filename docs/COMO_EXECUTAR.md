# Como executar o projeto

## 1. O que você precisa instalado

| Ferramenta | Versão | Para quê |
|---|---|---|
| JDK | 17 ou superior | compilar e rodar o Java |
| Maven | 3.9 ou superior | baixar dependências, rodar testes e a aplicação |
| PostgreSQL | 14 ou superior | banco de dados |
| Postman | qualquer | testar a API (coleção oficial + cenário próprio) |

Confira no terminal: `java -version` e `mvn -version`.

## 2. Banco de dados

1. Crie um banco chamado `oficinas` (ou use o nome do banco oficial fornecido pela professora).
2. Carregue o banco:
   - **Banco oficial:** execute o script que a professora disponibilizou.
   - **Banco de exemplo (só para desenvolver):** execute `sql/schema-exemplo.sql`.

   Pelo terminal (ajuste usuário e caminho):

   ```
   psql -U postgres -c "CREATE DATABASE oficinas"
   psql -U postgres -d oficinas -f sql/schema-exemplo.sql
   ```

   Pelo pgAdmin: clique com o botão direito no banco `oficinas` > Query Tool > abra o arquivo `.sql` > execute.

> Se os nomes de tabelas/colunas do banco oficial forem diferentes dos do exemplo, ajuste **somente** os SQLs
> dentro das classes `*Repository` (e o texto das situações em `SituacaoInscricao`, se necessário).

## 3. Credenciais (nunca vão para o Git)

Copie o arquivo de exemplo e preencha:

```
copy config\local.properties.example config\local.properties      (Windows)
cp config/local.properties.example config/local.properties        (Linux/macOS)
```

Conteúdo de `config/local.properties`:

```
DB_URL=jdbc:postgresql://localhost:5432/oficinas
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
```

O arquivo `config/local.properties` está no `.gitignore`, então não é versionado.

Alternativa: definir variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASSWORD`
(PowerShell: `$env:DB_USER="postgres"`).

## 4. Rodar a API

```
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Teste no navegador: `http://localhost:8080/oficinas`.

Pela IDE: execute a classe `OficinasApplication`.

## 5. Rodar os testes automatizados (JUnit)

```
mvn test
```

Os testes JUnit **não precisam do banco** (usam dublês de teste), então rodam em qualquer máquina.

## 6. Postman

1. Importe a **coleção oficial** da professora e execute pelo Runner (guarde o print do resultado).
2. Importe `postman/cenario-proprio.postman_collection.json` e execute pelo Runner.
   O cenário usa os participantes de id 1 e 2; se o banco oficial usar outros ids, altere as variáveis
   `participanteId1` e `participanteId2` da coleção.

## 7. Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `Could not resolve placeholder 'DB_USER'` ao subir | credenciais não configuradas | refaça o passo 3 |
| Respostas HTTP 503 | banco desligado, senha errada ou banco inexistente | confira o PostgreSQL e o `local.properties`; o motivo exato aparece no log do console |
| `Port 8080 was already in use` | outra aplicação usando a porta | feche a outra aplicação ou mude `server.port` em `application.properties` |
| `mvn` não é reconhecido | Maven fora do PATH | reinstale/adicione o Maven ao PATH e reabra o terminal |
