package br.com.concurseiro.api.comentario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    Page<Comentario> findByQuestaoId(String questaoId, Pageable pageable);
}