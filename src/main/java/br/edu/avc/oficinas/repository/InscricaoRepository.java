package br.edu.avc.oficinas.repository;

import br.edu.avc.oficinas.dominio.Inscricao;
import br.edu.avc.oficinas.dominio.SituacaoInscricao;
import br.edu.avc.oficinas.excecao.DuplicidadeException;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso ao PostgreSQL (JDBC) para inscricoes (o historico).
 * Nao faz commit nem rollback: recebe a Connection do Service, assim a inscricao e a
 * atualizacao de vagas da oficina participam da MESMA transacao.
 */
@Repository
public class InscricaoRepository {

    private static final String COLUNAS = "id, participante_id, oficina_id, data_registro, situacao";

    public Inscricao inserir(Connection conexao, Inscricao inscricao) {
        String sql = "INSERT INTO inscricoes (participante_id, oficina_id, data_registro, situacao) "
                + "VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, inscricao.getParticipanteId());
            ps.setLong(2, inscricao.getOficinaId());
            ps.setTimestamp(3, Timestamp.valueOf(inscricao.getDataRegistro()));
            ps.setString(4, inscricao.getSituacao().name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Inscricao(rs.getLong(1), inscricao.getParticipanteId(), inscricao.getOficinaId(),
                        inscricao.getDataRegistro(), inscricao.getSituacao());
            }
        } catch (SQLException e) {
            // 23505 = violacao de unicidade (garantia final do banco contra duplicidade ativa)
            if ("23505".equals(e.getSQLState())) {
                throw new DuplicidadeException("Participante já possui inscrição ativa nesta oficina");
            }
            throw new ErroPersistenciaException("Erro ao registrar inscrição", e);
        }
    }

    public List<Inscricao> listar(Connection conexao) {
        String sql = "SELECT " + COLUNAS + " FROM inscricoes ORDER BY id";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Inscricao> inscricoes = new ArrayList<>();
            while (rs.next()) {
                inscricoes.add(mapear(rs));
            }
            return inscricoes;
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao listar inscrições", e);
        }
    }

    public Optional<Inscricao> buscarPorId(Connection conexao, long id) {
        return buscar(conexao, "SELECT " + COLUNAS + " FROM inscricoes WHERE id = ?", id);
    }

    /** Busca e trava a inscricao ate o fim da transacao (evita cancelamentos simultaneos). */
    public Optional<Inscricao> buscarPorIdTravando(Connection conexao, long id) {
        return buscar(conexao, "SELECT " + COLUNAS + " FROM inscricoes WHERE id = ? FOR UPDATE", id);
    }

    public boolean existeAtiva(Connection conexao, long participanteId, long oficinaId) {
        String sql = "SELECT 1 FROM inscricoes WHERE participante_id = ? AND oficina_id = ? AND situacao = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, participanteId);
            ps.setLong(2, oficinaId);
            ps.setString(3, SituacaoInscricao.ATIVA.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao verificar inscrição ativa", e);
        }
    }

    /** Quantidade de inscricoes (ativas ou canceladas) da oficina: se > 0, ha historico. */
    public int contarPorOficina(Connection conexao, long oficinaId) {
        String sql = "SELECT COUNT(*) FROM inscricoes WHERE oficina_id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, oficinaId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao contar inscrições da oficina", e);
        }
    }

    public void atualizarSituacao(Connection conexao, Inscricao inscricao) {
        String sql = "UPDATE inscricoes SET situacao = ? WHERE id = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, inscricao.getSituacao().name());
            ps.setLong(2, inscricao.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao atualizar situação da inscrição", e);
        }
    }

    private Optional<Inscricao> buscar(Connection conexao, String sql, long id) {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Erro ao buscar inscrição", e);
        }
    }

    private Inscricao mapear(ResultSet rs) throws SQLException {
        return new Inscricao(
                rs.getLong("id"),
                rs.getLong("participante_id"),
                rs.getLong("oficina_id"),
                rs.getTimestamp("data_registro").toLocalDateTime(),
                SituacaoInscricao.valueOf(rs.getString("situacao")));
    }
}
