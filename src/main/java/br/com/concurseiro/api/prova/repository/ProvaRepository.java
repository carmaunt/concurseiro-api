package br.com.concurseiro.api.prova.repository;

import br.com.concurseiro.api.prova.model.Prova;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvaRepository extends JpaRepository<Prova, Long> {

    boolean existsByBancaIgnoreCaseAndInstituicaoCatalogoIdAndAnoAndCargoIgnoreCaseAndNivelIgnoreCaseAndModalidadeIgnoreCase(
            String banca,
            Long instituicaoCatalogoId,
            Integer ano,
            String cargo,
            String nivel,
            String modalidade
    );
}