package br.com.concurseiro.api.questoes.textoapoio.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "textos_apoio", indexes = {
        @Index(name = "idx_textos_apoio_hash_sha256", columnList = "hash_sha256")
})
public class TextoApoio {

    public enum Tipo {
        TEXTO,
        CODIGO,
        TABELA,
        IMAGEM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Tipo tipo = Tipo.TEXTO;

    @Lob
    @Column(nullable = false)
    private String conteudo;

    @Column(name = "conteudo_json", columnDefinition = "text")
    private String conteudoJson;

    @Column(name = "hash_sha256", nullable = false, unique = true, length = 64)
    private String hashSha256;

    @Column(nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo == null ? Tipo.TEXTO : tipo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getConteudoJson() { return conteudoJson; }
    public void setConteudoJson(String conteudoJson) { this.conteudoJson = conteudoJson; }

    public String getHashSha256() { return hashSha256; }
    public void setHashSha256(String hashSha256) { this.hashSha256 = hashSha256; }

    public OffsetDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(OffsetDateTime criadoEm) { this.criadoEm = criadoEm; }
}
