package br.com.concurseiro.api.catalogo; 
import jakarta.persistence.*; 

@Entity 
@Table(
    name = "catalogo_instituicoes", uniqueConstraints = { 
        @UniqueConstraint(
            name = "uk_catalogo_instituicoes_nome", columnNames = "nome"
        ) 
    }
) 
public class Instituicao {
     @Id @GeneratedValue(
        strategy = GenerationType.IDENTITY
    ) 
    private Long id; 
    @Column(
        nullable = false, length = 200
    ) 
    private String nome; 
    public Long getId() { 
        return id; 
    } 
    public String getNome() { 
        return nome; 
    } 
    public void setNome(String nome) { 
        this.nome = nome; 
    } 
}