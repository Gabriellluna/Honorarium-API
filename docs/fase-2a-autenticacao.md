# Fase 2A — Autenticação (login com JWT) — Instruções para o agente

> Leia este arquivo inteiro antes de começar. Ele é autossuficiente.
> O dono do projeto está **aprendendo Java e Spring**. O objetivo não é só
> terminar, é que ele entenda. Siga as regras de conduta abaixo.

---

## 0. Regras de conduta (obrigatórias)

1. **Ambiente:** Windows + PowerShell. Raiz do repositório: `C:\dev\honorarios-api`
   (a pasta que contém o `pom.xml`). Use `.\mvnw.cmd` (não dependa de Maven global).
2. **Explique em português.** Ao fim de cada passo, escreva um resumo curto
   (máx. ~10 linhas): o que foi criado, **por que**, e como as peças se conectam.
   Sem jargão solto: se usar um termo novo (bean, filtro, claim...), defina em uma frase.
3. **Um commit por passo**, com mensagem clara em português. Não junte passos.
4. **NUNCA** faça sem pedir confirmação ao usuário:
    - `docker compose down -v` (apaga os dados do banco)
    - `git push --force`, `git reset --hard`, reescrever histórico
    - editar ou renomear `V1__baseline.sql`, `V2__refresh_tokens.sql`, `V3__seed_tabelas_apoio.sql`
      (o Flyway guarda o checksum; editar uma migration já aplicada impede a aplicação de subir.
      Mudança de schema = **novo arquivo** `V4__...`)
5. **NUNCA** conecte no Supabase, nem leia/escreva dados de lá. Esta fase usa só o Postgres local (Docker).
6. **Segredos:** nada de senha, chave ou token reais em arquivos versionados.
   Os valores de `application-dev.yml` são **só para desenvolvimento local** e são descartáveis.
7. **Sem dependências novas** no `pom.xml` sem avisar e justificar. **Sem Lombok**
   (o dono quer escrever getters à mão e usar `record` nos DTOs).
8. **Spring Boot 4.1.x / Spring Security 7 / Hibernate 7.** Os trechos abaixo foram escritos
   a partir de Boot 3.x e **não foram compilados**. Se algum não compilar ou uma API tiver mudado,
   consulte a documentação oficial da versão em uso, adapte, e **registre a diferença no relatório final**.
   Nunca "resolva" um erro desligando segurança ou validações.
9. **Escopo:** faça só o que está aqui. Fora de escopo: refresh token, cadastro público
   (`/auth/register`), endpoints de clientes/pagamentos, Testcontainers, rate limiting.
10. Se algo falhar e você não souber resolver em 2 tentativas, **pare e reporte** o erro completo.

---

## 1. Contexto do projeto

Sistema de controle de honorários (contabilidade). O backend atual está no Supabase
(Postgres + auth + RLS) e está sendo reescrito como **API Spring Boot + Postgres próprio**,
consumida pelo mesmo frontend React.

| Item | Decisão |
|---|---|
| Pacote base | `br.com.honorarium.honorarios_api` |
| Java / Boot | Java 25, Spring Boot 4.1.2, Maven |
| Banco local | Postgres 16 no Docker (`docker-compose.yml`), db `honorarios`, user/senha `app`/`app` |
| Schema | Flyway: `src/main/resources/db/migration/V1..V3` (já aplicadas, tabelas: `usuarios`, `clientes`, `pagamentos`, `pagamentos_clientes`, `refresh_tokens`, `tipo_pagamento`, `forma_pagamento`) |
| JPA | `ddl-auto: validate` (o Hibernate só confere; quem cria o schema é o Flyway) |
| Auth | JWT próprio, assinado com HS256, emitido por esta API |
| Cadastro | **Não existe cadastro público.** Hoje são só 3 usuários (migrados depois, por script) |
| Regra de ouro | **O ID do usuário vem sempre do token validado, nunca do corpo, URL ou query.** |

Tabela `usuarios` (V1): `id uuid pk default gen_random_uuid()`, `email text unique not null`,
`senha_hash text not null`, `nome text`, `created_at timestamptz not null default now()`.

---

## 2. Passo 0 — Pré-verificação (sem alterar nada)

Execute e **reporte o resultado**:

```powershell
cd C:\dev\honorarios-api
git status
git remote -v
git log --oneline -5
docker compose up -d
docker compose ps
.\mvnw.cmd -v
.\mvnw.cmd compile
```

Critérios para prosseguir:
- `git status` limpo (ou só com mudanças esperadas) e remote `origin` configurado.
  Se não houver `origin`, **pare e peça a URL do repositório ao usuário**.
- `docker compose ps` mostra `honorarios-db` como `Up`.
- `mvnw compile` termina em `BUILD SUCCESS`.

