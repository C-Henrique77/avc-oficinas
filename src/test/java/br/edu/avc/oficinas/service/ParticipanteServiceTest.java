package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.config.ConnectionFactory;
import br.edu.avc.oficinas.dominio.Participante;
import br.edu.avc.oficinas.excecao.RecursoNaoEncontradoException;
import br.edu.avc.oficinas.repository.ParticipanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParticipanteServiceTest {

    private final ConnectionFactory fabrica = mock(ConnectionFactory.class);
    private final Connection conexao = mock(Connection.class);
    private final ParticipanteRepository repositorio = mock(ParticipanteRepository.class);
    private ParticipanteService service;

    @BeforeEach
    void preparar() {
        when(fabrica.abrir()).thenReturn(conexao);
        service = new ParticipanteService(fabrica, repositorio);
    }

    @Test
    void buscarParticipanteExistenteDevolveOParticipante() {
        Participante ana = new Participante(1L, "Ana", "ana@exemplo.com");
        when(repositorio.buscarPorId(conexao, 1L)).thenReturn(Optional.of(ana));

        assertThat(service.buscar(1L)).isSameAs(ana);
    }

    @Test
    void buscarParticipanteInexistenteLancaNaoEncontrado() {
        when(repositorio.buscarPorId(conexao, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }
}
