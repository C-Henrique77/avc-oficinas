package br.edu.avc.oficinas.algoritmos;

import br.edu.avc.oficinas.dominio.Oficina;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Modulo de algoritmos (independente da API): busca linear, busca binaria e ordenacao
 * sobre uma colecao EM MEMORIA de oficinas.
 *
 * Complexidades (n = quantidade de oficinas):
 *   - Busca linear por ID ......... O(n)      (percorre ate achar; melhor caso O(1))
 *   - Busca binaria por ID ........ O(log n)  (exige lista ORDENADA pelo mesmo criterio: o ID)
 *   - Ordenacao por insercao ...... O(n^2) no pior caso e no caso medio; O(n) se ja ordenada
 */
public final class AlgoritmosOficina {

    private AlgoritmosOficina() {
    }

    /** @return indice da oficina com o id, ou -1 se nao existir. Funciona com a lista em qualquer ordem. */
    public static int buscaLinearPorId(List<Oficina> oficinas, long id) {
        for (int i = 0; i < oficinas.size(); i++) {
            if (oficinas.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param oficinasOrdenadasPorId lista JA ordenada de forma crescente pelo id.
     *        Se estiver desordenada, o resultado pode estar errado (a busca descarta metades
     *        da lista assumindo a ordem).
     * @return indice da oficina com o id, ou -1 se nao existir.
     */
    public static int buscaBinariaPorId(List<Oficina> oficinasOrdenadasPorId, long id) {
        int inicio = 0;
        int fim = oficinasOrdenadasPorId.size() - 1;
        while (inicio <= fim) {
            int meio = (inicio + fim) / 2;
            long idDoMeio = oficinasOrdenadasPorId.get(meio).getId();
            if (idDoMeio == id) {
                return meio;
            } else if (idDoMeio < id) {
                inicio = meio + 1; // descarta a metade esquerda
            } else {
                fim = meio - 1;    // descarta a metade direita
            }
        }
        return -1;
    }

    /** Ordena por id crescente (pre-requisito da busca binaria por id). */
    public static void ordenarPorId(List<Oficina> oficinas) {
        ordenarPorInsercao(oficinas, Comparator.comparingLong(Oficina::getId));
    }

    /** Criterio relevante: oficinas com MAIS vagas disponiveis primeiro. */
    public static void ordenarPorVagasDisponiveis(List<Oficina> oficinas) {
        ordenarPorInsercao(oficinas, Comparator.comparingInt(Oficina::getVagasDisponiveis).reversed());
    }

    /**
     * Ordenacao por insercao (insertion sort): para cada elemento, desloca os maiores
     * uma posicao a direita e o encaixa no lugar certo. E estavel.
     */
    static void ordenarPorInsercao(List<Oficina> oficinas, Comparator<Oficina> criterio) {
        for (int i = 1; i < oficinas.size(); i++) {
            Oficina atual = oficinas.get(i);
            int j = i - 1;
            while (j >= 0 && criterio.compare(oficinas.get(j), atual) > 0) {
                oficinas.set(j + 1, oficinas.get(j));
                j--;
            }
            oficinas.set(j + 1, atual);
        }
    }

    /**
     * Gera uma colecao simples e REPRODUTIVEL de oficinas (ids 1..quantidade) para demonstrar
     * os algoritmos. A semente fixa (42) embaralha sempre da mesma forma, entao a lista sai
     * DESORDENADA de proposito: o passo de ordenar antes da busca binaria faz diferenca.
     */
    public static List<Oficina> gerarOficinasDeExemplo(int quantidade) {
        List<Oficina> oficinas = new ArrayList<>();
        for (int id = 1; id <= quantidade; id++) {
            int capacidade = 10 + (id * 3) % 15;
            int vagas = (id * 5) % (capacidade + 1);
            oficinas.add(new Oficina((long) id, "Oficina " + id, capacidade, vagas));
        }
        Collections.shuffle(oficinas, new Random(42));
        return oficinas;
    }
}
