package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.model.Participante;
import br.edu.avc.oficinas.service.ParticipanteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/participantes")
public class ParticipanteController {

    private final ParticipanteService participanteService;

    public ParticipanteController(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Participante cadastrar(@RequestBody Participante participante) {
        return participanteService.cadastrar(participante);
    }

    @GetMapping
    public List<Participante> listarTodos() {
        return participanteService.listarTodos();
    }

    @GetMapping("/{id}")
    public Participante buscarPorId(@PathVariable Long id) {
        return participanteService.buscarPorId(id);
    }
}
