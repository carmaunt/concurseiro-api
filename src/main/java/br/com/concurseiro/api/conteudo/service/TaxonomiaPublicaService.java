package br.com.concurseiro.api.conteudo.service;

import br.com.concurseiro.api.conteudo.dto.TaxonomiaResumoResponse;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.repository.CategoriaEditorialRepository;
import br.com.concurseiro.api.conteudo.repository.TagEditorialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TaxonomiaPublicaService {
    private final CategoriaEditorialRepository categoriaRepository;
    private final TagEditorialRepository tagRepository;

    public TaxonomiaPublicaService(CategoriaEditorialRepository categoriaRepository, TagEditorialRepository tagRepository) {
        this.categoriaRepository = categoriaRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<TaxonomiaResumoResponse> listarCategorias(ConteudoPortal.Tipo tipo) {
        return categoriaRepository.findPublicasComConteudo(tipo, ConteudoPortal.Status.PUBLICADO,
                        StatusTaxonomia.ATIVA, OffsetDateTime.now()).stream()
                .map(item -> new TaxonomiaResumoResponse(item.getId(), item.getNome(), item.getSlug()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaxonomiaResumoResponse> listarTags(ConteudoPortal.Tipo tipo) {
        return tagRepository.findPublicasComConteudo(tipo, ConteudoPortal.Status.PUBLICADO,
                        StatusTaxonomia.ATIVA, OffsetDateTime.now()).stream()
                .map(item -> new TaxonomiaResumoResponse(item.getId(), item.getNome(), item.getSlug()))
                .toList();
    }
}
