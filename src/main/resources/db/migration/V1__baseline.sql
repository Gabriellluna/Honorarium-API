-- ---------- usuarios (substitui auth.users + perfis) -----------------
create table usuarios (
  id          uuid        primary key default gen_random_uuid(),
  email       text        not null unique,
  senha_hash  text        not null,
  nome        text,
  created_at  timestamptz not null default now()
);

-- ---------- tabelas de apoio (seeds entram em V3 com os dados reais) --
create table tipo_servico (
  id         smallint primary key,
  nome       text     not null,
  descricao  text
);

create table forma_pagamento (
  id     smallint primary key,
  nome   text     not null
);

-- ---------- clientes -------------------------------------------------
create table clientes (
  id               uuid          primary key default gen_random_uuid(),
  usuario_id       uuid          not null references usuarios(id) on delete cascade,
  razao_social     text          not null,
  document         varchar(14)   not null,
  valor_honorario  numeric(10,2) not null check (valor_honorario > 0),
  dia_vencimento   smallint      not null check (dia_vencimento between 1 and 31),
  ativo            boolean       not null default true,
  observacoes      text,
  comp_inicio_mes  smallint      not null default extract(month from current_date)::smallint
                                 check (comp_inicio_mes between 1 and 12),
  comp_inicio_ano  smallint      not null default extract(year from current_date)::smallint
                                 check (comp_inicio_ano between 2000 and 2100),
  created_at       timestamptz   not null default now(),
  updated_at       timestamptz   not null default now()
);

create index idx_clientes_usuario_id on clientes (usuario_id);
create index idx_clientes_ativo      on clientes (ativo);
create index idx_clientes_razao_social
  on clientes using gin (to_tsvector('portuguese', razao_social));

-- ATENÇÃO (mudança de regra): no Supabase o índice único era GLOBAL
-- (document) where ativo. Num sistema multiusuário isso impede dois usuários
-- de cadastrarem o mesmo CNPJ e ainda revela que ele existe. Aqui a
-- unicidade é por usuário. Se você quiser manter o comportamento antigo,
-- troque por: (document) where ativo = true.
create unique index idx_clientes_usuario_document_ativo
  on clientes (usuario_id, document) where ativo = true;

-- ---------- servicos (cobranças) -----------------------------------
create table servicos (
  id               uuid          primary key default gen_random_uuid(),
  cliente_id       uuid          not null references clientes(id) on delete cascade,
  type             smallint      not null default 1 references tipo_servico(id),
  competencia_mes  smallint      not null check (competencia_mes between 1 and 12),
  competencia_ano  smallint      not null check (competencia_ano between 2000 and 2100),
  valor_cobrado    numeric(10,2) not null check (valor_cobrado > 0),
  data_vencimento  date          not null,
  data_pagamento   date,
  status           text          not null default 'pendente'
                                 check (status in ('pendente','pago','atrasado')),
  observacao       text,
  created_at       timestamptz   not null default now(),
  updated_at       timestamptz   not null default now()
);

create index idx_servicos_cliente_id  on servicos (cliente_id);
create index idx_servicos_type        on servicos (type);
create index idx_servicos_status      on servicos (status);
create index idx_servicos_competencia on servicos (competencia_ano, competencia_mes);

-- Só UMA cobrança de honorário (type = 1) por cliente/competência.
-- Serviços avulsos (type = 2) podem repetir: os dados reais já têm isso.
create unique index idx_servicos_honorario_unico
  on servicos (cliente_id, competencia_mes, competencia_ano) where type = 1;

-- ---------- pagamentos (recebimentos) -----------------------
create table pagamentos (
  id              uuid          primary key default gen_random_uuid(),
  servico_id    uuid          not null references servicos(id) on delete cascade,
  valor           numeric(10,2) not null check (valor > 0),
  data_pagamento  date          not null default current_date,
  forma_id        smallint      not null references forma_pagamento(id),
  observacao      text,
  created_at      timestamptz   not null default now()
);

create index idx_pc_pagamento_id on pagamentos (servico_id);
create index idx_pc_data         on pagamentos (data_pagamento);
