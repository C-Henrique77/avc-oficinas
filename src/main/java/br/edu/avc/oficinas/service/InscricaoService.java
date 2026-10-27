package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.dominio.Inscricao;
import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.excecao.DuplicidadeException;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.repository.InscricaoRepository;
import br.edu.avc.oficinas.repository.OficinaRepository;
import br.edu.avc.oficinas.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Casos de uso de inscricao: registrar (consome vaga) e listar o historico. */
@Service
public class InscricaoService {

    private final ConnectionFactory connectionFactory;
    private final ParticipanteRepository participanteRepository;
    private final OficinaRepository oficinaRepository;
    private final InscricaoRepository inscricaoRepository;

    public InscricaoService(ConnectionFactory connectionFactory,
                            ParticipanteRepository participanteRepository,
                            OficinaRepository oficinaRepository,
                            InscricaoRepository inscricaoRepository) {
        this.connectionFactory = connectionFactory;
        this.participanteRepository = participanteRepository;
        this.oficinaRepository = oficinaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    /**
     * Registra a inscricao e consome uma vaga NA MESMA TRANSACAO:
     * se qualquer etapa falhar, nada fica gravado (rollback).
     */
    public Inscricao registrar(Long participanteId, Long oficinaId) {
        if (participanteId == null || oficinaId == null) {
            throw new DadosInvalidosException("participanteId e oficinaId são obrigatórios");
        }
        try (Connection conexao = connectionFactory.abrir()) {
            conexao.setAutoCommit(false);
            try {
                participanteRepository.buscarPorId(conexao, participanteId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Participante " + participanteId + " não encontrado"));
                Oficina oficina = oficinaRepository.buscarPorIdTravando(conexao, oficinaId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Oficina " + oficinaId + " não encontrada"));

                if (inscricaoRepository.existeAtiva(conexao, participanteId, oficinaId)) {
                    throw new DuplicidadeException("Participante já possui inscrição ativa nesta oficina");
                }

                oficina.reservarVaga(); // regra do dominio: falha se estiver lotada
                oficinaRepository.atualizarVagas(conexao, oficina);
                Inscricao salva = inscricaoRepository.inserir(conexao, Inscricao.nova(participanteId, oficinaId));

                conexao.commit();
                return salva;
            } catch (RuntimeException | SQLException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Falha ao registrar inscrição", e);
        }
    }

    public List<Inscricao> listar() {
        try (Connection conexao = connectionFactory.abrir()) {
            return inscricaoRepository.listar(conexao);
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Falha ao listar inscrições", e);
        }
    }
}
