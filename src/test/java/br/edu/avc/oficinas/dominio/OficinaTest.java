package br.edu.avc.oficinas.dominio;

import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OficinaTest {

    @Test
    void reservarVagaReduzUmaVaga() {
        Oficina oficina = new Oficina(1L, "Java", 10, 3);

        oficina.reservarVaga();

        assertThat(oficina.getVagasDisponiveis()).isEqualTo(2);
    }

    @Test
    void reservarVagaEmOficinaLotadaLancaExcecaoEMantemVagas() {
        Oficina lotada = new Oficina(1L, "Java", 10, 0);

        assertThatThrownBy(lotada::reservarVaga)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("lotada");
        assertThat(lotada.getVagasDisponiveis()).isZero();
    }

    @Test
    void devolverVagaNaoUltrapassaACapacidade() {
        Oficina cheia = new Oficina(1L, "Java", 10, 10);

        assertThatThrownBy(cheia::devolverVaga).isInstanceOf(RegraNegocioException.class);
        assertThat(cheia.getVagasDisponiveis()).isEqualTo(10);
    }

    @Test
    void novaOficinaComCapacidadeInvalidaLancaDadosInvalidos() {
        assertThatThrownBy(() -> Oficina.nova("Java", 0)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> Oficina.nova("Java", -5)).isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void novaOficinaSemTituloLancaDadosInvalidos() {
        assertThatThrownBy(() -> Oficina.nova("  ", 10)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> Oficina.nova(null, 10)).isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void novaOficinaComecaComTodasAsVagasLivres() {
        Oficina nova = Oficina.nova("Git", 15);

        assertThat(nova.getVagasDisponiveis()).isEqualTo(nova.getCapacidade()).isEqualTo(15);
    }
}
