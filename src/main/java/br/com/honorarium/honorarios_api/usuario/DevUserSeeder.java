package br.com.honorarium.honorarios_api.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")   // NUNCA roda sem o profile dev explícito
public class DevUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUserSeeder.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String email;
    private final String senha;

    public DevUserSeeder(UsuarioRepository usuarios, PasswordEncoder encoder,
                         @Value("${app.dev-seed.email}") String email,
                         @Value("${app.dev-seed.password}") String senha) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (usuarios.existsByEmail(email)) return;
        usuarios.save(new Usuario(email, encoder.encode(senha), "Usuário Dev"));
        log.info("Usuário de desenvolvimento criado: {}", email);
    }
}
