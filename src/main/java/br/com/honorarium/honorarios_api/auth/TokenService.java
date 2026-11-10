package br.com.honorarium.honorarios_api.auth;

import br.com.honorarium.honorarios_api.config.JwtConfig;
import br.com.honorarium.honorarios_api.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final Clock clock;
    private final long minutos;

    public TokenService(JwtEncoder encoder, Clock clock,
                        @Value("${jwt.access-token-minutes}") long minutos) {
        this.encoder = encoder;
        this.clock = clock;
        this.minutos = minutos;
    }

    public String gerarAccessToken(Usuario usuario) {
        Instant agora = Instant.now(clock);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(JwtConfig.ISSUER)
            .subject(usuario.getId().toString())   // "sub" = ID do usuário. É daqui que vem o "dono" em toda requisição.
            .issuedAt(agora)
            .expiresAt(agora.plus(minutos, ChronoUnit.MINUTES))
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiraEmSegundos() {
        return minutos * 60;
    }
}
