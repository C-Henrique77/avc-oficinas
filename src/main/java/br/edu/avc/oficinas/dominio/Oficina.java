package br.edu.avc.oficinas.dominio;

import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;

/**
 * Oficina com vagas limitadas.
 * Invariante: 0 <= vagasDisponiveis <= capacidade.
 * Os atributos sao privados e so mudam pelos metodos abaixo, que protegem essa invariante.
 */
public class Oficina {

    private final Long id;
    private String titulo;
    private int capacidade;
    private int vagasDisponiveis;

    public Oficina(Long id, String titulo, int capacidade, int vagasDisponiveis) {
        validarTitulo(titulo);
        validarCapacidade(capacidade);
        if (vagasDisponiveis < 0 || vagasDisponiveis > capacidade) {
            throw new DadosInvalidosException("Vagas disponíveis deve estar entre 0 e a capacidade");
        }
        this.id = id;
        this.titulo = titulo.trim();
        this.capacidade = capacidade;
        this.vagasDisponiveis = vagasDisponiveis;
    }

    /** Oficina recem-criada: ainda sem id e com todas as vagas livres. */
    public static Oficina nova(String titulo, int capacidade) {
        return new Oficina(null, titulo, capacidade, capacidade);
    }

    public boolean temVaga() {
        return vagasDisponiveis > 0;
    }

    /** Consome uma vaga (usado ao registrar uma inscricao). */
    public void reservarVaga() {
        if (!temVaga()) {
            throw new RegraNegocioException("Oficina lotada: não há vagas disponíveis");
        }
        vagasDisponiveis--;
    }

    /** Devolve uma vaga (usado ao cancelar). Nunca ultrapassa a capacidade. */
    public void devolverVaga() {
        if (vagasDisponiveis >= capacidade) {
            throw new RegraNegocioException("Cancelamento inválido: as vagas já estão no limite da capacidade");
        }
        vagasDisponiveis++;
    }

    /** Edicao de campo descritivo (sempre permitida). */
    public void atualizarTitulo(String novoTitulo) {
        validarTitulo(novoTitulo);
        this.titulo = novoTitulo.trim();
    }

    /**
     * Redefine a capacidade. So deve ser chamado quando NAO ha historico de inscricoes
     * (o Service garante isso); nesse caso nenhuma vaga esta ocupada.
     */
    public void redefinirCapacidade(int novaCapacidade) {
        validarCapacidade(novaCapacidade);
        this.capacidade = novaCapacidade;
        this.vagasDisponiveis = novaCapacidade;
    }

    private static void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new DadosInvalidosException("Título é obrigatório");
        }
        if (titulo.trim().length() > 160) {
            throw new DadosInvalidosException("Título deve ter no máximo 160 caracteres");
        }
    }

    private static void validarCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new DadosInvalidosException("Capacidade deve ser maior que zero");
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }
}
