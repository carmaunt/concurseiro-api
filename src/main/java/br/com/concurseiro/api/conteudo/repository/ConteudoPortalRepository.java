package br.com.concurseiro.api.conteudo.repository;

import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ConteudoPortalRepository extends JpaRepository<ConteudoPortal, Long>, JpaSpecificationExecutor<ConteudoPortal> {
    boolean existsByTipoAndSlug(ConteudoPortal.Tipo tipo, String slug);
    boolean existsByTipoAndSlugAndIdNot(ConteudoPortal.Tipo tipo, String slug, Long id);
    Optional<ConteudoPortal> findByTipoAndSlugAndStatus(ConteudoPortal.Tipo tipo, String slug, ConteudoPortal.Status status);
    Page<ConteudoPortal> findByStatusAndDestaqueTrue(ConteudoPortal.Status status, Pageable pageable);
    long countByCategoriaId(Long categoriaId);
    long countByTagsId(Long tagId);
}
