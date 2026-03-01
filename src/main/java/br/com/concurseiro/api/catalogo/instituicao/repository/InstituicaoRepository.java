package br.com.concurseiro.api.catalogo.instituicao.repository;

import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    Optional<Instituicao> findByNomeIgnoreCase(String nome); 
    boolean existsByNomeIgnoreCase(String nome);

    List<Instituicao> findAllByOrderByNomeAsc();
}