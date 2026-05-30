package br.com.concurseiro.api.catalogo.importacao.dto;

public record ImportarCatalogoResponse(
        Long disciplinaId,
        String disciplina,
        int assuntosProcessados,
        int assuntosCriados,
        int assuntosExistentes,
        int subassuntosProcessados,
        int subassuntosCriados,
        int subassuntosExistentes
) {}