Depois suba a aplicação uma vez para confirmar a base:

```powershell
.\mvnw.cmd spring-boot:run
```

Esperado no log: `Successfully validated 3 migrations` (ou `Schema ... is up to date`)
e `Started HonorariosApiApplication`. Pare com `Ctrl+C`.

---

## 3. Passo 1 — Entity e Repository de `Usuario`

Crie o pacote `br.com.honorarium.honorarios_api.usuario` com dois arquivos.

**`Usuario.java`**

```java
package br.com.honorarium.honorarios_api.usuario;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    private String nome;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Usuario() { } // exigido pelo JPA

    public Usuario(String email, String senhaHash, String nome) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.nome = nome;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public String getNome() { return nome; }
    public Instant getCreatedAt() { return createdAt; }
}
```

**`UsuarioRepository.java`**

```java
package br.com.honorarium.honorarios_api.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
```

Observação para a explicação ao usuário: não há setters de propósito (o objeto nasce completo
pelo construtor); o Spring Data gera a implementação do repository a partir do nome dos métodos.

**Verificação:** `.\mvnw.cmd spring-boot:run` — como o `ddl-auto` é `validate`, se a entity
não bater com a tabela, a aplicação **não sobe** e o erro diz qual coluna divergiu.
Esperado: sobe normalmente.

**Commit:** `Adiciona entity Usuario e UsuarioRepository`

---

## 4. Passo 2 — Núcleo de segurança (BCrypt, JWT, filtro)

### 4.1 Configuração (`application.yml`)

Acrescente (mantendo o que já existe):

```yaml
jwt:
  secret: ${JWT_SECRET}          # sem valor padrão de propósito: em produção vem de variável de ambiente
  access-token-minutes: 15

app:
  cors:
    allowed-origins: ${CORS_ORIGINS:https://controle-de-honorarios.vercel.app,http://localhost:5173}

management:
  endpoints:
    web:
      exposure:
        include: health
```

Crie **`src/main/resources/application-dev.yml`** (só desenvolvimento local):

```yaml
# Valores SÓ para desenvolvimento local. Produção usa variáveis de ambiente.
jwt:
  secret: dev-only-secret-change-me-0123456789abcdef-NOT-FOR-PRODUCTION

app:
  dev-seed:
    email: dev@honorarios.local
    password: dev12345
```

Explique ao usuário: sem o profile `dev`, a aplicação **falha ao subir** por falta de `JWT_SECRET`.
Isso é intencional (seguro por padrão: ninguém sobe produção com chave de teste por esquecimento).
A chave HS256 precisa ter **pelo menos 32 bytes**.

### 4.2 Pacote `config`

**`JwtConfig.java`**

```java
package br.com.honorarium.honorarios_api.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    public static final String ISSUER = "honorarios-api";

    @Bean
    SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                "jwt.secret precisa ter pelo menos 32 bytes (256 bits) para HS256");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
        // valida assinatura + expiração (padrão) + emissor
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        return decoder;
    }
}
```

**`SecurityConfig.java`**

```java
package br.com.honorarium.honorarios_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Clock;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())   // API stateless com Bearer token: sem cookie de sessão, sem CSRF
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> origins) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origins);                       // origens explícitas, nunca "*"
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }
}
```

### 4.3 Pacote `auth` — `TokenService.java`

```java
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
```

**Verificação:** `.\mvnw.cmd compile` OK. Não suba ainda sem profile (falharia por falta de `JWT_SECRET`, como esperado).
Para subir: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"`. A aplicação deve subir.
Todas as rotas (exceto `/auth/login` e `/actuator/health`) devem responder **401** sem token.

**Commit:** `Adiciona configuração de segurança, BCrypt e emissão/validação de JWT`

---

## 5. Passo 3 — Login, `/auth/me`, erros e usuário de dev

### 5.1 Exceções e tratamento de erros (pacote `common`)

```java
// common/CredenciaisInvalidasException.java
package br.com.honorarium.honorarios_api.common;
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() { super("Credenciais inválidas"); }
}

// common/SessaoInvalidaException.java
package br.com.honorarium.honorarios_api.common;
public class SessaoInvalidaException extends RuntimeException {
    public SessaoInvalidaException() { super("Sessão inválida"); }
}
```

**`GlobalExceptionHandler.java`** (formato padrão `ProblemDetail`, RFC 9457):

```java
package br.com.honorarium.honorarios_api.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CredenciaisInvalidasException.class, SessaoInvalidaException.class})
    ProblemDetail naoAutenticado(RuntimeException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle("Não autenticado");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos");
        pd.setTitle("Requisição inválida");
        pd.setProperty("campos", campos);
        return pd;
    }
}
```

### 5.2 DTOs (pacote `auth`, todos `record`)

```java
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(max = 72) String senha) {}

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}

public record MeResponse(UUID id, String email, String nome) {}
```

