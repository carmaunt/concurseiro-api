package br.com.concurseiro.api.catalogo.assunto.model;

import br.com.concurseiro.api.catalogo.disciplina.model.Disciplina;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "assuntos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assuntos_disciplina_nome",
                columnNames = {"disciplina_id", "nome"}
        )
)
public class Assunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private Disciplina disciplina;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public Long getId() { return id; }

    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(OffsetDateTime criadoEm) { this.criadoEm = criadoEm; }
}