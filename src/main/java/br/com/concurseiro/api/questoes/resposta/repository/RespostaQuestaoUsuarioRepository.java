package br.com.concurseiro.api.questoes.resposta.repository;

import br.com.concurseiro.api.questoes.model.Questao;
import br.com.concurseiro.api.questoes.resposta.model.RespostaQuestaoUsuario;
import br.com.concurseiro.api.usuarios.model.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RespostaQuestaoUsuarioRepository extends JpaRepository<RespostaQuestaoUsuario, Long> {
    Optional<RespostaQuestaoUsuario> findFirstByUsuarioAndQuestaoOrderByRespondidaEmDesc(Usuario usuario, Questao questao);

    long countByUsuario(Usuario usuario);
    long countByUsuarioAndAcertou(Usuario usuario, boolean acertou);
    List<RespostaQuestaoUsuario> findByUsuarioOrderByRespondidaEmDesc(Usuario usuario, Pageable pageable);

    @Query("""
            select new br.com.concurseiro.api.estudante.dto.DesempenhoDisciplinaResponse(
                r.disciplina,
                count(r),
                sum(case when r.acertou = true then 1 else 0 end)
            )
            from RespostaQuestaoUsuario r
            where r.usuario = :usuario
            group by r.disciplina
            order by count(r) desc, r.disciplina asc
            """)
    List<br.com.concurseiro.api.estudante.dto.DesempenhoDisciplinaResponse> desempenhoPorDisciplina(Usuario usuario, Pageable pageable);
}
