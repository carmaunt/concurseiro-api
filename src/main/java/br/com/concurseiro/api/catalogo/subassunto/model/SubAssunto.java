package br.com.concurseiro.api.catalogo.subassunto.model;

import br.com.concurseiro.api.catalogo.assunto.model.Assunto;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "subassuntos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_subassuntos_assunto_nome",
                columnNames = {"assunto_id", "nome"}
        )
)
public class SubAssunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public Long getId() { return id; }

    public Assunto getAssunto() { return assunto; }
    public void setAssunto(Assunto assunto) { this.assunto = assunto; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(OffsetDateTime criadoEm) { this.criadoEm = criadoEm; }
}