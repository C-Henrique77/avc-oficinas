package br.edu.avc.oficinas.dto;

//** Corpo das requisiçoes POST /oficinas e PUT /oficinas{id}. */
public record OficinasRequest(String titulo, Interger capacidade) {
  }
