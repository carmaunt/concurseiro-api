package br.com.concurseiro.api.catalogo;

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
        return repository.findAll()
                .stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(b -> new CatalogoItemResponse(b.getId(), b.getNome()))
                .toList();
    }
}