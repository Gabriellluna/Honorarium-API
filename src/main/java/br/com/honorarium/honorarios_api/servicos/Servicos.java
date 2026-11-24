package br.com.honorarium.honorarios_api.servicos;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "servicos")
public class Servicos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "cliente_id", nullable = false, unique = false)
    private UUID clienteId;

    @Column(name = "type", nullable = false, unique = false)
    private Short tipo;

    @Column(name = "competencia_mes", nullable = false)
    private Short compMes;

    @Column(name = "competencia_ano", nullable = false)
    private Short compAno;

    @Column(name = "valor_cobrado", nullable = false)
    private Double valorCobrado;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDateTime dataVencimento;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDateTime dataPagamento;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "observacao", nullable = true)
    private String observacao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public Short getTipo() {
        return tipo;
    }

    public Short getCompMes() {
        return compMes;
    }

    public Short getCompAno() {
        return compAno;
    }

    public Double getValorCobrado() {
        return valorCobrado;
    }

    public LocalDateTime getDataVencimento() {
        return dataVencimento;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public String getStatus() {
        return status;
    }

    public String getObservacao() {
        return observacao;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Instant updatedAt;

}
