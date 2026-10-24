package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.dominio.Inscricao;
import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.dominio.SituacaoInscricao;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;
import br.edu.avc.oficinas.repository.InscricaoRepository;
import br.edu.avc.oficinas.repository.OficinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CancelamentoServiceTest {

    private final ConnectionFactory fabrica = mock(ConnectionFactory.class);
    private final Connection conexao = mock(Connection.class);
    private final OficinaRepository oficinaRepo = mock(OficinaRepository.class);
    private final InscricaoRepository inscricaoRepo = mock(InscricaoRepository.class);
    private CancelamentoService service;

    @BeforeEach
    void preparar() {
        when(fabrica.abrir()).thenReturn(conexao);
        service = new CancelamentoService(fabrica, oficinaRepo, inscricaoRepo);
    }

    private Inscricao inscricao(SituacaoInscricao situacao) {
        return new Inscricao(10L, 1L, 5L, LocalDateTime.now(), situacao);
    }

    @Test
    void cancelarInscricaoAtivaLiberaUmaVaga() throws SQLException {
        Inscricao ativa = inscricao(SituacaoInscricao.ATIVA);
        Oficina oficina = new Oficina(5L, "Java", 10, 3);
        when(inscricaoRepo.buscarPorIdTravando(conexao, 10L)).thenReturn(Optional.of(ativa));
        when(oficinaRepo.buscarPorIdTravando(conexao, 5L)).thenReturn(Optional.of(oficina));

        Inscricao resultado = service.cancelar(10L);

        assertThat(resultado.getSituacao()).isEqualTo(SituacaoInscricao.CANCELADA);
        assertThat(oficina.getVagasDisponiveis()).isEqualTo(4);
        verify(inscricaoRepo).atualizarSituacao(conexao, ativa);
        verify(oficinaRepo).atualizarVagas(conexao, oficina);
        verify(conexao).commit();
    }

    @Test
    void repetirCancelamentoERejeitadoSemNovaAlteracao() throws SQLException {
        Inscricao jaCancelada = inscricao(SituacaoInscricao.CANCELADA);
        Oficina oficina = new Oficina(5L, "Java", 10, 4);
        when(inscricaoRepo.buscarPorIdTravando(conexao, 10L)).thenReturn(Optional.of(jaCancelada));
        when(oficinaRepo.buscarPorIdTravando(conexao, 5L)).thenReturn(Optional.of(oficina));

        assertThatThrownBy(() -> service.cancelar(10L)).isInstanceOf(RegraNegocioException.class);

        assertThat(oficina.getVagasDisponiveis()).isEqualTo(4);
        verify(oficinaRepo, never()).atualizarVagas(any(), any());
        verify(inscricaoRepo, never()).atualizarSituacao(any(), any());
        verify(conexao).rollback();
        verify(conexao, never()).commit();
    }

    @Test
    void cancelamentoNaoPodeElevarVagasAcimaDaCapacidade() throws SQLException {
        Inscricao ativa = inscricao(SituacaoInscricao.ATIVA);
        Oficina semVagasOcupadas = new Oficina(5L, "Java", 10, 10); // dado inconsistente de proposito
        when(inscricaoRepo.buscarPorIdTravando(conexao, 10L)).thenReturn(Optional.of(ativa));
        when(oficinaRepo.buscarPorIdTravando(conexao, 5L)).thenReturn(Optional.of(semVagasOcupadas));

        assertThatThrownBy(() -> service.cancelar(10L)).isInstanceOf(RegraNegocioException.class);

        assertThat(semVagasOcupadas.getVagasDisponiveis()).isEqualTo(10);
        verify(inscricaoRepo, never()).atualizarSituacao(any(), any());
        verify(conexao).rollback();
    }

    @Test
    void cancelarInscricaoInexistenteLancaNaoEncontrado() {
        when(inscricaoRepo.buscarPorIdTravando(conexao, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");

        verify(oficinaRepo, never()).atualizarVagas(any(), any());
    }
}
