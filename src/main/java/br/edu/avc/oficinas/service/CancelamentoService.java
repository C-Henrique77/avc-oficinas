package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.dominio.Inscricao;
import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.repository.InscricaoRepository;
import br.edu.avc.oficinas.repository.OficinaRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

/** Caso de uso: cancelar uma inscricao e devolver a vaga (acao inversa do registro). */
@Service
public class CancelamentoService {

    private final ConnectionFactory connectionFactory;
    private final OficinaRepository oficinaRepository;
    private final InscricaoRepository inscricaoRepository;

    public CancelamentoService(ConnectionFactory connectionFactory,
                               OficinaRepository oficinaRepository,
                               InscricaoRepository inscricaoRepository) {
        this.connectionFactory = connectionFactory;
        this.oficinaRepository = oficinaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    /**
     * Cancela a inscricao e devolve a vaga NA MESMA TRANSACAO.
     * Regras: nao pode cancelar duas vezes e as vagas nunca passam da capacidade.
     */
    public Inscricao cancelar(long inscricaoId) {
        try (Connection conexao = connectionFactory.abrir()) {
            conexao.setAutoCommit(false);
            try {
                Inscricao inscricao = inscricaoRepository.buscarPorIdTravando(conexao, inscricaoId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Inscrição " + inscricaoId + " não encontrada"));
                Oficina oficina = oficinaRepository.buscarPorIdTravando(conexao, inscricao.getOficinaId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Oficina " + inscricao.getOficinaId() + " não encontrada"));

                inscricao.cancelar();   // regra do dominio: falha se ja estiver cancelada
                oficina.devolverVaga(); // regra do dominio: falha se passaria da capacidade

                inscricaoRepository.atualizarSituacao(conexao, inscricao);
                oficinaRepository.atualizarVagas(conexao, oficina);

                conexao.commit();
                return inscricao;
            } catch (RuntimeException | SQLException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Falha ao cancelar inscrição", e);
        }
    }
}
