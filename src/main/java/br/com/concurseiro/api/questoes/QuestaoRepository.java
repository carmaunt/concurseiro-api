package br.com.concurseiro.api.questoes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface QuestaoRepository extends JpaRepository<Questao, Long>, JpaSpecificationExecutor<Questao> {

    Optional<Questao> findByIdQuestion(String idQuestion);

}