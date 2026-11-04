-- =====================================================================
-- V3__seed_tabelas_apoio.sql
-- Dados fixos (lookup) copiados do Supabase. Os IDs são importantes:
-- o código e os dados de pagamentos referenciam esses números.
--   tipo_pagamento: 1 = Honorário (recorrente), 2 = Serviço Avulso
-- =====================================================================
insert into tipo_pagamento (id, nome, descricao) values
  (1, 'Honorário',      'Mensalidade recorrente de honorários contábeis'),
  (2, 'Serviço Avulso', 'Serviço pontual não recorrente');

insert into forma_pagamento (id, nome, icone) values
  (1,  'Pix',                    '⚡'),
  (2,  'Dinheiro',               '💵'),
  (3,  'Cartão de Crédito',      '💳'),
  (4,  'Cartão de Débito',       '💳'),
  (5,  'Boleto Bancário',        '📄'),
  (6,  'Transferência Bancária', '🏦'),
  (7,  'TED / DOC',              '🏦'),
  (8,  'Cheque',                 '📝'),
  (9,  'Parcelamento no Cartão', '💳'),
  (10, 'Outro',                  '💰');
