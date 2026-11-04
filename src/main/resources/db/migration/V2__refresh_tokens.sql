-- =====================================================================
-- V2__refresh_tokens.sql
-- Guarda os refresh tokens (só o HASH, nunca o token puro) para permitir
-- renovar o access token, fazer logout de verdade e revogar sessões.
-- =====================================================================
create table refresh_tokens (
  id          uuid        primary key default gen_random_uuid(),
  usuario_id  uuid        not null references usuarios(id) on delete cascade,
  token_hash  text        not null unique,
  expira_em   timestamptz not null,
  revogado    boolean     not null default false,
  created_at  timestamptz not null default now()
);

create index idx_refresh_tokens_usuario_id on refresh_tokens (usuario_id);
