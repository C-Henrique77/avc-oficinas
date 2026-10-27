package br.edu.avc.oficinas.dominio;

import br.edu.avc.oficinas.excecao.RegraNegocioException;

import java.time.LocalDateTime;

/**
 * Inscricao de um participante em uma oficina. Funciona tambem como historico:
 * ao cancelar, o registro nao e apagado, apenas muda a situacao.
 * A ligacao com Participante e Oficina e feita pelos identificadores (chaves estrangeiras).
 */
public class Inscricao {

    private final Long id;
    private final Long participanteId;
    private final Long oficinaId;
    private final LocalDateTime dataRegistro;
    private SituacaoInscricao situacao;

    public Inscricao(Long id, Long participanteId, Long oficinaId,
                     LocalDateTime dataRegistro, SituacaoInscricao situacao) {
        this.id = id;
        this.participanteId = participanteId;
        this.oficinaId = oficinaId;
        this.dataRegistro = dataRegistro;
        this.situacao = situacao;
    }

    /** Nova inscricao: nasce ATIVA, com a data/hora atual e ainda sem id. */
    public static Inscricao nova(Long participanteId, Long oficinaId) {
        return new Inscricao(null, participanteId, oficinaId, LocalDateTime.now(), SituacaoInscricao.ATIVA);
    }

    public boolean estaAtiva() {
        return situacao == SituacaoInscricao.ATIVA;
    }

    /** Cancela a inscricao. Nao pode ser repetido. */
    public void cancelar() {
        if (!estaAtiva()) {
            throw new RegraNegocioException("Inscrição já está cancelada");
        }
        this.situacao = SituacaoInscricao.CANCELADA;
    }

    public Long getId() {
        return id;
    }

    public Long getParticipanteId() {
        return participanteId;
    }

    public Long getOficinaId() {
        return oficinaId;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public SituacaoInscricao getSituacao() {
        return situacao;
    }
}
