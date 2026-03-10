package br.com.concurseiro.api.prova.repository;

import br.com.concurseiro.api.prova.model.Prova;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvaRepository extends JpaRepository<Prova, Long> {

    boolean existsByBancaIgnoreCaseAndInstituicaoIdAndAnoAndCargoIgnoreCaseAndNivelIgnoreCaseAndModalidadeIgnoreCase(
        String banca,
        Long instituicaoId,
        Integer ano,
        String cargo,
        String nivel,
        String modalidade
    );
}