package br.com.concurseiro.api.prova.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "provas")
public class Prova {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String banca;

    @Column(nullable = false, length = 200)
    private String instituicao;

    @Column(name = "instituicao_id")
    private Long instituicaoId;

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
    public String getInstituicao() { return instituicao; }
    public Long getInstituicaoId() { return instituicaoId; }
    public Integer getAno() { return ano; }
    public String getCargo() { return cargo; }
    public String getNivel() { return nivel; }
    public String getModalidade() { return modalidade; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }

    public void setBanca(String banca) { this.banca = banca; }
    public void setInstituicao(String instituicao) { this.instituicao = instituicao; }
    public void setInstituicaoId(Long instituicaoId) { this.instituicaoId = instituicaoId; }
    public void setAno(Integer ano) { this.ano = ano; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }
}
