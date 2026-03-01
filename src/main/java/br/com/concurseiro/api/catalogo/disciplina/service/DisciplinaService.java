package br.com.concurseiro.api.catalogo.disciplina.service;

import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DisciplinaService {

    private final DisciplinaRepository repository;

    public DisciplinaService(DisciplinaRepository repository) {
        this.repository = repository;
    }

    public CatalogoItemResponse cadastrar(String nomeBruto) {

        String nome = nomeBruto.trim();

        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Disciplina já cadastrada"
            );
        }

        Disciplina d = new Disciplina();
        d.setNome(nome);

        Disciplina salva = repository.save(d);

        return new CatalogoItemResponse(salva.getId(), salva.getNome());
    }

    public List<CatalogoItemResponse> listar() {
        return repository.findAllByOrderByNomeAsc()
                .stream()
                .map(d -> new CatalogoItemResponse(d.getId(), d.getNome()))
                .toList();
    }

    public CatalogoItemResponse atualizar(Long id, String nomeBruto) {
        Disciplina d = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
        String nome = nomeBruto.trim();
        if (repository.existsByNomeIgnoreCase(nome) && !d.getNome().equalsIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Disciplina já cadastrada");
        }
        d.setNome(nome);
        Disciplina salva = repository.save(d);
        return new CatalogoItemResponse(salva.getId(), salva.getNome());
    }

    public void excluir(Long id) {
        Disciplina d = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disciplina não encontrada"));
        repository.delete(d);
    }
}