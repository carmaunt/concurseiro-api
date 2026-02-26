package br.com.concurseiro.api.catalogo.instituicao.service;

import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import br.com.concurseiro.api.catalogo.instituicao.repository.InstituicaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InstituicaoService {

    private final InstituicaoRepository repository;

    public InstituicaoService(InstituicaoRepository repository) {
        this.repository = repository;
    }

    public CatalogoItemResponse cadastrar(String nomeBruto) {

        String nome = nomeBruto.trim();

        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Instituição já cadastrada"
            );
        }

        Instituicao inst = new Instituicao();
        inst.setNome(nome);

        Instituicao salva = repository.save(inst);

        return new CatalogoItemResponse(salva.getId(), salva.getNome());
    }

    public List<CatalogoItemResponse> listar() {
        return repository.findAll()
                .stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(i -> new CatalogoItemResponse(i.getId(), i.getNome()))
                .toList();
    }
}