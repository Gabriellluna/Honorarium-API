package br.com.honorarium.honorarios_api.tipo_servico;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tipo_servico")
public class TipoServicoController {

    private TipoServicoService tipoServicoService;

    public TipoServicoController(TipoServicoService tipoServicoService) {
        this.tipoServicoService = tipoServicoService;
    }

    @GetMapping("")
    public List<TipoServico> tipos() {
        List<TipoServico> tipos = tipoServicoService.buscarTipoServico();
        System.out.println("O que veio daqui: " + tipos.toString());
        return tipos;
    }

}
