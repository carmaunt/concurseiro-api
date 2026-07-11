package br.com.concurseiro.api.conteudo.controller;

import br.com.concurseiro.api.conteudo.dto.ImagemCapaUploadResponse;
import br.com.concurseiro.api.infra.storage.R2ConfiguredCondition;
import br.com.concurseiro.api.questoes.textoapoio.service.R2ImagemStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Conteúdos do portal", description = "Gestão editorial do portal público")
@RestController
@RequestMapping("/api/v1/admin/conteudos/imagens")
@Conditional(R2ConfiguredCondition.class)
public class AdminConteudoImagemController {

    private final R2ImagemStorageService storageService;

    public AdminConteudoImagemController(R2ImagemStorageService storageService) {
        this.storageService = storageService;
    }

    @Operation(summary = "Enviar imagem de capa de conteúdo")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemCapaUploadResponse enviar(@RequestParam("arquivo") MultipartFile arquivo) {
        return ImagemCapaUploadResponse.from(storageService.enviar(arquivo, "conteudos/capas"));
    }
}
