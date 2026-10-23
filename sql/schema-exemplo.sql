-- =====================================================================
-- ATENCAO: este script e apenas um BANCO DE EXEMPLO para desenvolver
-- localmente. O banco OFICIAL e o que a professora disponibilizar.
-- Quando receber o banco oficial, confira nomes de tabelas/colunas e
-- ajuste SOMENTE os SQLs dentro das classes *Repository.
-- =====================================================================

DROP TABLE IF EXISTS inscricao;
DROP TABLE IF EXISTS oficina;
DROP TABLE IF EXISTS participante;

CREATE TABLE participante (
    id    BIGSERIAL PRIMARY KEY,
    nome  VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE
);

CREATE TABLE oficina (
    id                BIGSERIAL PRIMARY KEY,
    titulo            VARCHAR(160) NOT NULL,
    capacidade        INT NOT NULL CHECK (capacidade > 0),
    vagas_disponiveis INT NOT NULL,
    CHECK (vagas_disponiveis >= 0 AND vagas_disponiveis <= capacidade)
);

CREATE TABLE inscricao (
    id              BIGSERIAL PRIMARY KEY,
    participante_id BIGINT NOT NULL REFERENCES participante (id),
    oficina_id      BIGINT NOT NULL REFERENCES oficina (id),
    data_registro   TIMESTAMP NOT NULL DEFAULT now(),
    situacao        VARCHAR(20) NOT NULL CHECK (situacao IN ('ATIVA', 'CANCELADA'))
);

-- No maximo uma inscricao ATIVA por participante em cada oficina
CREATE UNIQUE INDEX ux_inscricao_ativa
    ON inscricao (participante_id, oficina_id)
    WHERE situacao = 'ATIVA';

-- Dados iniciais (participantes sao somente consultados na AVC)
INSERT INTO participante (nome, email) VALUES
    ('Ana Souza',      'ana@exemplo.com'),
    ('Bruno Lima',     'bruno@exemplo.com'),
    ('Carla Mendes',   'carla@exemplo.com'),
    ('Diego Ferreira', 'diego@exemplo.com'),
    ('Elisa Rocha',    'elisa@exemplo.com');

INSERT INTO oficina (titulo, capacidade, vagas_disponiveis) VALUES
    ('Introdução ao Java',  3, 3),
    ('Git e GitHub',        2, 2),
    ('Testes com JUnit',    1, 1);
