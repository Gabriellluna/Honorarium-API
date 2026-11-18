package br.com.honorarium.honorarios_api.cliente;

import br.com.honorarium.honorarios_api.common.NenhumClienteException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponse buscarCliente(UUID id, UUID usuarioId) {
        Optional<Cliente> cliente = clienteRepository.findByIdAndUsuarioId(id, usuarioId);
        if(cliente.isEmpty()) {
            throw new NenhumClienteException();
        }
        Cliente c = cliente.get();
        return new ClienteResponse(c.getId(), c.getRazaoSocial(), c.getDocument(),
                c.getValorHonorario(), c.getDiaVencimento(), c.getAtivo(),
                c.getObservacoes(), c.getCompInicioMes(), c.getCompInicioAno(),
                c.getCreatedAt());
    }
}
