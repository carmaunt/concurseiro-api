package br.com.concurseiro.api.prova.dto;

import br.com.concurseiro.api.prova.model.Prova;
import java.time.OffsetDateTime;

public record ProvaResponse(
        Long id,
        String banca,
        String instituicao,
        Long instituicaoId,
        Integer ano,
        String cargo,
        String nivel,
        String modalidade,
        Long totalQuestoes,
        OffsetDateTime criadoEm
) {
    public static ProvaResponse fromEntity(Prova p, Long totalQuestoes) {
        return new ProvaResponse(
                p.getId(),
                p.getBanca(),
                p.getInstituicao(),
                p.getInstituicaoId(),
                p.getAno(),
                p.getCargo(),
                p.getNivel(),
                p.getModalidade(),
                totalQuestoes,
                p.getCriadoEm()
        );
    }
}
