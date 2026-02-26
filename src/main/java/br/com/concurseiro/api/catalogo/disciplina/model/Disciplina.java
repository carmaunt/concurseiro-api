package br.com.concurseiro.api.catalogo.disciplina.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "disciplinas",
        uniqueConstraints = @UniqueConstraint(name = "uk_disciplinas_nome", columnNames = "nome")
)
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(OffsetDateTime criadoEm) { this.criadoEm = criadoEm; }
}