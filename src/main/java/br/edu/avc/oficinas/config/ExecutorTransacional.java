package br.edu.avc.oficinas.config;

import br.edu.avc.oficinas.excecao.ErroPersistenciaException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Executa uma operacao dentro de UMA transacao JDBC:
 * abre a conexao, desliga o auto-commit, roda a operacao e
 * faz commit se tudo deu certo ou rollback se qualquer etapa falhar.
 *
 * Substitui o bloco try/commit/rollback que estava copiado em quatro metodos dos Services.
 */
public final class ExecutorTransacional {

    private ExecutorTransacional() {
    }

    /** Operacao que roda usando a conexao (e, portanto, a transacao) recebida. */
    @FunctionalInterface
    public interface Operacao<T> {
        T executar(Connection conexao);
    }

    public static <T> T executar(ConnectionFactory connectionFactory, String mensagemDeErro, Operacao<T> operacao) {
        try (Connection conexao = connectionFactory.abrir()) {
            conexao.setAutoCommit(false);
            try {
                T resultado = operacao.executar(conexao);
                conexao.commit();
                return resultado;
            } catch (RuntimeException | SQLException e) {
                conexao.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ErroPersistenciaException(mensagemDeErro, e);
        }
    }
}
