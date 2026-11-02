package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.dto.ErroResposta;
import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.excecao.DuplicidadeException;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;



/**
 * Traduz as excecoes do sistema em codigos HTTP e mensagens coerentes.
 * Resumo:
 *   400 dados invalidos | 404 referencia inexistente | 409 duplicidade/regra violada
 *   503 banco indisponivel
 */
@RestControllerAdvice
public class TratadorGlobalExcecoes {

    private static final Logger log = LoggerFactory.getLogger(TratadorGlobalExcecoes.class);

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResposta> dadosInvalidos(DadosInvalidosException e) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }



    @ExceptionHandler(DuplicidadeException.class)
    public ResponseEntity<ErroResposta> duplicidade(DuplicidadeException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ErroPersistenciaException.class)
    public ResponseEntity<ErroResposta> erroPersistencia(ErroPersistenciaException e) {
        log.error("Erro de persistencia", e);
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, "Servico temporariamente indisponivel.");
    }



    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return resposta(HttpStatus.BAD_REQUEST, "Parametro informado em formato invalido.");
    }



    private ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem) {
        ErroResposta erro = new ErroResposta(status.value(), status.getReasonPhrase(), mensagem);
        return ResponseEntity.status(status).body(erro);
    }
}
