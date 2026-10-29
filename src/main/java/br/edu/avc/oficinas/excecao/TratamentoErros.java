package br.edu.avc.oficinas.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestControllerAdvice
public class TratamentoErros {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return excecao.getMessage();
    }
}