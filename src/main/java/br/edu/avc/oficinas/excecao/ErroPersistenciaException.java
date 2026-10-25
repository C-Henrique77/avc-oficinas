package br.edu.avc.oficinas.excecao;

public class ErroPersistenciaException extends RuntimeException {

    public ErroPersistenciaException(String mensagem) {
        super(mensagem);
    }

    public ErroPersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
