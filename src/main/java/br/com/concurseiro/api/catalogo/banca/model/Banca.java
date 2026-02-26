package br.com.concurseiro.api.catalogo.banca.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bancas", uniqueConstraints = @UniqueConstraint(columnNames = "nome"))
public class Banca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160, unique = true)
    private String nome;

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}