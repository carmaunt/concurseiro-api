package br.com.concurseiro.api.catalogo; 

import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.Optional; 

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> { 
    Optional<Instituicao> findByNomeIgnoreCase(String nome); 
    boolean existsByNomeIgnoreCase(String nome); 
}