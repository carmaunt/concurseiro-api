package br.com.concurseiro.api.questoes.repository;

import br.com.concurseiro.api.questoes.model.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuestaoRepository extends JpaRepository<Questao, Long>, JpaSpecificationExecutor<Questao> {

    Optional<Questao> findByIdQuestion(String idQuestion);

    @Query("""
        select q
        from Questao q
        left join fetch q.disciplinaCatalogo
        left join fetch q.assuntoCatalogo
        left join fetch q.bancaCatalogo
        left join fetch q.instituicaoCatalogo
        where q.idQuestion = :idQuestion
    """)
    Optional<Questao> findDetalhadaByIdQuestion(String idQuestion);

    long countByProvaId(Long provaId);
}