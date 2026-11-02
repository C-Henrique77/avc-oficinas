-- Consultas para CONFERIR o estado do banco (use durante a demonstracao e os testes).

-- 1) As vagas gravadas batem com as inscricoes ativas?
--    vagas_disponiveis deve ser IGUAL a vagas_esperadas em todas as linhas.
SELECT o.id,
       o.titulo,
       o.capacidade,
       o.vagas_disponiveis,
       COUNT(i.id) FILTER (WHERE i.situacao = 'ATIVA')                  AS inscricoes_ativas,
       o.capacidade - COUNT(i.id) FILTER (WHERE i.situacao = 'ATIVA')   AS vagas_esperadas
FROM oficinas o
LEFT JOIN inscricoes i ON i.oficina_id = o.id
GROUP BY o.id
ORDER BY o.id;

-- 2) Historico completo de inscricoes (ativas e canceladas)
SELECT i.id, p.nome AS participante, o.titulo AS oficina, i.data_registro, i.situacao
FROM inscricoes i
JOIN participantes p ON p.id = i.participante_id
JOIN oficinas o      ON o.id = i.oficina_id
ORDER BY i.id;
