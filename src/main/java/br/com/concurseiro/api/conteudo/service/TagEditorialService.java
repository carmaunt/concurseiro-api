package br.com.concurseiro.api.conteudo.service;

import br.com.concurseiro.api.conteudo.dto.*;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.model.TagEditorial;
import br.com.concurseiro.api.conteudo.repository.ConteudoPortalRepository;
import br.com.concurseiro.api.conteudo.repository.TagEditorialRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class TagEditorialService {

    private final TagEditorialRepository repository;
    private final ConteudoPortalRepository conteudoRepository;

    public TagEditorialService(TagEditorialRepository repository, ConteudoPortalRepository conteudoRepository) {
        this.repository = repository;
        this.conteudoRepository = conteudoRepository;
    }

    @Transactional(readOnly = true)
    public Page<TagEditorialResponse> listar(String busca, StatusTaxonomia status, int page, int size) {
        validarPaginacao(page, size);
        Specification<TagEditorial> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (status != null) predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            if (busca != null && !busca.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("nome")), "%" + busca.trim().toLowerCase() + "%"));
            }
            return predicate;
        };
        return repository.findAll(spec, PageRequest.of(page, size, Sort.by("nome"))).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TagEditorialResponse buscar(Long id) { return toResponse(buscarEntidade(id)); }

    @Transactional(readOnly = true)
    public List<TaxonomiaResumoResponse> listarAtivas(String busca) {
        return repository.findAllByStatusOrderByNomeAsc(StatusTaxonomia.ATIVA).stream()
                .filter(item -> busca == null || busca.isBlank() || item.getNome().toLowerCase().contains(busca.trim().toLowerCase()))
                .map(item -> new TaxonomiaResumoResponse(item.getId(), item.getNome(), item.getSlug())).toList();
    }

    @Transactional
    public TagEditorialResponse criar(TagEditorialRequest request) {
        TagEditorial tag = new TagEditorial();
        aplicar(tag, request);
        validarDuplicidade(tag.getNome(), tag.getSlug(), null);
        return toResponse(repository.save(tag));
    }

    @Transactional
    public TagEditorialResponse atualizar(Long id, TagEditorialRequest request) {
        TagEditorial tag = buscarEntidade(id);
        aplicar(tag, request);
        validarDuplicidade(tag.getNome(), tag.getSlug(), id);
        return toResponse(repository.save(tag));
    }

    @Transactional
    public TagEditorialResponse alterarStatus(Long id, StatusTaxonomia status) {
        TagEditorial tag = buscarEntidade(id);
        tag.setStatus(status);
        return toResponse(repository.save(tag));
    }

    TagEditorial buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag não encontrada"));
    }

    private void aplicar(TagEditorial tag, TagEditorialRequest request) {
        String nome = request.nome().trim();
        tag.setNome(nome);
        tag.setSlug(ConteudoPortal.gerarSlug(request.slug() == null || request.slug().isBlank() ? nome : request.slug()));
    }

    private void validarDuplicidade(String nome, String slug, Long id) {
        boolean nomeExiste = id == null ? repository.existsByNomeIgnoreCase(nome) : repository.existsByNomeIgnoreCaseAndIdNot(nome, id);
        boolean slugExiste = id == null ? repository.existsBySlugIgnoreCase(slug) : repository.existsBySlugIgnoreCaseAndIdNot(slug, id);
        if (nomeExiste) throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma tag com este nome");
        if (slugExiste) throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma tag com este slug");
    }

    private TagEditorialResponse toResponse(TagEditorial tag) {
        return TagEditorialResponse.fromEntity(tag, conteudoRepository.countByTagsId(tag.getId()));
    }

    private void validarPaginacao(int page, int size) {
        if (page < 0 || size < 1 || size > 50) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação inválida");
    }
}
