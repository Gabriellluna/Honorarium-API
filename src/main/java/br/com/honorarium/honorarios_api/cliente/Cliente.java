package br.com.honorarium.honorarios_api.cliente;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, unique = false)
    private UUID usuarioId;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "document", nullable = false, length = 14)
    private String document;

    @Column(name = "valor_honorario", nullable = false)
    private BigDecimal valorHonorario;

    @Column(name = "dia_vencimento", nullable = false)
    private Short diaVencimento;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "observacoes", nullable = true)
    private String observacoes;

    @Column(name = "comp_inicio_mes", nullable = false)
    private Short compInicioMes;

    @Column(name = "comp_inicio_ano", nullable = false)
    private Short compInicioAno;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Instant updatedAt;

    protected Cliente() { } // exigido pelo JPA

    public Cliente( UUID usuarioId, String razaoSocial, String document, BigDecimal valorHonorario, Short diaVencimento,
        String observacoes, Short compInicioMes, Short compInicioAno) {
        this.usuarioId = usuarioId;
        this.razaoSocial = razaoSocial;
        this.document = document;
        this.valorHonorario = valorHonorario;
        this.diaVencimento = diaVencimento;
        this.ativo = true;
        this.observacoes = observacoes;
        this.compInicioMes = compInicioMes;
        this.compInicioAno = compInicioAno;
    }

    public UUID getId() {return id;}
    public UUID getUsuarioId() {return usuarioId;}
    public String getRazaoSocial() {return razaoSocial;}
    public String getDocument() {return document;}
    public BigDecimal getValorHonorario() {return valorHonorario;}
    public Short getDiaVencimento() {return diaVencimento;}
    public Boolean getAtivo() {return ativo;}
    public String getObservacoes() {return observacoes;}
    public Short getCompInicioMes() {return compInicioMes;}
    public Short getCompInicioAno() {return compInicioAno;}
    public Instant getCreatedAt() {return createdAt;}
    public Instant getUpdatedAt() {return updatedAt;}
}
