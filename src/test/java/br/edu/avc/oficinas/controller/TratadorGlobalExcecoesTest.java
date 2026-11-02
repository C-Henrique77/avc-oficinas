package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.excecao.DuplicidadeException;
import br.edu.avc.oficinas.excecao.ErroPersistenciaException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;
import br.edu.avc.oficinas.service.OficinaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica o mapeamento excecao -> codigo HTTP, sem subir o Spring inteiro nem o banco. */
class TratadorGlobalExcecoesTest {

    private OficinaService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = mock(OficinaService.class);
        mvc = MockMvcBuilders.standaloneSetup(new OficinaController(service))
                .setControllerAdvice(new TratadorGlobalExcecoes())
                .build();
    }

    @Test
    void referenciaInexistenteViraHttp404() throws Exception {
        when(service.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Oficina 99 não encontrada"));

        mvc.perform(get("/oficinas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Oficina 99 não encontrada"));
    }

    @Test
    void regraDeNegocioViolada_viraHttp409() throws Exception {
        when(service.buscar(1L)).thenThrow(new RegraNegocioException("Oficina lotada: não há vagas disponíveis"));

        mvc.perform(get("/oficinas/1")).andExpect(status().isConflict());
    }

    @Test
    void duplicidadeViraHttp409() throws Exception {
        when(service.buscar(1L)).thenThrow(new DuplicidadeException("duplicado"));

        mvc.perform(get("/oficinas/1")).andExpect(status().isConflict());
    }

    @Test
    void bancoIndisponivelViraHttp503SemVazarDetalhesInternos() throws Exception {
        when(service.listar()).thenThrow(new ErroPersistenciaException("falha", new SQLException("senha errada")));

        mvc.perform(get("/oficinas"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("senha"))));
    }

    @Test
    void jsonMalformadoViraHttp400() throws Exception {
        mvc.perform(post("/oficinas").contentType(MediaType.APPLICATION_JSON).content("{titulo: ???"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void idNaoNumericoViraHttp400() throws Exception {
        mvc.perform(get("/oficinas/abc")).andExpect(status().isBadRequest());
    }
}
