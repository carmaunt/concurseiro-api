package br.com.concurseiro.api.conteudo.service;

import br.com.concurseiro.api.conteudo.dto.ConteudoPortalRequest;
import br.com.concurseiro.api.conteudo.dto.ConteudoPortalResponse;
import br.com.concurseiro.api.conteudo.dto.FonteEditorial;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.concurseiro.api.conteudo.event.ConteudoPublicoAlteradoEvent;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.model.TagEditorial;
import br.com.concurseiro.api.conteudo.repository.CategoriaEditorialRepository;
import br.com.concurseiro.api.conteudo.repository.ConteudoPortalRepository;
import br.com.concurseiro.api.conteudo.repository.TagEditorialRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.time.OffsetDateTime;

@Service
public class ConteudoPortalService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ConteudoPortalRepository repository;
    private final CategoriaEditorialRepository categoriaRepository;
    private final TagEditorialRepository tagRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConteudoPortalService(ConteudoPortalRepository repository,
                                 CategoriaEditorialRepository categoriaRepository,
                                 TagEditorialRepository tagRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository = tagRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<ConteudoPortalResponse> listarPublicados(ConteudoPortal.Tipo tipo, String busca, String categoriaLegada,
                                                         String categoriaSlug, String tagSlug, int page, int size) {
        validarPaginacao(page, size);
        return repository.findAll(specPublico(tipo, busca, categoriaLegada, categoriaSlug, tagSlug), pageRequest(page, size))
                .map(ConteudoPortalResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ConteudoPortalResponse> listarDestaques(int page, int size) {
        validarPaginacao(page, size);
        return repository.findAll(
                specPublico(null, null, null, null, null).and((root, query, cb) -> cb.isTrue(root.get("destaque"))),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publicadoEm"))
        ).map(ConteudoPortalResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ConteudoPortalResponse buscarPublicado(ConteudoPortal.Tipo tipo, String slug) {
        return repository.findOne(specPublico(tipo, null, null, null, null).and((root, query, cb) ->
                        cb.equal(root.get("slug"), ConteudoPortal.gerarSlug(slug))))
                .map(ConteudoPortalResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conteúdo não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<ConteudoPortalResponse> listarAdmin(ConteudoPortal.Tipo tipo, ConteudoPortal.Status status, String busca, int page, int size) {
        validarPaginacao(page, size);
        return repository.findAll(spec(tipo, status, busca, null), pageRequest(page, size))
                .map(ConteudoPortalResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ConteudoPortalResponse buscarAdmin(Long id) {
        return ConteudoPortalResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public ConteudoPortalResponse criar(ConteudoPortalRequest request) {
        ConteudoPortal conteudo = new ConteudoPortal();
        aplicarRequest(conteudo, request);
        validarSlugUnico(conteudo.getTipo(), conteudo.getSlug(), null);
        ConteudoPortal salvo = repository.save(conteudo);
        if (salvo.getStatus() == ConteudoPortal.Status.PUBLICADO) publicarRevalidacao(salvo, "criação");
        return ConteudoPortalResponse.fromEntity(salvo);
    }

    @Transactional
    public ConteudoPortalResponse atualizar(Long id, ConteudoPortalRequest request) {
        ConteudoPortal conteudo = buscarEntidade(id);
        boolean eraPublicado = conteudo.getStatus() == ConteudoPortal.Status.PUBLICADO;
        aplicarRequest(conteudo, request);
        validarSlugUnico(conteudo.getTipo(), conteudo.getSlug(), id);
        ConteudoPortal salvo = repository.save(conteudo);
        if (eraPublicado || salvo.getStatus() == ConteudoPortal.Status.PUBLICADO) publicarRevalidacao(salvo, "atualização");
        return ConteudoPortalResponse.fromEntity(salvo);
    }

    @Transactional
    public ConteudoPortalResponse alterarStatus(Long id, ConteudoPortal.Status status) {
        ConteudoPortal conteudo = buscarEntidade(id);
        boolean eraPublicado = conteudo.getStatus() == ConteudoPortal.Status.PUBLICADO;
        conteudo.setStatus(status);
        ConteudoPortal salvo = repository.save(conteudo);
        if (eraPublicado || status == ConteudoPortal.Status.PUBLICADO) publicarRevalidacao(salvo, "status");
        return ConteudoPortalResponse.fromEntity(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        ConteudoPortal conteudo = buscarEntidade(id);
        boolean eraPublicado = conteudo.getStatus() == ConteudoPortal.Status.PUBLICADO;
        repository.delete(conteudo);
        if (eraPublicado) publicarRevalidacao(conteudo, "exclusão");
    }

    private ConteudoPortal buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conteúdo não encontrado"));
    }

    private void aplicarRequest(ConteudoPortal conteudo, ConteudoPortalRequest request) {
        conteudo.setTitulo(request.titulo().trim());
        conteudo.setSlug(ConteudoPortal.gerarSlug(request.slug() == null || request.slug().isBlank() ? request.titulo() : request.slug()));
        conteudo.setResumo(request.resumo().trim());
        conteudo.setConteudo(request.conteudo().trim());
        conteudo.setImagemCapa(normalizarOpcional(request.imagemCapa()));
        conteudo.setImagemCapaAlt(normalizarOpcional(request.imagemCapaAlt()));
        conteudo.setImagemSecundaria(normalizarOpcional(request.imagemSecundaria()));
        conteudo.setImagemSecundariaAlt(normalizarOpcional(request.imagemSecundariaAlt()));
        conteudo.setAutorNome(normalizarOpcional(request.autorNome()));
        conteudo.setRevisadoPor(normalizarOpcional(request.revisadoPor()));
        conteudo.setFontesOficiais(serializarFontes(request.fontesOficiais()));
        aplicarCategoria(conteudo, request.categoriaId());
        aplicarTags(conteudo, request.tagIds());
        conteudo.setStatus(request.status());
        conteudo.setTipo(request.tipo());
        conteudo.setDestaque(request.destaque());
        conteudo.setSeoTitulo(normalizarOpcional(request.seoTitulo()));
        conteudo.setSeoDescricao(normalizarOpcional(request.seoDescricao()));
    }

    private void aplicarCategoria(ConteudoPortal conteudo, Long categoriaId) {
        if (categoriaId == null) {
            conteudo.setCategoria(null);
            return;
        }

        var categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria não encontrada"));
        boolean jaAssociada = conteudo.getCategoria() != null && conteudo.getCategoria().getId().equals(categoriaId);
        if (categoria.getStatus() == StatusTaxonomia.ARQUIVADA && !jaAssociada) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria arquivada não pode ser associada a novos conteúdos");
        }
        conteudo.setCategoria(categoria);
    }

    private void aplicarTags(ConteudoPortal conteudo, Set<Long> tagIds) {
        Set<Long> idsSolicitados = tagIds == null ? Set.of() : new LinkedHashSet<>(tagIds);
        Set<Long> idsAtuais = conteudo.getTags().stream().map(TagEditorial::getId).collect(java.util.stream.Collectors.toSet());
        List<TagEditorial> tags = idsSolicitados.isEmpty() ? List.of() : tagRepository.findAllByIdIn(idsSolicitados);
        if (tags.size() != idsSolicitados.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais tags não foram encontradas");
        }
        boolean possuiArquivadaNova = tags.stream().anyMatch(tag -> tag.getStatus() == StatusTaxonomia.ARQUIVADA && !idsAtuais.contains(tag.getId()));
        if (possuiArquivadaNova) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag arquivada não pode ser associada a novos conteúdos");
        }
        conteudo.setTags(new LinkedHashSet<>(tags));
    }

    private void validarSlugUnico(ConteudoPortal.Tipo tipo, String slug, Long idAtual) {
        boolean exists = idAtual == null
                ? repository.existsByTipoAndSlug(tipo, slug)
                : repository.existsByTipoAndSlugAndIdNot(tipo, slug, idAtual);

        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug já existe para este tipo de conteúdo");
        }
    }

    private Specification<ConteudoPortal> spec(ConteudoPortal.Tipo tipo, ConteudoPortal.Status status, String busca, String categoria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (busca != null && !busca.isBlank()) {
                String like = "%" + busca.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("titulo")), like),
                        cb.like(cb.lower(root.get("resumo")), like),
                        cb.like(cb.lower(root.get("conteudo")), like)
                ));
            }

            if (categoria != null && !categoria.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.join("categoria", JoinType.LEFT).get("nome")),
                        categoria.trim().toLowerCase()
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<ConteudoPortal> specPublico(ConteudoPortal.Tipo tipo, String busca, String categoriaLegada,
                                                       String categoriaSlug, String tagSlug) {
        return spec(tipo, ConteudoPortal.Status.PUBLICADO, busca, categoriaLegada).and((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNotNull(root.get("publicadoEm")));
            predicates.add(cb.lessThanOrEqualTo(root.get("publicadoEm"), OffsetDateTime.now()));

            if (categoriaSlug != null && !categoriaSlug.isBlank()) {
                var categoria = root.join("categoria", JoinType.INNER);
                predicates.add(cb.equal(cb.lower(categoria.get("slug")), categoriaSlug.trim().toLowerCase()));
                predicates.add(cb.equal(categoria.get("status"), StatusTaxonomia.ATIVA));
            }

            if (tagSlug != null && !tagSlug.isBlank()) {
                var tag = root.join("tags", JoinType.INNER);
                predicates.add(cb.equal(cb.lower(tag.get("slug")), tagSlug.trim().toLowerCase()));
                predicates.add(cb.equal(tag.get("status"), StatusTaxonomia.ATIVA));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        });
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publicadoEm").and(Sort.by(Sort.Direction.DESC, "updatedAt")));
    }

    private void validarPaginacao(int page, int size) {
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page não pode ser negativa");
        if (size < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size deve ser maior que zero");
        if (size > MAX_PAGE_SIZE) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size máximo permitido é " + MAX_PAGE_SIZE);
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    private String serializarFontes(List<FonteEditorial> fontes) {
        if (fontes == null || fontes.isEmpty()) return null;
        try { return new ObjectMapper().writeValueAsString(fontes); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fontes oficiais inválidas"); }
    }

    private void publicarRevalidacao(ConteudoPortal conteudo, String operacao) {
        eventPublisher.publishEvent(new ConteudoPublicoAlteradoEvent(conteudo.getId(), operacao));
    }
}
