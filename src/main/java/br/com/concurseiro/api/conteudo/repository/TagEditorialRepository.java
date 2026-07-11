package br.com.concurseiro.api.conteudo.repository;

import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import br.com.concurseiro.api.conteudo.model.TagEditorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface TagEditorialRepository extends JpaRepository<TagEditorial, Long>, JpaSpecificationExecutor<TagEditorial> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    boolean existsBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);
    List<TagEditorial> findAllByStatusOrderByNomeAsc(StatusTaxonomia status);
    List<TagEditorial> findAllByIdIn(Collection<Long> ids);

    @Query("""
            select distinct tag from ConteudoPortal conteudo
            join conteudo.tags tag
            where tag.status = :statusTaxonomia
              and conteudo.tipo = :tipo
              and conteudo.status = :statusConteudo
              and conteudo.publicadoEm is not null
              and conteudo.publicadoEm <= :agora
            order by tag.nome
            """)
    List<TagEditorial> findPublicasComConteudo(
            @Param("tipo") ConteudoPortal.Tipo tipo,
            @Param("statusConteudo") ConteudoPortal.Status statusConteudo,
            @Param("statusTaxonomia") StatusTaxonomia statusTaxonomia,
            @Param("agora") OffsetDateTime agora
    );
}
