package br.com.concurseiro.api.questoes.repository;

import br.com.concurseiro.api.questoes.model.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Long>, JpaSpecificationExecutor<Questao> {

    Optional<Questao> findByIdQuestion(String idQuestion);

    @Query("""
        select q
        from Questao q
        left join fetch q.disciplinaCatalogo
        left join fetch q.assuntoCatalogo
        left join fetch q.subAssuntoCatalogo
        left join fetch q.bancaCatalogo
        left join fetch q.instituicaoCatalogo
        left join fetch q.textoApoio
        left join fetch q.enunciadoCatalogo
        where q.idQuestion = :idQuestion
    """)
    Optional<Questao> findDetalhadaByIdQuestion(String idQuestion);

    long countByProvaId(Long provaId);

    @Query("select distinct q.ano from Questao q where q.ano is not null order by q.ano desc")
    List<Integer> findAnosDisponiveis();
}
