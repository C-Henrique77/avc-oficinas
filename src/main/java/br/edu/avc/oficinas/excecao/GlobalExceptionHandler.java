
package br.edu.avc.oficinas.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErroResponse(String mensagem) {
    }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResponse> dadosInvalidos(
            DadosInvalidosException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponse(e.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(
            RecursoNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponse(e.getMessage()));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(
            RegraNegocioException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse(e.getMessage()));
    }

    @ExceptionHandler(DuplicidadeException.class)
    public ResponseEntity<ErroResponse> duplicidade(
            DuplicidadeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResponse(e.getMessage()));
    }

    @ExceptionHandler(ErroPersistenciaException.class)
    public ResponseEntity<ErroResponse> erroPersistencia(
            ErroPersistenciaException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErroResponse(e.getMessage()));
    }
}
