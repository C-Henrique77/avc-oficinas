package br.edu.avc.oficinas.model;

public class Oficina {

    private Long id;
    private String titulo;
    private int capacidade;
    private int vagasDisponiveis;

    public Oficina() {
    }

    public Oficina(Long id, String titulo, int capacidade, int vagasDisponiveis) {
        this.id = id;
        this.titulo = titulo;
        this.capacidade = capacidade;
        this.vagasDisponiveis = vagasDisponiveis;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }
}
