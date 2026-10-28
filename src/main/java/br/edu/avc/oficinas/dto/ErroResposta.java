package br.edu.avc.oficinas.dto;

//** Formato padrao das respostas de erro da API. */
public record ErroResposta(int status, String erro, String mensagem){
}
