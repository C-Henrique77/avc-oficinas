package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.dominio.Inscricao;
import br.edu.avc.oficinas.service.CancelamentoService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rota HTTP de cancelamento: PATCH /inscricoes/{id}/cancelamento. */
@RestController
@RequestMapping("/inscricoes")
public class CancelamentoController {

    private final CancelamentoService service;

    public CancelamentoController(CancelamentoService service) {
        this.service = service;
    }

    @PatchMapping("/{id}/cancelamento")
    public Inscricao cancelar(@PathVariable long id) {
        return service.cancelar(id);
    }
}
