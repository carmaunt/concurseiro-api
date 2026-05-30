package br.com.concurseiro.api.catalogo.importacao.service;

import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import br.com.concurseiro.api.catalogo.assunto.repository.AssuntoRepository;
import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import br.com.concurseiro.api.catalogo.disciplina.repository.DisciplinaRepository;
import br.com.concurseiro.api.catalogo.importacao.dto.ImportarCatalogoResponse;
import br.com.concurseiro.api.catalogo.subassunto.model.SubAssunto;
import br.com.concurseiro.api.catalogo.subassunto.repository.SubAssuntoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoImportacaoService {

    private final CatalogoTextoParser parser;
    private final DisciplinaRepository disciplinaRepository;
    private final AssuntoRepository assuntoRepository;
    private final SubAssuntoRepository subAssuntoRepository;

    public CatalogoImportacaoService(
            CatalogoTextoParser parser,
            DisciplinaRepository disciplinaRepository,
            AssuntoRepository assuntoRepository,
            SubAssuntoRepository subAssuntoRepository
    ) {
        this.parser = parser;
        this.disciplinaRepository = disciplinaRepository;
        this.assuntoRepository = assuntoRepository;
        this.subAssuntoRepository = subAssuntoRepository;
    }

    @Transactional
    public ImportarCatalogoResponse importarTexto(String texto) {
        CatalogoTextoParser.CatalogoTexto catalogo = parser.parse(texto);

        ResultadoDisciplina resultadoDisciplina = obterOuCriarDisciplina(catalogo.disciplina());

        int assuntosProcessados = 0;
        int assuntosCriados = 0;
        int assuntosExistentes = 0;
        int subassuntosProcessados = 0;
        int subassuntosCriados = 0;
        int subassuntosExistentes = 0;

        for (CatalogoTextoParser.AssuntoTexto assuntoTexto : catalogo.assuntos()) {
            assuntosProcessados++;

            ResultadoAssunto resultadoAssunto = obterOuCriarAssunto(
                    resultadoDisciplina.disciplina(),
                    assuntoTexto.nome()
            );

            if (resultadoAssunto.criado()) {
                assuntosCriados++;
            } else {
                assuntosExistentes++;
            }

            for (String subassuntoNome : assuntoTexto.subassuntos()) {
                subassuntosProcessados++;
                boolean criado = obterOuCriarSubassunto(resultadoAssunto.assunto(), subassuntoNome);

                if (criado) {
                    subassuntosCriados++;
                } else {
                    subassuntosExistentes++;
                }
            }
        }

        return new ImportarCatalogoResponse(
                resultadoDisciplina.disciplina().getId(),
                resultadoDisciplina.disciplina().getNome(),
                assuntosProcessados,
                assuntosCriados,
                assuntosExistentes,
                subassuntosProcessados,
                subassuntosCriados,
                subassuntosExistentes
        );
    }

    private ResultadoDisciplina obterOuCriarDisciplina(String nomeBruto) {
        String nome = nomeBruto.trim();

        return disciplinaRepository.findByNomeIgnoreCase(nome)
                .map(disciplina -> new ResultadoDisciplina(disciplina, false))
                .orElseGet(() -> {
                    Disciplina disciplina = new Disciplina();
                    disciplina.setNome(nome);
                    return new ResultadoDisciplina(disciplinaRepository.save(disciplina), true);
                });
    }

    private ResultadoAssunto obterOuCriarAssunto(Disciplina disciplina, String nomeBruto) {
        String nome = nomeBruto.trim();

        return assuntoRepository.findByDisciplinaIdAndNomeIgnoreCase(disciplina.getId(), nome)
                .map(assunto -> new ResultadoAssunto(assunto, false))
                .orElseGet(() -> {
                    Assunto assunto = new Assunto();
                    assunto.setDisciplina(disciplina);
                    assunto.setNome(nome);
                    return new ResultadoAssunto(assuntoRepository.save(assunto), true);
                });
    }

    private boolean obterOuCriarSubassunto(Assunto assunto, String nomeBruto) {
        String nome = nomeBruto.trim();

        return subAssuntoRepository.findByAssuntoIdAndNomeIgnoreCase(assunto.getId(), nome)
                .map(subassunto -> false)
                .orElseGet(() -> {
                    SubAssunto subAssunto = new SubAssunto();
                    subAssunto.setAssunto(assunto);
                    subAssunto.setNome(nome);
                    subAssuntoRepository.save(subAssunto);
                    return true;
                });
    }

    private record ResultadoDisciplina(Disciplina disciplina, boolean criado) {}

    private record ResultadoAssunto(Assunto assunto, boolean criado) {}
}
