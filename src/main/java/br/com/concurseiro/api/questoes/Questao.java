package br.com.concurseiro.api.questoes;

import jakarta.persistence.*;

import java.text.Normalizer;
import java.time.OffsetDateTime;

@Entity
@Table(name = "questoes")
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

    @Column(nullable = false, length = 160)
    private String disciplina;

    @Column(nullable = false, length = 200)
    private String assunto;

    @Column(nullable = false, length = 160)
    private String banca;

    @Column(nullable = false, length = 200)
    private String instituicao;

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

    @Column(nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    // Campo otimizado para busca (sem acento + UPPER), evita problemas de CLOB e melhora performance
    @Column(nullable = true, length = 20000)
    private String textoBusca;

    @PrePersist
    @PreUpdate
    private void preencherTextoBusca() {
        this.textoBusca = normalizarParaBusca(
                (enunciado == null ? "" : enunciado) + " " +
                (questao == null ? "" : questao) + " " +
                (assunto == null ? "" : assunto)
        );
    }

    private static String normalizarParaBusca(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", "");          // remove acentos
        n = n.replaceAll("\\s+", " ").trim();   // normaliza espaços
        return n.toUpperCase();
    }

    // getters/setters

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

    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }

    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }

    public String getBanca() { return banca; }
    public void setBanca(String banca) { this.banca = banca; }

    public String getInstituicao() { return instituicao; }
    public void setInstituicao(String instituicao) { this.instituicao = instituicao; }

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
}