
package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.service.OficinaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/oficinas")
public class OficinaController {

    private final OficinaService service;

    public OficinaController(OficinaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Oficina> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Oficina buscar(@PathVariable long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Oficina> cadastrar(
            @RequestBody OficinaRequest request) {

        Oficina oficina = service.cadastrar(
                request.titulo(), request.capacidade());

        return ResponseEntity.status(HttpStatus.CREATED).body(oficina);
    }

    @PutMapping("/{id}")
    public Oficina atualizar(
            @PathVariable long id,
            @RequestBody OficinaRequest request) {

        return service.atualizar(
                id, request.titulo(), request.capacidade());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    public record OficinaRequest(String titulo, Integer capacidade) {
    }
}
