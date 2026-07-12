package br.com.concurseiro.api.conteudo.event;

public record ConteudoPublicoAlteradoEvent(Long conteudoId, String operacao) {
}
