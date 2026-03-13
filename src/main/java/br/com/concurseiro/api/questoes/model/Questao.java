package br.com.concurseiro.api.questoes.model;

import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import br.com.concurseiro.api.catalogo.banca.model.Banca;
import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import jakarta.persistence.*;

import java.text.Normalizer;
import java.time.OffsetDateTime;

@Entity
@Table(name = "questoes", indexes = {
        @Index(name = "idx_questao_ano", columnList = "ano"),
        @Index(name = "idx_questao_id_question", columnList = "id_question"),
        @Index(name = "idx_questao_texto_busca", columnList = "textoBusca")
})
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_question", nullable = false, unique = true, length = 16)
    private String idQuestion;

    @Lob
    @Column(nullable = false)
    private String enunciado;

    @Lob
    @Column(nullable = false)
    private String questao;

    @Lob
    @Column(nullable = false)
    private String alternativas;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, length = 160)
    private String cargo;

    @Column(nullable = false, length = 80)
    private String nivel;

    @Column(nullable = false, length = 40)
    private String modalidade;

    @Column(length = 8)
    private String gabarito;

    @Column(name = "prova_id")
    private Long provaId;

    @Column(nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplinaCatalogo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assuntoCatalogo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banca_id", nullable = false)
    private Banca bancaCatalogo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicaoCatalogo;

    @Column(nullable = true, length = 20000)
    private String textoBusca;

    @PrePersist
    @PreUpdate
    private void preencherTextoBusca() {
        this.textoBusca = normalizarParaBusca(
                (enunciado == null ? "" : enunciado) + " " +
                (questao == null ? "" : questao) + " " +
                (getAssunto() == null ? "" : getAssunto())
        );
    }

    private static String normalizarParaBusca(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", "");
        n = n.replaceAll("\\s+", " ").trim();
        return n.toUpperCase();
    }

    private String nomeOuNull(Object entidade) {
        if (entidade == null) {
            return null;
        }
        if (entidade instanceof Disciplina d) {
            return d.getNome();
        }
        if (entidade instanceof Assunto a) {
            return a.getNome();
        }
        if (entidade instanceof Banca b) {
            return b.getNome();
        }
        if (entidade instanceof Instituicao i) {
            return i.getNome();
        }
        return null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdQuestion() { return idQuestion; }
    public void setIdQuestion(String idQuestion) { this.idQuestion = idQuestion; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getQuestao() { return questao; }
    public void setQuestao(String questao) { this.questao = questao; }

    public String getAlternativas() { return alternativas; }
    public void setAlternativas(String alternativas) { this.alternativas = alternativas; }

    public String getDisciplina() { return nomeOuNull(disciplinaCatalogo); }
    public void setDisciplina(String disciplina) { }

    public String getAssunto() { return nomeOuNull(assuntoCatalogo); }
    public void setAssunto(String assunto) { }

    public String getBanca() { return nomeOuNull(bancaCatalogo); }
    public void setBanca(String banca) { }

    public String getInstituicao() { return nomeOuNull(instituicaoCatalogo); }
    public void setInstituicao(String instituicao) { }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }

    public String getGabarito() { return gabarito; }
    public void setGabarito(String gabarito) { this.gabarito = gabarito; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(OffsetDateTime criadoEm) { this.criadoEm = criadoEm; }

    public String getTextoBusca() { return textoBusca; }
    public void setTextoBusca(String textoBusca) { this.textoBusca = textoBusca; }

    public Disciplina getDisciplinaCatalogo() { return disciplinaCatalogo; }
    public void setDisciplinaCatalogo(Disciplina disciplinaCatalogo) { this.disciplinaCatalogo = disciplinaCatalogo; }

    public Assunto getAssuntoCatalogo() { return assuntoCatalogo; }
    public void setAssuntoCatalogo(Assunto assuntoCatalogo) { this.assuntoCatalogo = assuntoCatalogo; }

    public Banca getBancaCatalogo() { return bancaCatalogo; }
    public void setBancaCatalogo(Banca bancaCatalogo) { this.bancaCatalogo = bancaCatalogo; }

    public Instituicao getInstituicaoCatalogo() { return instituicaoCatalogo; }
    public void setInstituicaoCatalogo(Instituicao instituicaoCatalogo) { this.instituicaoCatalogo = instituicaoCatalogo; }

    public Long getProvaId() { return provaId; }
    public void setProvaId(Long provaId) { this.provaId = provaId; }
}