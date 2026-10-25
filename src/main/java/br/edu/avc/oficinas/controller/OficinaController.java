package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.model.Oficina;
import br.edu.avc.oficinas.service.OficinaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/oficinas")
public class OficinaController {

    private final OficinaService oficinaService;

    public OficinaController(OficinaService oficinaService) {
        this.oficinaService = oficinaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Oficina cadastrar(@RequestBody Oficina oficina) {
        return oficinaService.cadastrar(oficina);
    }

    @GetMapping
    public List<Oficina> listarTodas() {
        return oficinaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Oficina buscarPorId(@PathVariable Long id) {
        return oficinaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Oficina atualizar(
            @PathVariable Long id,
            @RequestBody Oficina oficina) {
        return oficinaService.atualizar(id, oficina);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        oficinaService.excluir(id);
    }
}
