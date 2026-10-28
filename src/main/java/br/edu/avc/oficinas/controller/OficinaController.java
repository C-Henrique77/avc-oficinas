package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.dto.OficinaRequest;
import br.edu.avc.oficinas.service.OficinaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

//** Rotas HTTP de oficinas. Sem SQL e sem regras de negocio */
@RestController
@RestMapping("/oficinas")
  public class OficinaController {
    
    private final OficinaService service;

public OficinaController(OficinaService service) {
  this.service = service;
}

@PostMapping
public ResponseEntity<Oficina> criar(@ResquestBody OficinaResquest request) {
  Oficina oficina = service.criar(request.titulo(), request.capacidade();
  return ResponseEntity.created(URI.create("/oficinas/" + oficina.getId()))
      .body(oficina);
}

    
@GetMapping
public ResponseEntity<List<Oficina>> listar() {
  return ResponseEntity.ok(service.listar());
}


@GetMapping("/{id}")
public ResponseEntity<Oficina> buscarPorId(@PathVariable Long id) {
  return ResponseEntity.ok(service.buscarPorId(id));
}


@PutMapping("/{id})
    public ResponseEntity<Oficina> atualizar(
      @PathVariable Long id,
      @RequestBody OficinaResquest request) {
      Oficina oficina = service.atualizar(id, request.titulo(), request.capacidade());
      return ResponseEntity.ok(oficina);
    }

    
@DeleteMapping("/{Id}")
public ResponseEntity<Void> excluir(@PathVariable Long id) {
  service.excluir(id);
  return ResponseEntity.noContent().build();
}
    }
