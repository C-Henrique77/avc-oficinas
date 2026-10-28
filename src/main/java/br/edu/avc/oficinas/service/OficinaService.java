package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.config.ExecutorTransacional;
import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;
import br.edu.avc.oficinas.repository.InscricaoRepository;
import br.edu.avc.oficinas.repository.OficinaRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Casos de uso de oficinas: CRUD com as regras de negocio sobre historico. */
@Service
public class OficinaService {

    private final ConnectionFactory connectionFactory;
    private final OficinaRepository oficinaRepository;
    private final InscricaoRepository inscricaoRepository;

    public OficinaService(ConnectionFactory connectionFactory,
                          OficinaRepository oficinaRepository,
                          InscricaoRepository inscricaoRepository) {
        this.connectionFactory = connectionFactory;
        this.oficinaRepository = oficinaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    public Oficina cadastrar(String titulo, Integer capacidade) {
        if (capacidade == null) {
            throw new DadosInvalidosException("Capacidade é obrigatória");
        }
        Oficina nova = Oficina.nova(titulo, capacidade); // valida titulo e capacidade
        try (Connection conexao = connectionFactory.abrir()) {
            return oficinaRepository.inserir(conexao, nova);
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Falha ao cadastrar oficina", e);
        }
    }

    public List<Oficina> listar() {
        try (Connection conexao = connectionFactory.abrir()) {
            return oficinaRepository.listar(conexao);
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Falha ao listar oficinas", e);
        }
    }

    public Oficina buscar(long id) {
        try (Connection conexao = connectionFactory.abrir()) {
            return oficinaRepository.buscarPorId(conexao, id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina " + id + " não encontrada"));
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Falha ao buscar oficina", e);
        }
    }

    /**
     * Atualiza a oficina. Regra: depois que existe historico de inscricoes, a capacidade
     * NAO pode mudar; so o titulo (campo descritivo).
     */
    public Oficina atualizar(long id, String titulo, Integer capacidade) {
        if (capacidade == null) {
            throw new DadosInvalidosException("Capacidade é obrigatória");
        }
        return ExecutorTransacional.executar(connectionFactory, "Falha ao atualizar oficina", conexao -> {
            Oficina oficina = oficinaRepository.buscarPorIdTravando(conexao, id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina " + id + " não encontrada"));
            boolean temHistorico = inscricaoRepository.contarPorOficina(conexao, id) > 0;

            if (temHistorico && capacidade != oficina.getCapacidade()) {
                throw new RegraNegocioException(
                        "A capacidade não pode ser alterada após existir histórico de inscrições");
            }
            oficina.atualizarTitulo(titulo);
            if (!temHistorico) {
                oficina.redefinirCapacidade(capacidade);
            }
            oficinaRepository.atualizar(conexao, oficina);
            return oficina;
        });
    }

    /** Exclui a oficina somente se nunca teve inscricoes (o historico nao pode ser destruido). */
    public void excluir(long id) {
        ExecutorTransacional.executar(connectionFactory, "Falha ao excluir oficina", conexao -> {
            oficinaRepository.buscarPorIdTravando(conexao, id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina " + id + " não encontrada"));
            if (inscricaoRepository.contarPorOficina(conexao, id) > 0) {
                throw new RegraNegocioException("Oficina com histórico de inscrições não pode ser excluída");
            }
            oficinaRepository.excluir(conexao, id);
            return null;
        });
    }
}
