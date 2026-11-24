package br.com.honorarium.honorarios_api.tipo_servico;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoServicoService {

    private TipoServicoRepository tipoServicoRepository;

    public TipoServicoService(TipoServicoRepository tipoServicoRepository) {
        this.tipoServicoRepository = tipoServicoRepository;
    }

    public List<TipoServico> buscarTipoServico() {
    System.out.println("ai pai para: " + tipoServicoRepository.findAll());
    return tipoServicoRepository.findAll();
    }

}
