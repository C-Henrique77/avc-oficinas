package br.edu.avc.oficinas.excecao;

/** Falha de infraestrutura (banco indisponivel, erro de SQL). Vira HTTP 503. */
public class ErroPersistenciaException extends RuntimeException {

    public ErroPersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
