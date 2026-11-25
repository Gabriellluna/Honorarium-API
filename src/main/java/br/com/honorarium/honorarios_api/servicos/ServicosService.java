package br.com.honorarium.honorarios_api.servicos;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServicosService {

    private final ServicosRepository servicosRepository;

    public ServicosService(ServicosRepository servicosRepository) {
        this.servicosRepository = servicosRepository;
    }

    public void buscaServicosPorCliente(UUID clienteId){
        List<Servicos> servicos  = servicosRepository.findByClienteId(clienteId);
        System.out.println("servicos: " + servicos);
    }

}
