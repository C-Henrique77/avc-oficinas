package br.edu.avc.oficinas.service;

import br.edu.avc.oficinas.excecao.DadosInvalidosException;
import br.edu.avc.oficinas.model.Oficina;
import br.edu.avc.oficinas.repository.OficinaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OficinaService {

    private final OficinaRepository oficinaRepository;

    public OficinaService(OficinaRepository oficinaRepository) {
        this.oficinaRepository = oficinaRepository;
    }

    public Oficina cadastrar(Oficina oficina) {
        validar(oficina);
        oficina.setId(null);
        return oficinaRepository.salvar(oficina);
    }

    public List<Oficina> listarTodas() {
        return oficinaRepository.listarTodas();
    }

    public Oficina buscarPorId(Long id) {
        validarId(id);
        return oficinaRepository.buscarPorId(id);
    }

    public Oficina atualizar(Long id, Oficina oficina) {
        validarId(id);
        validar(oficina);
        return oficinaRepository.atualizar(id, oficina);
    }

    public void excluir(Long id) {
        validarId(id);
        oficinaRepository.excluir(id);
    }

    private void validar(Oficina oficina) {
        if (oficina == null) {
            throw new DadosInvalidosException(
                    "Os dados da oficina são obrigatórios.");
        }

        if (oficina.getTitulo() == null || oficina.getTitulo().isBlank()) {
            throw new DadosInvalidosException(
                    "O título da oficina é obrigatório.");
        }

        if (oficina.getCapacidade() <= 0) {
            throw new DadosInvalidosException(
                    "A capacidade deve ser maior que zero.");
        }

        if (oficina.getVagasDisponiveis() < 0
                || oficina.getVagasDisponiveis() > oficina.getCapacidade()) {
            throw new DadosInvalidosException(
                    "As vagas disponíveis devem estar entre zero e a capacidade.");
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new DadosInvalidosException(
                    "O ID da oficina deve ser um número positivo.");
        }
    }
}
