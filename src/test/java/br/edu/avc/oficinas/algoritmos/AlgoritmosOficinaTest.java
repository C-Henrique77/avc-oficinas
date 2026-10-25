package br.edu.avc.oficinas.algoritmos;

import br.edu.avc.oficinas.dominio.Oficina;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlgoritmosOficinaTest {

    private static Oficina oficina(long id, int vagas) {
        return new Oficina(id, "Oficina " + id, 20, vagas);
    }

    private static List<Oficina> desordenada() {
        return new ArrayList<>(List.of(oficina(5, 2), oficina(1, 9), oficina(3, 0), oficina(4, 7), oficina(2, 4)));
    }

    @Test
    void buscaLinearEncontraEmListaDesordenada() {
        List<Oficina> lista = desordenada();

        assertThat(AlgoritmosOficina.buscaLinearPorId(lista, 3)).isEqualTo(2);
        assertThat(AlgoritmosOficina.buscaLinearPorId(lista, 5)).isZero();
    }

    @Test
    void buscaLinearDevolveMenosUmQuandoNaoExiste() {
        assertThat(AlgoritmosOficina.buscaLinearPorId(desordenada(), 99)).isEqualTo(-1);
    }

    @Test
    void ordenarPorIdDeixaAListaEmOrdemCrescente() {
        List<Oficina> lista = desordenada();

        AlgoritmosOficina.ordenarPorId(lista);

        assertThat(lista).extracting(Oficina::getId).containsExactly(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    void buscaBinariaEncontraEmListaOrdenadaPorId() {
        List<Oficina> lista = desordenada();
        AlgoritmosOficina.ordenarPorId(lista);

        for (long id = 1; id <= 5; id++) {
            int indice = AlgoritmosOficina.buscaBinariaPorId(lista, id);
            assertThat(lista.get(indice).getId()).isEqualTo(id);
        }
    }

    @Test
    void buscaBinariaDevolveMenosUmQuandoNaoExiste() {
        List<Oficina> lista = desordenada();
        AlgoritmosOficina.ordenarPorId(lista);

        assertThat(AlgoritmosOficina.buscaBinariaPorId(lista, 99)).isEqualTo(-1);
        assertThat(AlgoritmosOficina.buscaBinariaPorId(new ArrayList<>(), 1)).isEqualTo(-1);
    }

    @Test
    void buscaBinariaExigeListaOrdenada_SemOrdenarPodeNaoAcharQuemExiste() {
        List<Oficina> lista = new ArrayList<>(List.of(oficina(5, 1), oficina(1, 1), oficina(3, 1)));

        // O id 5 existe (indice 0), mas a binaria assume ordem crescente e descarta a metade errada.
        assertThat(AlgoritmosOficina.buscaLinearPorId(lista, 5)).isZero();
        assertThat(AlgoritmosOficina.buscaBinariaPorId(lista, 5)).isEqualTo(-1);
    }

    @Test
    void ordenarPorVagasColocaAsComMaisVagasPrimeiro() {
        List<Oficina> lista = desordenada();

        AlgoritmosOficina.ordenarPorVagasDisponiveis(lista);

        assertThat(lista).extracting(Oficina::getVagasDisponiveis).containsExactly(9, 7, 4, 2, 0);
    }

    @Test
    void geradorDeExemploEReproduzivelEDesordenado() {
        List<Oficina> primeira = AlgoritmosOficina.gerarOficinasDeExemplo(10);
        List<Oficina> segunda = AlgoritmosOficina.gerarOficinasDeExemplo(10);

        assertThat(primeira).extracting(Oficina::getId).containsExactlyElementsOf(
                segunda.stream().map(Oficina::getId).toList());
        assertThat(primeira).extracting(Oficina::getId)
                .isNotEqualTo(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
    }
}
