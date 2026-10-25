package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.dominio.Inscricao;
import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.dominio.Participante;
import br.edu.avc.oficinas.dominio.SituacaoInscricao;
import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.excecao.DuplicidadeException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;
import br.edu.avc.oficinas.repository.InscricaoRepository;
import br.edu.avc.oficinas.repository.OficinaRepository;
import br.edu.avc.oficinas.repository.ParticipanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InscricaoServiceTest {

    private final ConnectionFactory fabrica = mock(ConnectionFactory.class);
    private final Connection conexao = mock(Connection.class);
    private final ParticipanteRepository participanteRepo = mock(ParticipanteRepository.class);
    private final OficinaRepository oficinaRepo = mock(OficinaRepository.class);
    private final InscricaoRepository inscricaoRepo = mock(InscricaoRepository.class);
    private InscricaoService service;

    @BeforeEach
    void preparar() {
        when(fabrica.abrir()).thenReturn(conexao);
        service = new InscricaoService(fabrica, participanteRepo, oficinaRepo, inscricaoRepo);
        when(participanteRepo.buscarPorId(conexao, 1L))
                .thenReturn(Optional.of(new Participante(1L, "Ana", "ana@exemplo.com")));
        when(inscricaoRepo.inserir(eq(conexao), any(Inscricao.class))).thenAnswer(invocacao -> {
            Inscricao nova = invocacao.getArgument(1);
            return new Inscricao(10L, nova.getParticipanteId(), nova.getOficinaId(),
                    nova.getDataRegistro(), nova.getSituacao());
        });
    }

    @Test
    void inscreverComVagaDisponivelCriaInscricaoEReduzUmaVaga() throws SQLException {
        Oficina oficina = new Oficina(5L, "Java", 10, 3);
        when(oficinaRepo.buscarPorIdTravando(conexao, 5L)).thenReturn(Optional.of(oficina));
        when(inscricaoRepo.existeAtiva(conexao, 1L, 5L)).thenReturn(false);

        Inscricao resultado = service.registrar(1L, 5L);

        assertThat(resultado.getId()).isEqualTo(10L);
        assertThat(resultado.getSituacao()).isEqualTo(SituacaoInscricao.ATIVA);
        assertThat(oficina.getVagasDisponiveis()).isEqualTo(2);
        verify(oficinaRepo).atualizarVagas(conexao, oficina);
        verify(conexao).commit();
        verify(conexao, never()).rollback();
    }

    @Test
    void inscreverEmOficinaLotadaRejeitaSemGravarNada() throws SQLException {
        Oficina lotada = new Oficina(5L, "Java", 10, 0);
        when(oficinaRepo.buscarPorIdTravando(conexao, 5L)).thenReturn(Optional.of(lotada));

        assertThatThrownBy(() -> service.registrar(1L, 5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("lotada");

        assertThat(lotada.getVagasDisponiveis()).isZero();
        verify(oficinaRepo, never()).atualizarVagas(any(), any());
        verify(inscricaoRepo, never()).inserir(any(), any());
        verify(conexao).rollback();
        verify(conexao, never()).commit();
    }

    @Test
    void repetirInscricaoAtivaRejeitaDuplicidadeSemConsumirVaga() throws SQLException {
        Oficina oficina = new Oficina(5L, "Java", 10, 3);
        when(oficinaRepo.buscarPorIdTravando(conexao, 5L)).thenReturn(Optional.of(oficina));
        when(inscricaoRepo.existeAtiva(conexao, 1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(1L, 5L)).isInstanceOf(DuplicidadeException.class);

        assertThat(oficina.getVagasDisponiveis()).isEqualTo(3);
        verify(oficinaRepo, never()).atualizarVagas(any(), any());
        verify(inscricaoRepo, never()).inserir(any(), any());
        verify(conexao).rollback();
    }

    @Test
    void participanteInexistenteRejeitaSemAlterarAOficina() throws SQLException {
        when(participanteRepo.buscarPorId(conexao, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(99L, 5L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Participante");

        verify(oficinaRepo, never()).atualizarVagas(any(), any());
        verify(inscricaoRepo, never()).inserir(any(), any());
        verify(conexao).rollback();
    }

    @Test
    void oficinaInexistenteRejeitaSemRegistrarInscricao() {
        when(oficinaRepo.buscarPorIdTravando(conexao, 77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(1L, 77L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Oficina");

        verify(inscricaoRepo, never()).inserir(any(), any());
    }

    @Test
    void semIdsLancaDadosInvalidosSemAbrirConexao() {
        assertThatThrownBy(() -> service.registrar(null, 5L)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.registrar(1L, null)).isInstanceOf(DadosInvalidosException.class);

        verify(fabrica, never()).abrir();
    }
}
