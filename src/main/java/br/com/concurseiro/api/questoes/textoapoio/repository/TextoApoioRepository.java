package br.com.concurseiro.api.questoes.textoapoio.repository;

import br.com.concurseiro.api.questoes.textoapoio.model.TextoApoio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TextoApoioRepository extends JpaRepository<TextoApoio, Long> {

    Optional<TextoApoio> findByHashSha256(String hashSha256);

    Page<TextoApoio> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);
}
