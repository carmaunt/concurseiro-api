package br.com.concurseiro.api.estudante.dto;

import java.util.List;

public record DashboardEstudanteResponse(
        long questoesResolvidas,
        long acertos,
        long erros,
        double aproveitamento,
        List<UltimaRespostaResponse> ultimasRespostas,
        List<DesempenhoDisciplinaResponse> desempenhoPorDisciplina
) {}
