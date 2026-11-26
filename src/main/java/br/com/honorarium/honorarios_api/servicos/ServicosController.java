package br.com.honorarium.honorarios_api.servicos;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("servicos")
public class ServicosController {

    private final  ServicosService servicosService;

    public ServicosController(ServicosService servicosService) {
        this.servicosService = servicosService;
    }

    @GetMapping("")
    public void listaServicos() {

    }

}
