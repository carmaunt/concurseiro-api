package br.com.concurseiro.api.questoes.textoapoio.controller;

import br.com.concurseiro.api.infra.storage.R2ConfiguredCondition;
import br.com.concurseiro.api.questoes.textoapoio.dto.TextoApoioResponse;
import br.com.concurseiro.api.questoes.textoapoio.service.TextoApoioImagemService;
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

@Tag(name = "Textos de apoio", description = "Cadastro e reutilização de textos base para questões")
@RestController
@RequestMapping("/api/v1/admin/textos-apoio/imagens")
@Conditional(R2ConfiguredCondition.class)
public class TextoApoioImagemAdminController {

    private final TextoApoioImagemService service;

    public TextoApoioImagemAdminController(TextoApoioImagemService service) {
        this.service = service;
    }

    @Operation(summary = "Enviar imagem e cadastrar um texto de apoio")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TextoApoioResponse cadastrar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam("textoAlternativo") String textoAlternativo
    ) {
        return service.cadastrar(arquivo, titulo, textoAlternativo);
    }
}
