package br.edu.avc.oficinas.dto;

/** Corpo das requisicoes POST /oficinas e PUT /oficinas/{id}. */
public record OficinaRequest(String titulo, Integer capacidade) {
}
