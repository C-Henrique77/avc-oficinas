package br.edu.avc.oficinas.excecao;

/** Entrada invalida (campo vazio, numero negativo, id ausente). Vira HTTP 400. */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
