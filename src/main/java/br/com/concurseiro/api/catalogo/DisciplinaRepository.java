package br.com.concurseiro.api.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    Optional<Disciplina> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}