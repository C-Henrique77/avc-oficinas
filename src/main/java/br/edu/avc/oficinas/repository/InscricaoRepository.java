package br.edu.avc.oficinas.repository;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.excecao.DuplicidadeException;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import br.edu.avc.oficinas.model.Inscricao;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InscricaoRepository {

    private final ConnectionFactory connectionFactory;

    public InscricaoRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Inscricao salvar(Inscricao inscricao) {
        String sql = "INSERT INTO inscricao "
                + "(participante_id, oficina_id, situacao) "
                + "VALUES (?, ?, ?) RETURNING id, data_registro";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, inscricao.getParticipanteId());
            stmt.setLong(2, inscricao.getOficinaId());
            stmt.setString(3, inscricao.getSituacao());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    inscricao.setId(rs.getLong("id"));

                    Timestamp data = rs.getTimestamp("data_registro");
                    if (data != null) {
                        inscricao.setDataRegistro(data.toLocalDateTime());
                    }
                }
            }

            return inscricao;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException(
                        "O participante já possui uma inscrição ativa nesta oficina.");
            }

            throw new ErroPersistenciaException(
                    "Erro ao salvar inscrição: " + e.getMessage());
        }
    }

    public List<Inscricao> listarTodas() {
        String sql = "SELECT id, participante_id, oficina_id, "
                + "data_registro, situacao FROM inscricao ORDER BY id";

        List<Inscricao> inscricoes = new ArrayList<>();

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                inscricoes.add(mapear(rs));
            }

            return inscricoes;

        } catch (SQLException e) {
            throw new ErroPersistenciaException(
                    "Erro ao listar inscrições: " + e.getMessage());
        }
    }

    public boolean existeInscricaoAtiva(Long participanteId, Long oficinaId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM inscricao "
                + "WHERE participante_id = ? AND oficina_id = ? "
                + "AND situacao = 'ATIVA')";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, participanteId);
            stmt.setLong(2, oficinaId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }

        } catch (SQLException e) {
            throw new ErroPersistenciaException(
                    "Erro ao verificar inscrição: " + e.getMessage());
        }
    }

    private Inscricao mapear(ResultSet rs) throws SQLException {
        Timestamp data = rs.getTimestamp("data_registro");

        return new Inscricao(
                rs.getLong("id"),
                rs.getLong("participante_id"),
                rs.getLong("oficina_id"),
                data == null ? null : data.toLocalDateTime(),
                rs.getString("situacao")
        );
    }
}
