package br.edu.avc.oficinas.excecao;

/** Referencia a um registro que nao existe (participante, oficina, inscricao). Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
