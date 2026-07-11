package br.com.concurseiro.api.conteudo.service;

import br.com.concurseiro.api.conteudo.dto.*;
import br.com.concurseiro.api.conteudo.model.CategoriaEditorial;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.repository.CategoriaEditorialRepository;
import br.com.concurseiro.api.conteudo.repository.ConteudoPortalRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class CategoriaEditorialService {

    private final CategoriaEditorialRepository repository;
    private final ConteudoPortalRepository conteudoRepository;

    public CategoriaEditorialService(CategoriaEditorialRepository repository, ConteudoPortalRepository conteudoRepository) {
        this.repository = repository;
        this.conteudoRepository = conteudoRepository;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaEditorialResponse> listar(String busca, StatusTaxonomia status, int page, int size) {
        validarPaginacao(page, size);
        Specification<CategoriaEditorial> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (status != null) predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            if (busca != null && !busca.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("nome")), "%" + busca.trim().toLowerCase() + "%"));
            }
            return predicate;
        };
        return repository.findAll(spec, PageRequest.of(page, size, Sort.by("nome")))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoriaEditorialResponse buscar(Long id) { return toResponse(buscarEntidade(id)); }

    @Transactional(readOnly = true)
    public List<TaxonomiaResumoResponse> listarAtivas() {
        return repository.findAllByStatusOrderByNomeAsc(StatusTaxonomia.ATIVA).stream()
                .map(item -> new TaxonomiaResumoResponse(item.getId(), item.getNome(), item.getSlug())).toList();
    }

    @Transactional
    public CategoriaEditorialResponse criar(CategoriaEditorialRequest request) {
        CategoriaEditorial categoria = new CategoriaEditorial();
        aplicar(categoria, request);
        validarDuplicidade(categoria.getNome(), categoria.getSlug(), null);
        return toResponse(repository.save(categoria));
    }

    @Transactional
    public CategoriaEditorialResponse atualizar(Long id, CategoriaEditorialRequest request) {
        CategoriaEditorial categoria = buscarEntidade(id);
        aplicar(categoria, request);
        validarDuplicidade(categoria.getNome(), categoria.getSlug(), id);
        return toResponse(repository.save(categoria));
    }

    @Transactional
    public CategoriaEditorialResponse alterarStatus(Long id, StatusTaxonomia status) {
        CategoriaEditorial categoria = buscarEntidade(id);
        categoria.setStatus(status);
        return toResponse(repository.save(categoria));
    }

    CategoriaEditorial buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    private void aplicar(CategoriaEditorial categoria, CategoriaEditorialRequest request) {
        String nome = request.nome().trim();
        categoria.setNome(nome);
        categoria.setSlug(ConteudoPortal.gerarSlug(request.slug() == null || request.slug().isBlank() ? nome : request.slug()));
        categoria.setDescricao(request.descricao() == null || request.descricao().isBlank() ? null : request.descricao().trim());
    }

    private void validarDuplicidade(String nome, String slug, Long id) {
        boolean nomeExiste = id == null ? repository.existsByNomeIgnoreCase(nome) : repository.existsByNomeIgnoreCaseAndIdNot(nome, id);
        boolean slugExiste = id == null ? repository.existsBySlugIgnoreCase(slug) : repository.existsBySlugIgnoreCaseAndIdNot(slug, id);
        if (nomeExiste) throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com este nome");
        if (slugExiste) throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com este slug");
    }

    private CategoriaEditorialResponse toResponse(CategoriaEditorial categoria) {
        return CategoriaEditorialResponse.fromEntity(categoria, conteudoRepository.countByCategoriaId(categoria.getId()));
    }

    private void validarPaginacao(int page, int size) {
        if (page < 0 || size < 1 || size > 50) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação inválida");
    }
}
