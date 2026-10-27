package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.dominio.Oficina;
import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;
import br.edu.avc.oficinas.repository.InscricaoRepository;
import br.edu.avc.oficinas.repository.OficinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OficinaServiceTest {

    private final ConnectionFactory fabrica = mock(ConnectionFactory.class);
    private final Connection conexao = mock(Connection.class);
    private final OficinaRepository oficinaRepo = mock(OficinaRepository.class);
    private final InscricaoRepository inscricaoRepo = mock(InscricaoRepository.class);
    private OficinaService service;

    @BeforeEach
    void preparar() {
        when(fabrica.abrir()).thenReturn(conexao);
        service = new OficinaService(fabrica, oficinaRepo, inscricaoRepo);
    }

    @Test
    void alterarCapacidadeComHistoricoERejeitadoEFazRollback() throws SQLException {
        when(oficinaRepo.buscarPorIdTravando(conexao, 1L)).thenReturn(Optional.of(new Oficina(1L, "Java", 10, 8)));
        when(inscricaoRepo.contarPorOficina(conexao, 1L)).thenReturn(2);

        assertThatThrownBy(() -> service.atualizar(1L, "Java", 20))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("capacidade");

        verify(oficinaRepo, never()).atualizar(any(), any());
        verify(conexao).rollback();
        verify(conexao, never()).commit();
    }

    @Test
    void alterarSoOTituloComHistoricoEPermitido() throws SQLException {
        when(oficinaRepo.buscarPorIdTravando(conexao, 1L)).thenReturn(Optional.of(new Oficina(1L, "Java", 10, 8)));
        when(inscricaoRepo.contarPorOficina(conexao, 1L)).thenReturn(2);

        Oficina atualizada = service.atualizar(1L, "Java Avançado", 10);

        assertThat(atualizada.getTitulo()).isEqualTo("Java Avançado");
        assertThat(atualizada.getCapacidade()).isEqualTo(10);
        assertThat(atualizada.getVagasDisponiveis()).isEqualTo(8); // vagas ocupadas preservadas
        verify(oficinaRepo).atualizar(conexao, atualizada);
        verify(conexao).commit();
    }

    @Test
    void alterarCapacidadeSemHistoricoRedefineAsVagas() throws SQLException {
        when(oficinaRepo.buscarPorIdTravando(conexao, 1L)).thenReturn(Optional.of(new Oficina(1L, "Java", 10, 10)));
        when(inscricaoRepo.contarPorOficina(conexao, 1L)).thenReturn(0);

        Oficina atualizada = service.atualizar(1L, "Java", 25);

        assertThat(atualizada.getCapacidade()).isEqualTo(25);
        assertThat(atualizada.getVagasDisponiveis()).isEqualTo(25);
        verify(conexao).commit();
    }

    @Test
    void excluirOficinaComHistoricoERejeitado() throws SQLException {
        when(oficinaRepo.buscarPorIdTravando(conexao, 1L)).thenReturn(Optional.of(new Oficina(1L, "Java", 10, 9)));
        when(inscricaoRepo.contarPorOficina(conexao, 1L)).thenReturn(1);

        assertThatThrownBy(() -> service.excluir(1L)).isInstanceOf(RegraNegocioException.class);

        verify(oficinaRepo, never()).excluir(any(), anyLong());
        verify(conexao).rollback();
    }

    @Test
    void excluirOficinaSemHistoricoRemoveEFazCommit() throws SQLException {
        when(oficinaRepo.buscarPorIdTravando(conexao, 1L)).thenReturn(Optional.of(new Oficina(1L, "Java", 10, 10)));
        when(inscricaoRepo.contarPorOficina(conexao, 1L)).thenReturn(0);

        service.excluir(1L);

        verify(oficinaRepo).excluir(conexao, 1L);
        verify(conexao).commit();
    }

    @Test
    void excluirOficinaInexistenteLancaNaoEncontrado() {
        when(oficinaRepo.buscarPorIdTravando(conexao, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void cadastrarSemTituloOuSemCapacidadeLancaDadosInvalidos() {
        assertThatThrownBy(() -> service.cadastrar("", 10)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.cadastrar("Java", null)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.cadastrar("Java", 0)).isInstanceOf(DadosInvalidosException.class);
    }
}