(Imports: `jakarta.validation.constraints.*`, `java.util.UUID`. Um record por arquivo.)

### 5.3 `AuthService.java`

```java
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
```

### 5.4 `AuthController.java`

```java
package br.com.honorarium.honorarios_api.auth;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        UUID usuarioId = UUID.fromString(jwt.getSubject()); // vem do token validado, nunca do cliente
        return auth.me(usuarioId);
    }
}
```

### 5.5 Usuário de desenvolvimento (só com profile `dev`)

Não há cadastro público; para testar localmente é preciso ter um usuário.
**`usuario/DevUserSeeder.java`:**

```java
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
```

### 5.6 Ajuste no teste gerado

O teste `HonorariosApiApplicationTests.contextLoads` sobe o contexto inteiro e agora precisa
de banco e de `JWT_SECRET`. Adicione `@ActiveProfiles("dev")` na classe de teste.
(Ele passa a exigir o Postgres do Docker no ar. Testes com banco descartável via Testcontainers
ficam para a Fase 5; registre isso no relatório.)

### 5.7 Verificações (PowerShell, com a aplicação rodando no profile `dev`)

```powershell
# 1) sem token -> 401
curl.exe -i http://localhost:8080/auth/me

# 2) health público -> 200 {"status":"UP"}
curl.exe -i http://localhost:8080/actuator/health

# 3) login correto -> 200 com accessToken
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/auth/login `
  -ContentType "application/json" `
  -Body (@{ email = "dev@honorarios.local"; senha = "dev12345" } | ConvertTo-Json)
$login
$token = $login.accessToken

# 4) /auth/me com token -> 200 com id, email, nome
Invoke-RestMethod -Uri http://localhost:8080/auth/me -Headers @{ Authorization = "Bearer $token" }

# 5) senha errada -> 401 "Credenciais inválidas"
curl.exe -i -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"email\":\"dev@honorarios.local\",\"senha\":\"errada\"}"

# 6) e-mail inexistente -> 401 com a MESMA mensagem do item 5
curl.exe -i -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"email\":\"naoexiste@x.com\",\"senha\":\"qualquer123\"}"

# 7) corpo inválido -> 400 com o campo "campos"
curl.exe -i -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"email\":\"nao-e-email\",\"senha\":\"\"}"

# 8) token adulterado (troque o último caractere) -> 401
$ruim = $token.Substring(0, $token.Length - 1) + "x"
curl.exe -i http://localhost:8080/auth/me -H "Authorization: Bearer $ruim"
```

Confirme também no banco que a senha **não** está em texto puro (deve começar com `$2a$` ou `$2b$`):

```powershell
docker exec -it honorarios-db psql -U app -d honorarios -c "select email, left(senha_hash, 7) as inicio_do_hash from usuarios;"
```

**Commit:** `Adiciona login com JWT, endpoint /auth/me, tratamento de erros e usuário de dev`

---

## 6. Fechamento

1. Confira que nenhum segredo real foi commitado (`git diff origin/main` antes do push).
   `application-dev.yml` é aceitável: só tem valores de desenvolvimento.
2. `git push`.
3. Entregue o **relatório final** neste formato:

```
## Relatório Fase 2A
### Feito
(lista curta por passo, com hash do commit)
### Verificações
(cada item 1 a 8 do 5.7: status HTTP obtido; e o resultado do hash no banco)
### Diferenças em relação às instruções
(qualquer trecho adaptado por causa do Spring Boot 4 / Security 7, com o motivo)
### Avisos e pendências
(warnings do log, comportamentos estranhos, dúvidas)
```

---

## 7. Notas para a explicação ao usuário (conceitos desta fase)

- **Autenticação x autorização:** provar quem é (login) x decidir o que pode ver (ownership). Esta fase é a primeira.
- **JWT:** texto com 3 partes (cabeçalho, payload, assinatura). O payload é só codificado
  (não é segredo, não coloque nada sensível); a **assinatura** impede alterar o `sub`.
  Por isso "o ID vem do token" é seguro: o cliente não consegue forjá-lo.
- **BCrypt:** guarda hash com salt embutido; nunca se guarda nem compara senha em texto puro.
- **Mesma mensagem e mesmo tempo** para e-mail inexistente e senha errada: evita revelar quais e-mails existem.
- **Stateless:** o servidor não guarda sessão; cada requisição traz o token. Por isso não há CSRF a tratar aqui.
- **`ddl-auto: validate` + Flyway:** quem manda no schema é a migration; a entity é só um espelho conferido na partida.
- **Profile `dev`:** conveniências de desenvolvimento (chave de teste, usuário de teste) que não existem em produção.

Próxima fase (2B, em outro arquivo): refresh token com rotação, logout e cookie `httpOnly`.