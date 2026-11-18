package br.com.honorarium.honorarios_api.cliente;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String razaoSocial,
        String document,
        BigDecimal valorHonorario,
        Short diaVencimento,
        Boolean ativo,
        String observacoes,
        Short compInicioMes,
        Short compInicioAno,
        Instant createdAt
        ) {
}
