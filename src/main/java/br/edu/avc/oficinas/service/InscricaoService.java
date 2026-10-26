package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.excecao.RegraNegocioException;
import br.edu.avc.oficinas.model.Inscricao;
import br.edu.avc.oficinas.model.Oficina;
import br.edu.avc.oficinas.repository.InscricaoRepository;
import br.edu.avc.oficinas.repository.OficinaRepository;
import br.edu.avc.oficinas.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final OficinaRepository oficinaRepository;
    private final ParticipanteRepository participanteRepository;

    public InscricaoService(
            InscricaoRepository inscricaoRepository,
            OficinaRepository oficinaRepository,
            ParticipanteRepository participanteRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.oficinaRepository = oficinaRepository;
        this.participanteRepository = participanteRepository;
    }
    public Inscricao inscrever(Long participanteId, Long oficinaId) {
        if (participanteId == null || participanteId <= 0) {
            throw new DadosInvalidosException(
                    "Informe um ID de participante válido.");
        }

        if (oficinaId == null || oficinaId <= 0) {
            throw new DadosInvalidosException(
                    "Informe um ID de oficina válido.");
        }

        participanteRepository.buscarPorId(participanteId);

        Oficina oficina = oficinaRepository.buscarPorId(oficinaId);

        if (inscricaoRepository.existeInscricaoAtiva(
                participanteId, oficinaId)) {
            throw new RegraNegocioException(
                    "O participante já está inscrito nesta oficina.");
        }

        if (oficina.getVagasDisponiveis() <= 0) {
            throw new RegraNegocioException(
                    "Não há vagas disponíveis nesta oficina.");
        }

        oficina.setVagasDisponiveis(
                oficina.getVagasDisponiveis() - 1);

        oficinaRepository.atualizar(oficinaId, oficina);

        Inscricao inscricao = new Inscricao();
        inscricao.setParticipanteId(participanteId);
        inscricao.setOficinaId(oficinaId);
        inscricao.setSituacao("ATIVA");

        return inscricaoRepository.salvar(inscricao);
    }

    public List<Inscricao> listarTodas() {
        return inscricaoRepository.listarTodas();
    }
}
