package br.com.honorarium.honorarios_api.servicos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicosRepository extends JpaRepository<Servicos, UUID> {

    Optional<Servicos> findByClienteUsuarioId(UUID usuarioId);

}
