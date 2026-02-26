
package br.com.concurseiro.api.catalogo.subassunto.repository;

import br.com.concurseiro.api.catalogo.subassunto.model.SubAssunto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubAssuntoRepository extends JpaRepository<SubAssunto, Long> {

    List<SubAssunto> findByAssuntoIdAndAtivoTrueOrderByNomeAsc(Long assuntoId);

    Optional<SubAssunto> findByAssuntoIdAndNomeIgnoreCase(Long assuntoId, String nome);

    boolean existsByAssuntoIdAndNomeIgnoreCase(Long assuntoId, String nome);
}