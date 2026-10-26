# Diagrama de classes (UML)

O diagrama abaixo é escrito em Mermaid: o GitHub o desenha automaticamente ao abrir este arquivo.
Para gerar uma imagem (exigida no README), cole o código em <https://mermaid.live> e baixe o PNG
para `docs/diagrama-classes.png`.

## Domínio

```mermaid
classDiagram
    class Participante {
        -Long id
        -String nome
        -String email
        +getId() Long
        +getNome() String
        +getEmail() String
    }

    class Oficina {
        -Long id
        -String titulo
        -int capacidade
        -int vagasDisponiveis
        +nova(titulo, capacidade) Oficina$
        +temVaga() boolean
        +reservarVaga() void
        +devolverVaga() void
        +atualizarTitulo(novoTitulo) void
        +redefinirCapacidade(novaCapacidade) void
    }

    class Inscricao {
        -Long id
        -Long participanteId
        -Long oficinaId
        -LocalDateTime dataRegistro
        -SituacaoInscricao situacao
        +nova(participanteId, oficinaId) Inscricao$
        +estaAtiva() boolean
        +cancelar() void
    }

    class SituacaoInscricao {
        <<enumeration>>
        ATIVA
        CANCELADA
    }

    Participante "1" --> "0..*" Inscricao : realiza (participanteId)
    Oficina "1" --> "0..*" Inscricao : recebe (oficinaId)
    Inscricao --> SituacaoInscricao : situacao
```

Notas de modelagem:

- A ligação entre `Inscricao`, `Participante` e `Oficina` é feita pelos identificadores (chaves
  estrangeiras do banco), por isso a associação é rotulada com o nome do atributo.
- `Oficina` protege a invariante `0 <= vagasDisponiveis <= capacidade`: os atributos são privados e só
  mudam por `reservarVaga`, `devolverVaga` e `redefinirCapacidade`.
- `Inscricao` funciona como histórico: `cancelar()` apenas muda a situação, nunca apaga o registro.

## Camadas e dependências

```mermaid
classDiagram
    direction LR
    class OficinaController
    class InscricaoController
    class CancelamentoController
    class ParticipanteController
    class TratadorGlobalExcecoes

    class OficinaService
    class InscricaoService
    class CancelamentoService
    class ParticipanteService

    class OficinaRepository
    class InscricaoRepository
    class ParticipanteRepository
    class ConnectionFactory

    OficinaController --> OficinaService
    InscricaoController --> InscricaoService
    CancelamentoController --> CancelamentoService
    ParticipanteController --> ParticipanteService

    OficinaService --> OficinaRepository
    OficinaService --> InscricaoRepository
    InscricaoService --> ParticipanteRepository
    InscricaoService --> OficinaRepository
    InscricaoService --> InscricaoRepository
    CancelamentoService --> OficinaRepository
    CancelamentoService --> InscricaoRepository
    ParticipanteService --> ParticipanteRepository

    OficinaService --> ConnectionFactory
    InscricaoService --> ConnectionFactory
    CancelamentoService --> ConnectionFactory
    ParticipanteService --> ConnectionFactory
```

Fluxo: Postman/HTTP → Controller → Service → Repository → PostgreSQL.
O `TratadorGlobalExcecoes` traduz as exceções lançadas pelas camadas de baixo em códigos HTTP.
