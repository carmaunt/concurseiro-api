package br.com.concurseiro.api.catalogo.disciplina.repository;

import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    Optional<Disciplina> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    List<Disciplina> findAllByOrderByNomeAsc();
}