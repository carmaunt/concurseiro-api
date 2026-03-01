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
        return repository.findAllByOrderByNomeAsc()
                .stream()
                .map(i -> new CatalogoItemResponse(i.getId(), i.getNome()))
                .toList();
    }

    public CatalogoItemResponse atualizar(Long id, String nomeBruto) {
        Instituicao inst = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada"));
        String nome = nomeBruto.trim();
        if (repository.existsByNomeIgnoreCase(nome) && !inst.getNome().equalsIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Instituição já cadastrada");
        }
        inst.setNome(nome);
        Instituicao salva = repository.save(inst);
        return new CatalogoItemResponse(salva.getId(), salva.getNome());
    }

    public void excluir(Long id) {
        Instituicao inst = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada"));
        repository.delete(inst);
    }
}