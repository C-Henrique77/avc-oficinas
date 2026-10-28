package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.dto.OficinaRequest;
import br.edu.avc.oficinas.service.OficinaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/oficinas")
public class OficinaController {

    private final OficinaService service;

    public OficinaController(OficinaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Oficina> criar(
            @RequestBody OficinaRequest request) {

        Oficina oficina = service.cadastrar(
                request.titulo(),
                request.capacidade()
        );

        return ResponseEntity
                .created(URI.create("/oficinas/" + oficina.getId()))
                .body(oficina);
    }

    @GetMapping
    public ResponseEntity<List<Oficina>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Oficina> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Oficina> atualizar(
            @PathVariable Long id,
            @RequestBody OficinaRequest request) {

        Oficina oficina = service.atualizar(
                id,
                request.titulo(),
                request.capacidade()
        );

        return ResponseEntity.ok(oficina);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        service.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
