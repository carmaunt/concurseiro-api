package br.com.concurseiro.api.catalogo.banca.service;

import br.com.concurseiro.api.catalogo.banca.model.Banca;
import br.com.concurseiro.api.catalogo.banca.repository.BancaRepository;
import br.com.concurseiro.api.catalogo.disciplina.dto.CatalogoItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BancaService {

    private final BancaRepository repository;

    public BancaService(BancaRepository repository) {
        this.repository = repository;
    }

    public CatalogoItemResponse cadastrar(String nomeBruto) {

        String nome = nomeBruto.trim();

        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Banca já cadastrada"
            );
        }

        Banca banca = new Banca();
        banca.setNome(nome);

        Banca salva = repository.save(banca);

        return new CatalogoItemResponse(salva.getId(), salva.getNome());
    }

    public List<CatalogoItemResponse> listar() {
        return repository.findAllByOrderByNomeAsc()
                .stream()
                .map(b -> new CatalogoItemResponse(b.getId(), b.getNome()))
                .toList();
    }

    public CatalogoItemResponse atualizar(Long id, String nomeBruto) {
        Banca b = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banca não encontrada"));
        String nome = nomeBruto.trim();
        if (repository.existsByNomeIgnoreCase(nome) && !b.getNome().equalsIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Banca já cadastrada");
        }
        b.setNome(nome);
        Banca salva = repository.save(b);
        return new CatalogoItemResponse(salva.getId(), salva.getNome());
    }

    public void excluir(Long id) {
        Banca b = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banca não encontrada"));
        repository.delete(b);
    }
}