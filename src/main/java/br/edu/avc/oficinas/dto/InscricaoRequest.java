package br.edu.avc.oficinas.dto;

/** Corpo da requisicao POST /inscricoes. */
public record InscricaoRequest(Long participanteId, Long oficinaId) {
}
