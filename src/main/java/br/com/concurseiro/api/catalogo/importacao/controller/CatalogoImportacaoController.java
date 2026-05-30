package br.com.concurseiro.api.catalogo.importacao.controller;

import br.com.concurseiro.api.catalogo.importacao.dto.ImportarCatalogoResponse;
import br.com.concurseiro.api.catalogo.importacao.service.CatalogoImportacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Importação de Catálogo", description = "Importação administrativa de disciplinas, assuntos e subassuntos")
@RestController
@RequestMapping("/api/v1/admin/catalogo")
public class CatalogoImportacaoController {

    private final CatalogoImportacaoService service;

    public CatalogoImportacaoController(CatalogoImportacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Importar disciplina, assuntos e subassuntos a partir de texto livre")
    @PostMapping(
            value = "/importar-texto",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ImportarCatalogoResponse importarTexto(@RequestBody String texto) {
        return service.importarTexto(texto);
    }
}
