package br.com.concurseiro.api.catalogo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SubAssuntoService {

    private final SubAssuntoRepository subAssuntoRepository;
    private final AssuntoRepository assuntoRepository;

    public SubAssuntoService(
            SubAssuntoRepository subAssuntoRepository,
            AssuntoRepository assuntoRepository
    ) {
        this.subAssuntoRepository = subAssuntoRepository;
        this.assuntoRepository = assuntoRepository;
    }

    public CatalogoItemResponse cadastrar(Long assuntoId, String nomeBruto) {

        var assunto = assuntoRepository.findById(assuntoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assunto não encontrado"
                ));

        String nome = nomeBruto.trim();

        if (subAssuntoRepository.existsByAssuntoIdAndNomeIgnoreCase(assuntoId, nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Subassunto já cadastrado para este assunto"
            );
        }

        SubAssunto sub = new SubAssunto();
        sub.setAssunto(assunto);
        sub.setNome(nome);

        SubAssunto salvo = subAssuntoRepository.save(sub);

        return new CatalogoItemResponse(salvo.getId(), salvo.getNome());
    }

    public List<CatalogoItemResponse> listarPorAssunto(Long assuntoId) {
        return subAssuntoRepository.findByAssuntoIdAndAtivoTrueOrderByNomeAsc(assuntoId)
                .stream()
                .map(s -> new CatalogoItemResponse(s.getId(), s.getNome()))
                .toList();
    }
}