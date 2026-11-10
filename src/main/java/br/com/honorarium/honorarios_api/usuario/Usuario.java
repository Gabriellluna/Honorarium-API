package br.com.honorarium.honorarios_api.usuario;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    private String nome;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Usuario() { } // exigido pelo JPA

    public Usuario(String email, String senhaHash, String nome) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.nome = nome;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public String getNome() { return nome; }
    public Instant getCreatedAt() { return createdAt; }
}
