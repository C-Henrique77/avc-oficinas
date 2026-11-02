# Transação e concorrência

Este documento explica **por que** e **como** as operações "registrar inscrição" e "cancelar inscrição"
são transacionais. É a base da explicação do item "Persistência e transação" do enunciado.

## 1. O problema

Registrar uma inscrição muda **duas** tabelas:

1. `oficina.vagas_disponiveis` diminui em 1;
2. uma linha nova entra em `inscricao`.

Se o sistema cair (ou der erro) entre os dois passos, ficaria uma vaga consumida sem inscrição, ou uma
inscrição sem vaga consumida. Uma **transação** garante "tudo ou nada": ou os dois passos são
confirmados (`commit`), ou nenhum fica gravado (`rollback`). O cancelamento é a ação inversa e também é
transacional.

## 2. Quem faz o quê

| Camada | Papel na transação |
|---|---|
| `ConnectionFactory` | abre a conexão JDBC |
| **Service** | decide onde a transação começa e termina: `setAutoCommit(false)`, `commit()` e `rollback()` |
| **Repository** | executa o SQL **na conexão recebida**; nunca faz commit/rollback e nunca abre conexão própria |

Por isso todos os métodos dos repositories recebem um `Connection`: dois repositories diferentes
(oficina e inscrição) usam a **mesma** conexão e, portanto, a mesma transação.

## 3. Fluxo de `registrar` (InscricaoService)

```
abre conexão e desliga o auto-commit
  1. participante existe?                  não -> 404, rollback
  2. oficina existe? (trava a linha)       não -> 404, rollback
  3. já existe inscrição ativa?            sim -> 409 duplicidade, rollback
  4. oficina.reservarVaga()                lotada -> 409, rollback
  5. UPDATE oficinas (vagas - 1)
  6. INSERT inscricao (ATIVA)
commit
```

## 4. Fluxo de `cancelar` (CancelamentoService)

```
abre conexão e desliga o auto-commit
  1. inscrição existe? (trava a linha)     não -> 404, rollback
  2. oficina existe? (trava a linha)       não -> 404, rollback
  3. inscricao.cancelar()                  já cancelada -> 409, rollback
  4. oficina.devolverVaga()                passaria da capacidade -> 409, rollback
  5. UPDATE inscricoes (CANCELADA)
  6. UPDATE oficinas (vagas + 1)
commit
```

## 5. Concorrência: por que `SELECT ... FOR UPDATE`

Imagine uma oficina com **1 vaga** e duas pessoas clicando ao mesmo tempo. Sem trava:

1. A lê `vagas = 1`; B lê `vagas = 1`;
2. A grava `vagas = 0` e cria a inscrição; B também grava `vagas = 0` e cria a inscrição;
3. Resultado: **duas** inscrições ativas para **uma** vaga.

Com `FOR UPDATE` (métodos `buscarPorIdTravando`), a linha da oficina fica travada até o fim da transação.
A segunda transação **espera**, e quando continua enxerga `vagas = 0`, então recebe "oficina lotada".

### Experimento para repetir (dois terminais com `psql`, banco oficial)

Terminal A:

```sql
BEGIN;
SELECT id, vagas_disponiveis FROM oficinas WHERE id = 1 FOR UPDATE;   -- trava a linha
-- (não finalize ainda; conte até 10)
UPDATE oficinas SET vagas_disponiveis = vagas_disponiveis - 1 WHERE id = 1;
COMMIT;
```

Terminal B (rode logo depois do `FOR UPDATE` do A):

```sql
BEGIN;
SELECT id, vagas_disponiveis FROM oficinas WHERE id = 1 FOR UPDATE;   -- fica ESPERANDO
```

Resultado esperado: o terminal B só responde depois do `COMMIT` do A, e já mostra a vaga decrementada.

## 6. Como provar o rollback

```sql
BEGIN;
UPDATE oficinas SET vagas_disponiveis = vagas_disponiveis - 1 WHERE id = 1;
SELECT vagas_disponiveis FROM oficinas WHERE id = 1;   -- mostra o valor alterado (dentro da transação)
ROLLBACK;
SELECT vagas_disponiveis FROM oficinas WHERE id = 1;   -- valor original de volta
```

Nos testes JUnit, o mesmo comportamento é verificado nos cenários "oficina lotada", "duplicidade" e
"participante inexistente": em todos, `rollback()` é chamado e nada é gravado (`atualizarVagas` e `inserir`
nunca são executados).

## 7. Garantia extra do banco

O banco oficial tem um índice único parcial (`uq_inscricao_ativa`) que impede duas inscrições ATIVAS do
mesmo participante na mesma oficina, e `CHECK`s que impedem vagas negativas ou acima da capacidade.
A regra de negócio vive no Service/domínio; o banco é a última linha de defesa. Quando o banco oficial chegar,
confira se ele possui garantias parecidas.
