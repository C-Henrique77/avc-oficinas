package br.edu.avc.oficinas.repository;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.model.Oficina;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.excecao.DuplicidadeException;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OficinaRepository {

    private final ConnectionFactory connectionFactory;

    public OficinaRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Oficina salvar(Oficina oficina) {
        String sql = "INSERT INTO oficina (titulo, capacidade, vagas_disponiveis) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, oficina.getTitulo());
            stmt.setInt(2, oficina.getCapacidade());
            stmt.setInt(3, oficina.getVagasDisponiveis());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    oficina.setId(rs.getLong("id"));
                }
            }
            return oficina;

        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao salvar oficina: " + e.getMessage());
        }
    }

    public List<Oficina> listarTodas() {
        String sql = "SELECT id, titulo, capacidade, vagas_disponiveis FROM oficina ORDER BY id";
        List<Oficina> oficinas = new ArrayList<>();

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                oficinas.add(mapear(rs));
            }
            return oficinas;

        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao listar oficinas: " + e.getMessage());
        }
    }

    public Oficina buscarPorId(Long id) {
        String sql = "SELECT id, titulo, capacidade, vagas_disponiveis FROM oficina WHERE id = ?";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

            throw new RecursoNaoEncontradoException("Oficina não encontrada: " + id);

        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao buscar oficina: " + e.getMessage());
        }
    }

    public Oficina atualizar(Long id, Oficina oficina) {
        String sql = "UPDATE oficina SET titulo = ?, capacidade = ?, vagas_disponiveis = ? WHERE id = ?";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, oficina.getTitulo());
            stmt.setInt(2, oficina.getCapacidade());
            stmt.setInt(3, oficina.getVagasDisponiveis());
            stmt.setLong(4, id);

            if (stmt.executeUpdate() == 0) {
                throw new RecursoNaoEncontradoException("Oficina não encontrada: " + id);
            }

            oficina.setId(id);
            return oficina;

        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao atualizar oficina: " + e.getMessage());
        }
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM oficina WHERE id = ?";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            if (stmt.executeUpdate() == 0) {
                throw new RecursoNaoEncontradoException("Oficina não encontrada: " + id);
            }

        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao excluir oficina: " + e.getMessage());
        }
    }

    private Oficina mapear(ResultSet rs) throws SQLException {
        return new Oficina(
                rs.getLong("id"),
                rs.getString("titulo"),
                rs.getInt("capacidade"),
                rs.getInt("vagas_disponiveis")
        );
    }
}
