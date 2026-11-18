package br.com.honorarium.honorarios_api.cliente;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/{id}")
    public ClienteResponse cliente(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        UUID usuarioId = UUID.fromString(jwt.getSubject()); // vem do token validado, nunca do cliente
        return clienteService.buscarCliente(id, usuarioId);
    }
}
