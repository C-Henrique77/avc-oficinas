package br.edu.avc.oficinas.controller;

import br.edu.avc.oficinas.model.Inscricao;
import br.edu.avc.oficinas.service.InscricaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inscricoes")
public class InscricaoController {

    private final InscricaoService inscricaoService;

    public InscricaoController(InscricaoService inscricaoService) {
        this.inscricaoService = inscricaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inscricao inscrever(@RequestBody NovaInscricao dados) {
        return inscricaoService.inscrever(
                dados.getParticipanteId(),
                dados.getOficinaId()
        );
    }

    @GetMapping
    public List<Inscricao> listarTodas() {
        return inscricaoService.listarTodas();
    }

    public static class NovaInscricao {

        private Long participanteId;
        private Long oficinaId;

        public Long getParticipanteId() {
            return participanteId;
        }

        public void setParticipanteId(Long participanteId) {
            this.participanteId = participanteId;
        }

        public Long getOficinaId() {
            return oficinaId;
        }

        public void setOficinaId(Long oficinaId) {
            this.oficinaId = oficinaId;
        }
    }
}
