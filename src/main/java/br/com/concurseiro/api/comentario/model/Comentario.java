package br.com.concurseiro.api.comentario.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "comentarios", indexes = {
        @Index(name = "idx_comentario_questao_id", columnList = "questao_id")
})
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "questao_id", nullable = false, length = 16)
    private String questaoId;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false)
    private Integer curtidas = 0;

    @Column(nullable = false)
    private Integer descurtidas = 0;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public Long getId() { return id; }
    public String getQuestaoId() { return questaoId; }
    public String getAutor() { return autor; }
    public String getTexto() { return texto; }
    public Integer getCurtidas() { return curtidas; }
    public Integer getDescurtidas() { return descurtidas; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }

    public void setQuestaoId(String questaoId) { this.questaoId = questaoId; }
    public void setAutor(String autor) { this.autor = autor; }
    public void setTexto(String texto) { this.texto = texto; }
    public void setCurtidas(Integer curtidas) { this.curtidas = curtidas; }
    public void setDescurtidas(Integer descurtidas) { this.descurtidas = descurtidas; }
}
