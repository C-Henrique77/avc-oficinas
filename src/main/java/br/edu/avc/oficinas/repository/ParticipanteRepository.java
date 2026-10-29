package br.edu.avc.oficinas.repository;

import br.edu.avc.oficinas.dominio.Participante;
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
 * Acesso ao PostgreSQL (JDBC) para participantes. So consulta.
 * Todo SQL fica aqui; nenhuma regra de negocio. A Connection vem de fora
 * (do Service) para que varias operacoes possam participar da mesma transacao.
 */
@Repository
public class ParticipanteRepository {

    public List<Participante> listar(Connection conexao) {
        String sql = "SELECT id, nome, email FROM participante ORDER BY id";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Participante> participantes = new ArrayList<>();
            while (rs.next()) {
                participantes.add(mapear(rs));
            }
            return participantes;
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao listar participantes", e);
        }
    }

    public Optional<Participante> buscarPorId(Connection conexao, long id) {
        String sql = "SELECT id, nome, email FROM participante WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao buscar participante", e);
        }
    }

    private Participante mapear(ResultSet rs) throws SQLException {
        return new Participante(rs.getLong("id"), rs.getString("nome"), rs.getString("email"));
    }
}
