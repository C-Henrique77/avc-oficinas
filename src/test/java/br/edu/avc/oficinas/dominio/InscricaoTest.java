package br.edu.avc.oficinas.dominio;

import br.edu.avc.oficinas.excecao.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InscricaoTest {

    @Test
    void novaInscricaoNasceAtiva() {
        Inscricao inscricao = Inscricao.nova(1L, 2L);

        assertThat(inscricao.estaAtiva()).isTrue();
        assertThat(inscricao.getSituacao()).isEqualTo(SituacaoInscricao.ATIVA);
        assertThat(inscricao.getDataRegistro()).isNotNull();
    }

    @Test
    void cancelarMudaASituacaoParaCancelada() {
        Inscricao inscricao = Inscricao.nova(1L, 2L);

        inscricao.cancelar();

        assertThat(inscricao.getSituacao()).isEqualTo(SituacaoInscricao.CANCELADA);
    }

    @Test
    void cancelarDuasVezesLancaExcecao() {
        Inscricao inscricao = Inscricao.nova(1L, 2L);
        inscricao.cancelar();

        assertThatThrownBy(inscricao::cancelar).isInstanceOf(RegraNegocioException.class);
    }
}
