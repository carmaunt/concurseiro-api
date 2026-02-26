package br.com.concurseiro.api.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssuntoService {

    private final AssuntoRepository repository;
    private final DisciplinaRepository disciplinaRepository;

    public AssuntoService(
            AssuntoRepository repository,
            DisciplinaRepository disciplinaRepository
    ) {
        this.repository = repository;
        this.disciplinaRepository = disciplinaRepository;
    }

    public CatalogoItemResponse cadastrar(Long disciplinaId, String nomeBruto) {

        var disciplina = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Disciplina não encontrada"
                ));

        String nome = nomeBruto.trim();

        if (repository.existsByDisciplinaIdAndNomeIgnoreCase(disciplinaId, nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assunto já cadastrado para esta disciplina"
            );
        }

        Assunto a = new Assunto();
        a.setDisciplina(disciplina);
        a.setNome(nome);

        Assunto salvo = repository.save(a);

        return new CatalogoItemResponse(salvo.getId(), salvo.getNome());
    }

    public List<CatalogoItemResponse> listarPorDisciplina(Long disciplinaId) {
        return repository.findByDisciplinaIdAndAtivoTrueOrderByNomeAsc(disciplinaId)
                .stream()
                .map(a -> new CatalogoItemResponse(a.getId(), a.getNome()))
                .toList();
    }
}