package br.com.concurseiro.api.questoes.spec;

import br.com.concurseiro.api.questoes.model.Questao;
import org.springframework.data.jpa.domain.Specification;

import java.text.Normalizer;

public class QuestaoSpecifications {

    public static Specification<Questao> cargoEquals(String cargo) {
        return (root, query, cb) ->
                cargo == null ? null :
                        cb.equal(cb.lower(root.get("cargo")), cargo.toLowerCase());
    }

    public static Specification<Questao> nivelEquals(String nivel) {
        return (root, query, cb) ->
                nivel == null ? null :
                        cb.equal(cb.lower(root.get("nivel")), nivel.toLowerCase());
    }

    public static Specification<Questao> modalidadeEquals(String modalidade) {
        return (root, query, cb) ->
                modalidade == null ? null :
                        cb.equal(cb.upper(root.get("modalidade")), modalidade.toUpperCase());
    }

    public static Specification<Questao> anoEquals(Integer ano) {
        return (root, query, cb) ->
                ano == null ? null :
                        cb.equal(root.get("ano"), ano);
    }

    // Busca textual (sem acento + UPPER) usando o campo textoBusca (otimizado para pesquisa)
    public static Specification<Questao> textoContains(String texto) {
        return (root, query, cb) -> {
            if (texto == null || texto.isBlank()) return null;

            String normalizado = Normalizer
                    .normalize(texto, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replaceAll("\\s+", " ")
                    .trim()
                    .toUpperCase();

            return cb.like(root.get("textoBusca"), "%" + normalizado + "%");
        };
    }

    public static Specification<Questao> disciplinaIdEquals(Long disciplinaId) {
        return (root, query, cb) ->
                disciplinaId == null ? null :
                        cb.equal(root.get("disciplinaCatalogo").get("id"), disciplinaId);
    }

    public static Specification<Questao> assuntoIdEquals(Long assuntoId) {
        return (root, query, cb) ->
                assuntoId == null ? null :
                        cb.equal(root.get("assuntoCatalogo").get("id"), assuntoId);
    }

    public static Specification<Questao> bancaIdEquals(Long bancaId) {
        return (root, query, cb) ->
                bancaId == null ? null :
                        cb.equal(root.get("bancaCatalogo").get("id"), bancaId);
    }

    public static Specification<Questao> instituicaoIdEquals(Long instituicaoId) {
        return (root, query, cb) ->
                instituicaoId == null ? null :
                        cb.equal(root.get("instituicaoCatalogo").get("id"), instituicaoId);
    }
}