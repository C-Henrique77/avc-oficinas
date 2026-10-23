package br.edu.avc.oficinas.config;

import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica de conexoes JDBC com o PostgreSQL.
 * URL, usuario e senha vem da configuracao externa (variaveis de ambiente ou
 * config/local.properties), nunca fixos no codigo.
 *
 * Cada chamada de abrir() devolve uma conexao nova; quem abriu e responsavel
 * por fecha-la (use try-with-resources).
 */
@Component
public class ConnectionFactory {

    private final String url;
    private final String usuario;
    private final String senha;

    public ConnectionFactory(@Value("${app.db.url}") String url,
                             @Value("${app.db.user}") String usuario,
                             @Value("${app.db.password}") String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    public Connection abrir() {
        try {
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            throw new ErroPersistenciaException("Banco de dados indisponível", e);
        }
    }
}
