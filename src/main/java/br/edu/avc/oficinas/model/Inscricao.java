package br.edu.avc.oficinas.model;

import java.time.LocalDateTime;

public class Inscricao {

    private Long id;
    private Long participanteId;
    private Long oficinaId;
    private LocalDateTime dataRegistro;
    private String situacao;

    public Inscricao() {
    }

    public Inscricao(Long id, Long participanteId, Long oficinaId,
                     LocalDateTime dataRegistro, String situacao) {
        this.id = id;
        this.participanteId = participanteId;
        this.oficinaId = oficinaId;
        this.dataRegistro = dataRegistro;
        this.situacao = situacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }
}
