package br.com.concurseiro.api.catalogo.assunto.repository;

import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AssuntoRepository extends JpaRepository<Assunto, Long> {

    List<Assunto> findByDisciplinaIdAndAtivoTrueOrderByNomeAsc(Long disciplinaId);

    Optional<Assunto> findByDisciplinaIdAndNomeIgnoreCase(Long disciplinaId, String nome);

    boolean existsByDisciplinaIdAndNomeIgnoreCase(Long disciplinaId, String nome);
}