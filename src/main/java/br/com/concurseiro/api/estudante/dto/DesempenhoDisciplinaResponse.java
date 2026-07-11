package br.com.concurseiro.api.estudante.dto;

public record DesempenhoDisciplinaResponse(
        String disciplina,
        long total,
        long acertos,
        long erros,
        double aproveitamento
) {
    public DesempenhoDisciplinaResponse(String disciplina, long total, long acertos) {
        this(
                disciplina,
                total,
                acertos,
                Math.max(total - acertos, 0),
                total == 0 ? 0 : Math.round((acertos * 1000.0 / total)) / 10.0
        );
    }
}
