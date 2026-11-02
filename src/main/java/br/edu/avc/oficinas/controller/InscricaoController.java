package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.dominio.Inscricao;
import br.edu.avc.oficinas.dto.InscricaoRequest;
import br.edu.avc.oficinas.service.InscricaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Rotas HTTP de inscricoes (registrar e listar o historico). */
@RestController
@RequestMapping("/inscricoes")
public class InscricaoController {

    private final InscricaoService service;

    public InscricaoController(InscricaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Inscricao> registrar(@RequestBody InscricaoRequest requisicao) {
        Inscricao criada = service.registrar(requisicao.participanteId(), requisicao.oficinaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public List<Inscricao> listar() {
        return service.listar();
    }
}
