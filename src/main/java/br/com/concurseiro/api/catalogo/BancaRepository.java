package br.com.concurseiro.api.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BancaRepository extends JpaRepository<Banca, Long> {
    Optional<Banca> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}