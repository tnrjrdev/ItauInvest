-- ============================================================================
-- ItauInvest - Schema inicial
-- ============================================================================

CREATE TABLE tb_user (
    id            UUID                     NOT NULL,
    name          VARCHAR(120)             NOT NULL,
    email         VARCHAR(180)             NOT NULL,
    cpf           VARCHAR(11)              NOT NULL,
    password_hash VARCHAR(100)             NOT NULL,
    role          VARCHAR(20)              NOT NULL,
    active        BOOLEAN                  NOT NULL,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_user_email ON tb_user (email);
CREATE UNIQUE INDEX ux_user_cpf ON tb_user (cpf);

CREATE TABLE tb_wallet (
    id           UUID                     NOT NULL,
    user_id      UUID                     NOT NULL,
    cash_balance NUMERIC(19, 4)           NOT NULL,
    version      BIGINT                   NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_wallet PRIMARY KEY (id),
    CONSTRAINT uq_wallet_user UNIQUE (user_id),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES tb_user (id)
);

CREATE TABLE tb_product (
    id                  UUID                     NOT NULL,
    name                VARCHAR(140)             NOT NULL,
    type                VARCHAR(30)              NOT NULL,
    risk_level          VARCHAR(10)              NOT NULL,
    annual_rate_percent NUMERIC(9, 4)            NOT NULL,
    minimum_amount      NUMERIC(19, 4)           NOT NULL,
    liquidity_days      INTEGER                  NOT NULL,
    maturity_date       DATE,
    active              BOOLEAN                  NOT NULL,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id)
);

CREATE TABLE tb_investment (
    id              UUID                     NOT NULL,
    user_id         UUID                     NOT NULL,
    product_id      UUID                     NOT NULL,
    invested_amount NUMERIC(19, 4)           NOT NULL,
    status          VARCHAR(15)              NOT NULL,
    applied_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    version         BIGINT                   NOT NULL,
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_investment PRIMARY KEY (id),
    CONSTRAINT fk_investment_user FOREIGN KEY (user_id) REFERENCES tb_user (id),
    CONSTRAINT fk_investment_product FOREIGN KEY (product_id) REFERENCES tb_product (id)
);
CREATE INDEX ix_investment_user ON tb_investment (user_id);
CREATE INDEX ix_investment_product ON tb_investment (product_id);

CREATE TABLE tb_transaction (
    id            UUID                     NOT NULL,
    wallet_id     UUID                     NOT NULL,
    investment_id UUID,
    type          VARCHAR(15)              NOT NULL,
    amount        NUMERIC(19, 4)           NOT NULL,
    balance_after NUMERIC(19, 4)           NOT NULL,
    description   VARCHAR(255),
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_transaction PRIMARY KEY (id),
    CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES tb_wallet (id),
    CONSTRAINT fk_transaction_investment FOREIGN KEY (investment_id) REFERENCES tb_investment (id)
);
CREATE INDEX ix_transaction_wallet ON tb_transaction (wallet_id);
CREATE INDEX ix_transaction_created_at ON tb_transaction (created_at);
