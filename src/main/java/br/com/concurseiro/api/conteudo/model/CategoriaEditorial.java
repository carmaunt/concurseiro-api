package br.com.concurseiro.api.conteudo.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "categorias_editoriais")
public class CategoriaEditorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 140)
    private String slug;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTaxonomia status = StatusTaxonomia.ATIVA;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    @PreUpdate
    void normalize() {
        updatedAt = OffsetDateTime.now();
        if (createdAt == null) createdAt = updatedAt;
        if (status == null) status = StatusTaxonomia.ATIVA;
        nome = nome == null ? null : nome.trim();
        slug = ConteudoPortal.gerarSlug(slug == null || slug.isBlank() ? nome : slug);
        descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public StatusTaxonomia getStatus() { return status; }
    public void setStatus(StatusTaxonomia status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
