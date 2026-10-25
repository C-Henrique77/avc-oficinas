package br.edu.avc.oficinas.repository;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.model.Participante;
import br.edu.avc.oficinas.excecao.DuplicidadeException;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ParticipanteRepository {

    private final ConnectionFactory connectionFactory;

    public ParticipanteRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Participante salvar(Participante participante) {
        String sql = "INSERT INTO participante (nome, email) VALUES (?, ?) RETURNING id";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, participante.getNome());
            stmt.setString(2, participante.getEmail());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    participante.setId(rs.getLong("id"));
                }
            }

            return participante;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Já existe um participante com esse e-mail.");
            }
            throw new ErroPersistenciaException(
                    "Erro ao salvar participante: " + e.getMessage());
        }
    }

    public List<Participante> listarTodos() {
        String sql = "SELECT id, nome, email FROM participante ORDER BY id";
        List<Participante> participantes = new ArrayList<>();

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                participantes.add(mapear(rs));
            }

            return participantes;

        } catch (SQLException e) {
            throw new ErroPersistenciaException(
                    "Erro ao listar participantes: " + e.getMessage());
        }
    }

    public Participante buscarPorId(Long id) {
        String sql = "SELECT id, nome, email FROM participante WHERE id = ?";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

            throw new RecursoNaoEncontradoException(
                    "Participante não encontrado: " + id);

        } catch (SQLException e) {
            throw new ErroPersistenciaException(
                    "Erro ao buscar participante: " + e.getMessage());
        }
    }

    private Participante mapear(ResultSet rs) throws SQLException {
        return new Participante(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("email")
        );
    }
}
