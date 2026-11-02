package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.dominio.Participante;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Casos de uso de participantes (somente consulta). */
@Service
public class ParticipanteService {

    private final ConnectionFactory connectionFactory;
    private final ParticipanteRepository participanteRepository;

    public ParticipanteService(ConnectionFactory connectionFactory,
                               ParticipanteRepository participanteRepository) {
        this.connectionFactory = connectionFactory;
        this.participanteRepository = participanteRepository;
    }

    public List<Participante> listar() {
        try (Connection conexao = connectionFactory.abrir()) {
            return participanteRepository.listar(conexao);
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Falha ao listar participantes", e);
        }
    }

    public Participante buscar(long id) {
        try (Connection conexao = connectionFactory.abrir()) {
            return participanteRepository.buscarPorId(conexao, id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Participante " + id + " não encontrado"));
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Falha ao buscar participante", e);
        }
    }
}
