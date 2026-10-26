package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.model.Participante;
import br.edu.avc.oficinas.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;

    public ParticipanteService(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }

    public Participante cadastrar(Participante participante) {
        validar(participante);
        participante.setId(null);
        return participanteRepository.salvar(participante);
    }

    public List<Participante> listarTodos() {
        return participanteRepository.listarTodos();
    }

    public Participante buscarPorId(Long id) {
        validarId(id);
        return participanteRepository.buscarPorId(id);
    }

    private void validar(Participante participante) {
        if (participante == null) {
            throw new DadosInvalidosException(
                    "Os dados do participante são obrigatórios.");
        }

        if (participante.getNome() == null
                || participante.getNome().isBlank()) {
            throw new DadosInvalidosException(
                    "O nome do participante é obrigatório.");
        }

        if (participante.getEmail() == null
                || participante.getEmail().isBlank()
                || !participante.getEmail().contains("@")) {
            throw new DadosInvalidosException(
                    "Informe um e-mail válido.");
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new DadosInvalidosException(
                    "O ID do participante deve ser um número positivo.");
        }
    }
}
