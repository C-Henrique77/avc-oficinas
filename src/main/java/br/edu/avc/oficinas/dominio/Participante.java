package br.edu.avc.oficinas.dominio;

/** Participante: somente consultado nesta AVC (os dados vem prontos no banco). */
public class Participante {

    private final Long id;
    private final String nome;
    private final String email;

    public Participante(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
