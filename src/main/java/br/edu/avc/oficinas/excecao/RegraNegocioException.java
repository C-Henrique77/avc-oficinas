package br.edu.avc.oficinas.excecao;

/**
 * Violacao de regra de negocio / estado impossivel (oficina lotada,
 * cancelamento repetido, exclusao com historico...). Vira HTTP 409.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
