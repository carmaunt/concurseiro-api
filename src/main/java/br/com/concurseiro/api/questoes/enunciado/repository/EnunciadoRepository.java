package br.com.concurseiro.api.questoes.enunciado.repository;

import br.com.concurseiro.api.questoes.enunciado.model.Enunciado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EnunciadoRepository extends JpaRepository<Enunciado, Long> {

    Optional<Enunciado> findByHashSha256(String hashSha256);

    Page<Enunciado> findByConteudoContainingIgnoreCase(String conteudo, Pageable pageable);

    @Modifying
    @Query(value = """
            INSERT INTO enunciados (conteudo, hash_sha256, criado_em)
            VALUES (:conteudo, :hash, CURRENT_TIMESTAMP)
            ON CONFLICT (hash_sha256) DO NOTHING
            """, nativeQuery = true)
    int inserirSeAusente(@Param("conteudo") String conteudo, @Param("hash") String hash);
}
