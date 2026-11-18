package br.com.honorarium.honorarios_api.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends  JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByIdAndUsuarioId(UUID id, UUID usuarioId);

}
