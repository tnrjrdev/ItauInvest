-- ============================================================================
-- ItauInvest - Seed do catalogo de produtos
-- O usuario ADMIN e um cliente demo sao provisionados via DataInitializer
-- (precisam de hash BCrypt gerado pela aplicacao).
-- ============================================================================

INSERT INTO tb_product
    (id, name, type, risk_level, annual_rate_percent, minimum_amount, liquidity_days, maturity_date, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'CDB Liquidez Diaria 102% CDI', 'CDB', 'BAIXO', 10.7100, 100.0000, 0, NULL, TRUE, now(), now()),
    (gen_random_uuid(), 'CDB Itau 110% CDI 2 anos', 'CDB', 'BAIXO', 11.5500, 1000.0000, 720, '2027-06-01', TRUE, now(), now()),
    (gen_random_uuid(), 'Tesouro Selic 2029', 'TESOURO_DIRETO', 'BAIXO', 10.5000, 100.0000, 1, '2029-03-01', TRUE, now(), now()),
    (gen_random_uuid(), 'Tesouro IPCA+ 2035', 'TESOURO_DIRETO', 'MEDIO', 12.2000, 100.0000, 1, '2035-05-15', TRUE, now(), now()),
    (gen_random_uuid(), 'LCI Imobiliaria 95% CDI', 'LCI_LCA', 'BAIXO', 9.9700, 5000.0000, 90, '2026-12-31', TRUE, now(), now()),
    (gen_random_uuid(), 'Fundo Multimercado Itau Hedge', 'FUNDO', 'MEDIO', 14.0000, 500.0000, 30, NULL, TRUE, now(), now()),
    (gen_random_uuid(), 'Fundo Acoes Itau Indice', 'FUNDO', 'ALTO', 18.5000, 500.0000, 30, NULL, TRUE, now(), now()),
    (gen_random_uuid(), 'Acao ITUB4', 'ACAO', 'ALTO', 16.0000, 50.0000, 0, NULL, TRUE, now(), now());
