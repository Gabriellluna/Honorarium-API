package br.com.honorarium.honorarios_api.servicos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicosRepository extends JpaRepository<UUID, Servicos> {

    List<Servicos> findByClienteId(UUID clienteId);

}
