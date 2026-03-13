package br.com.concurseiro.api.prova.model;

import br.com.concurseiro.api.catalogo.instituicao.model.Instituicao;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "provas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_provas_cabecalho",
            columnNames = {
                "banca",
                "instituicao_id",
                "ano",
                "cargo",
                "nivel",
                "modalidade"
            }
        )
    }
)
public class Prova {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String banca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicaoCatalogo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false, length = 160)
    private String cargo;

    @Column(nullable = false, length = 80)
    private String nivel;

    @Column(nullable = false, length = 40)
    private String modalidade;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public Long getId() { return id; }
    public String getBanca() { return banca; }
    public String getInstituicao() { return instituicaoCatalogo != null ? instituicaoCatalogo.getNome() : null; }
    public Long getInstituicaoId() { return instituicaoCatalogo != null ? instituicaoCatalogo.getId() : null; }
    public Integer getAno() { return ano; }
    public String getCargo() { return cargo; }
    public String getNivel() { return nivel; }
    public String getModalidade() { return modalidade; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public Instituicao getInstituicaoCatalogo() { return instituicaoCatalogo; }

    public void setBanca(String banca) { this.banca = banca; }
    public void setInstituicao(String instituicao) { }
    public void setInstituicaoId(Long instituicaoId) { }
    public void setInstituicaoCatalogo(Instituicao instituicaoCatalogo) { this.instituicaoCatalogo = instituicaoCatalogo; }
    public void setAno(Integer ano) { this.ano = ano; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }
}