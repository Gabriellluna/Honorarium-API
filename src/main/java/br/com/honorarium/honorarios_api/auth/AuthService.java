package br.com.honorarium.honorarios_api.auth;

import br.com.honorarium.honorarios_api.common.CredenciaisInvalidasException;
import br.com.honorarium.honorarios_api.common.SessaoInvalidaException;
import br.com.honorarium.honorarios_api.usuario.Usuario;
import br.com.honorarium.honorarios_api.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.tokens = tokens;
        // hash válido de uma senha aleatória, usado quando o e-mail não existe
        this.hashFicticio = encoder.encode(UUID.randomUUID().toString());
    }

    public TokenResponse login(LoginRequest req) {
        Usuario usuario = autenticar(req.email(), req.senha());
        return new TokenResponse(tokens.gerarAccessToken(usuario), "Bearer", tokens.expiraEmSegundos());
    }

    public MeResponse me(UUID usuarioId) {
        Usuario u = usuarios.findById(usuarioId).orElseThrow(SessaoInvalidaException::new);
        return new MeResponse(u.getId(), u.getEmail(), u.getNome());
    }

    private Usuario autenticar(String email, String senha) {
        Optional<Usuario> encontrado = usuarios.findByEmail(email.trim().toLowerCase());
        // Sempre roda o BCrypt, mesmo com e-mail inexistente: o tempo de resposta
        // não revela quais e-mails estão cadastrados (enumeração de usuários).
        String hash = encontrado.map(Usuario::getSenhaHash).orElse(hashFicticio);
        boolean senhaOk = encoder.matches(senha, hash);
        if (encontrado.isEmpty() || !senhaOk) {
            throw new CredenciaisInvalidasException(); // mesma mensagem para os dois casos
        }
        return encontrado.get();
    }
}
