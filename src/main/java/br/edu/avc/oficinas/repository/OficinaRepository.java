package br.edu.avc.oficinas.repository;

import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso ao PostgreSQL (JDBC) para oficinas. Apenas SQL parametrizado,
 * sem regras de negocio. A Connection e recebida de fora (transacao do Service).
 */
@Repository
public class OficinaRepository {

    private static final String COLUNAS = "id, titulo, capacidade, vagas_disponiveis";

    public Oficina inserir(Connection conexao, Oficina oficina) {
        String sql = "INSERT INTO oficinas (titulo, capacidade, vagas_disponiveis) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, oficina.getTitulo());
            ps.setInt(2, oficina.getCapacidade());
            ps.setInt(3, oficina.getVagasDisponiveis());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Oficina(rs.getLong(1), oficina.getTitulo(),
                        oficina.getCapacidade(), oficina.getVagasDisponiveis());
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao cadastrar oficina", e);
        }
    }

    public List<Oficina> listar(Connection conexao) {
        String sql = "SELECT " + COLUNAS + " FROM oficinas ORDER BY id";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Oficina> oficinas = new ArrayList<>();
            while (rs.next()) {
                oficinas.add(mapear(rs));
            }
            return oficinas;
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao listar oficinas", e);
        }
    }

    public Optional<Oficina> buscarPorId(Connection conexao, long id) {
        return buscar(conexao, "SELECT " + COLUNAS + " FROM oficinas WHERE id = ?", id);
    }

    /**
     * Busca e TRAVA a linha (SELECT ... FOR UPDATE) ate o fim da transacao.
     * Evita que duas inscricoes simultaneas consumam a mesma ultima vaga.
     */
    public Optional<Oficina> buscarPorIdTravando(Connection conexao, long id) {
        return buscar(conexao, "SELECT " + COLUNAS + " FROM oficinas WHERE id = ? FOR UPDATE", id);
    }

    /** Atualiza os dados cadastrais e as vagas (usado no PUT). */
    public void atualizar(Connection conexao, Oficina oficina) {
        String sql = "UPDATE oficinas SET titulo = ?, capacidade = ?, vagas_disponiveis = ? WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, oficina.getTitulo());
            ps.setInt(2, oficina.getCapacidade());
            ps.setInt(3, oficina.getVagasDisponiveis());
            ps.setLong(4, oficina.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao atualizar oficina", e);
        }
    }

    /** Atualiza somente as vagas (usado ao inscrever e ao cancelar). */
    public void atualizarVagas(Connection conexao, Oficina oficina) {
        String sql = "UPDATE oficinas SET vagas_disponiveis = ? WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, oficina.getVagasDisponiveis());
            ps.setLong(2, oficina.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao atualizar vagas da oficina", e);
        }
    }

    /** @return true se uma linha foi removida. */
    public boolean excluir(Connection conexao, long id) {
        String sql = "DELETE FROM oficinas WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao excluir oficina", e);
        }
    }

    private Optional<Oficina> buscar(Connection conexao, String sql, long id) {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao buscar oficina", e);
        }
    }

    private Oficina mapear(ResultSet rs) throws SQLException {
        return new Oficina(rs.getLong("id"), rs.getString("titulo"),
                rs.getInt("capacidade"), rs.getInt("vagas_disponiveis"));
    }
}
