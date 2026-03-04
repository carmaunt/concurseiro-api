package br.com.concurseiro.api.comentario;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "questao_id", nullable = false)
    private String questaoId;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false)
    private Integer curtidas = 0;

    @Column(nullable = false)
    private Integer descurtidas = 0;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public Comentario() {}

    public Comentario(String questaoId, String autor, String texto) {
        this.questaoId = questaoId;
        this.autor = autor;
        this.texto = texto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestaoId() { return questaoId; }
    public void setQuestaoId(String questaoId) { this.questaoId = questaoId; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public Integer getCurtidas() { return curtidas; }
    public void setCurtidas(Integer curtidas) { this.curtidas = curtidas; }

    public Integer getDescurtidas() { return descurtidas; }
    public void setDescurtidas(Integer descurtidas) { this.descurtidas = descurtidas; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(OffsetDateTime criadoEm) { this.criadoEm = criadoEm; }
}