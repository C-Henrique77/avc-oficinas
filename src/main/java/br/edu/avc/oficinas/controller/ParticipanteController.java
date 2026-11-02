package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.dominio.Participante;
import br.edu.avc.oficinas.service.ParticipanteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Rotas HTTP de participantes. Sem SQL e sem regras: so recebe, chama o Service e responde. */
@RestController
@RequestMapping("/participantes")
public class ParticipanteController {

    private final ParticipanteService service;

    public ParticipanteController(ParticipanteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Participante> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Participante buscar(@PathVariable long id) {
        return service.buscar(id);
    }
}
