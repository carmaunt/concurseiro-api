package br.com.concurseiro.api.conteudo.dto;

import br.com.concurseiro.api.questoes.textoapoio.service.R2ImagemStorageService;

public record ImagemCapaUploadResponse(
        String url,
        String contentType,
        Integer largura,
        Integer altura
) {
    public static ImagemCapaUploadResponse from(R2ImagemStorageService.ImagemArmazenada imagem) {
        return new ImagemCapaUploadResponse(
                imagem.url(),
                imagem.contentType(),
                imagem.largura(),
                imagem.altura()
        );
    }
}
