package br.com.concurseiro.api.catalogo.banca.repository;

import br.com.concurseiro.api.catalogo.banca.model.Banca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BancaRepository extends JpaRepository<Banca, Long> {
    Optional<Banca> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);

    List<Banca> findAllByOrderByNomeAsc();
}