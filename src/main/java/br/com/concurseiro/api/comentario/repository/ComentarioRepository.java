package br.com.concurseiro.api.comentario.repository;

import br.com.concurseiro.api.comentario.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    Page<Comentario> findByQuestaoId(String questaoId, Pageable pageable);
}
