package br.edu.avc.oficinas.excecao;

/** Operacao que criaria um registro duplicado (ex.: inscricao ativa repetida). Vira HTTP 409. */
public class DuplicidadeException extends RuntimeException {

    public DuplicidadeException(String mensagem) {
        super(mensagem);
    }
}
