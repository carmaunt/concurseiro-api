package br.com.concurseiro.api.prova.repository;

import br.com.concurseiro.api.prova.model.Prova;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvaRepository extends JpaRepository<Prova, Long> {
    Page<Prova> findAllByOrderByCriadoEmDesc(Pageable pageable);
}
