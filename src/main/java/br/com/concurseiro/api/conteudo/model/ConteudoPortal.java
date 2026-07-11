package br.com.concurseiro.api.conteudo.model;

import jakarta.persistence.*;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(
        name = "conteudos_portal",
        indexes = {
                @Index(name = "idx_conteudos_portal_tipo_status_publicado", columnList = "tipo,status,publicado_em"),
                @Index(name = "idx_conteudos_portal_slug", columnList = "slug"),
                @Index(name = "idx_conteudos_portal_destaque", columnList = "destaque")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_conteudos_portal_tipo_slug", columnNames = {"tipo", "slug"})
        }
)
public class ConteudoPortal {

    public enum Tipo {
        NOTICIA,
        BLOG,
        CONCURSO_ABERTO,
        EDITAL_PREVISTO
    }

    public enum Status {
        RASCUNHO,
        PUBLICADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(nullable = false, length = 220)
    private String slug;

    @Column(nullable = false, length = 500)
    private String resumo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "imagem_capa", length = 1000)
    private String imagemCapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaEditorial categoria;

    @ManyToMany
    @JoinTable(
            name = "conteudos_portal_tags",
            joinColumns = @JoinColumn(name = "conteudo_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @OrderBy("nome ASC")
    private Set<TagEditorial> tags = new LinkedHashSet<>();

    @Column(name = "categoria", insertable = false, updatable = false, length = 120)
    private String categoriaLegada;

    @Column(name = "tags", insertable = false, updatable = false, columnDefinition = "TEXT")
    private String tagsLegadas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Status status = Status.RASCUNHO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Tipo tipo;

    @Column(nullable = false)
    private boolean destaque = false;

    @Column(name = "publicado_em")
    private OffsetDateTime publicadoEm;

    @Column(name = "seo_titulo", length = 180)
    private String seoTitulo;

    @Column(name = "seo_descricao", length = 300)
    private String seoDescricao;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    @PreUpdate
    void normalize() {
        updatedAt = OffsetDateTime.now();
        if (createdAt == null) createdAt = updatedAt;
        if (status == null) status = Status.RASCUNHO;
        if (slug == null || slug.isBlank()) slug = gerarSlug(titulo);
        slug = gerarSlug(slug);
        if (status == Status.PUBLICADO && publicadoEm == null) {
            publicadoEm = updatedAt;
        }
        if (status == Status.RASCUNHO) {
            publicadoEm = null;
        }
    }

    public static String gerarSlug(String value) {
        if (value == null || value.isBlank()) return "conteudo";

        String normalized = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (normalized.isBlank()) return "conteudo";
        return normalized.length() > 220 ? normalized.substring(0, 220).replaceAll("-+$", "") : normalized;
    }

    public Long getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getResumo() { return resumo; }
    public void setResumo(String resumo) { this.resumo = resumo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getImagemCapa() { return imagemCapa; }
    public void setImagemCapa(String imagemCapa) { this.imagemCapa = imagemCapa; }

    public CategoriaEditorial getCategoria() { return categoria; }
    public void setCategoria(CategoriaEditorial categoria) { this.categoria = categoria; }

    public Set<TagEditorial> getTags() { return tags; }
    public void setTags(Set<TagEditorial> tags) { this.tags = tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags); }

    public String getCategoriaLegada() { return categoriaLegada; }
    public String getTagsLegadas() { return tagsLegadas; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public boolean isDestaque() { return destaque; }
    public void setDestaque(boolean destaque) { this.destaque = destaque; }

    public OffsetDateTime getPublicadoEm() { return publicadoEm; }
    public void setPublicadoEm(OffsetDateTime publicadoEm) { this.publicadoEm = publicadoEm; }

    public String getSeoTitulo() { return seoTitulo; }
    public void setSeoTitulo(String seoTitulo) { this.seoTitulo = seoTitulo; }

    public String getSeoDescricao() { return seoDescricao; }
    public void setSeoDescricao(String seoDescricao) { this.seoDescricao = seoDescricao; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
