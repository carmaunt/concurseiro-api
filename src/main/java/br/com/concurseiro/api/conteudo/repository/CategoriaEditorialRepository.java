package br.com.concurseiro.api.conteudo.repository;

import br.com.concurseiro.api.conteudo.model.CategoriaEditorial;
import br.com.concurseiro.api.conteudo.model.StatusTaxonomia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.concurseiro.api.conteudo.model.ConteudoPortal;
import java.time.OffsetDateTime;
import java.util.List;

public interface CategoriaEditorialRepository extends JpaRepository<CategoriaEditorial, Long>, JpaSpecificationExecutor<CategoriaEditorial> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    boolean existsBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);
    List<CategoriaEditorial> findAllByStatusOrderByNomeAsc(StatusTaxonomia status);

    @Query("""
            select distinct categoria from ConteudoPortal conteudo
            join conteudo.categoria categoria
            where categoria.status = :statusTaxonomia
              and conteudo.tipo = :tipo
              and conteudo.status = :statusConteudo
              and conteudo.publicadoEm is not null
              and conteudo.publicadoEm <= :agora
            order by categoria.nome
            """)
    List<CategoriaEditorial> findPublicasComConteudo(
            @Param("tipo") ConteudoPortal.Tipo tipo,
            @Param("statusConteudo") ConteudoPortal.Status statusConteudo,
            @Param("statusTaxonomia") StatusTaxonomia statusTaxonomia,
            @Param("agora") OffsetDateTime agora
    );
}
