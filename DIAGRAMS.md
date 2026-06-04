# Diagramas UML — ItauInvest

Diagramas em [Mermaid](https://mermaid.js.org/) (renderizam no preview do VS Code
e no GitHub). Refletem a implementação atual do código.

Índice:
1. [Diagrama de Casos de Uso](#1-diagrama-de-casos-de-uso)
2. [Diagrama de Classes (Domínio)](#2-diagrama-de-classes-dominio)
3. [Diagrama Entidade-Relacionamento (Banco)](#3-diagrama-entidade-relacionamento-banco)
4. [Diagrama de Componentes (Arquitetura em Camadas)](#4-diagrama-de-componentes-arquitetura-em-camadas)
5. [Sequência — Autenticação (Login)](#5-sequencia--autenticacao-login)
6. [Sequência — Aplicação em Produto](#6-sequencia--aplicacao-em-produto)
7. [Sequência — Resgate](#7-sequencia--resgate)
8. [Máquina de Estados — Investimento](#8-maquina-de-estados--investimento)

---

## 1. Diagrama de Casos de Uso

```mermaid
flowchart LR
    Cliente(["👤 Cliente"])
    Admin(["👤 Administrador"])

    subgraph Sistema["ItauInvest"]
        UC1(["Registrar-se"])
        UC2(["Autenticar (login)"])
        UC3(["Consultar saldo da carteira"])
        UC4(["Depositar / Sacar"])
        UC5(["Consultar extrato"])
        UC6(["Listar produtos"])
        UC7(["Aplicar em produto"])
        UC8(["Resgatar investimento"])
        UC9(["Consultar carteira consolidada"])
        UC10(["Gerenciar produtos (CRUD)"])
    end

    Cliente --- UC1
    Cliente --- UC2
    Cliente --- UC3
    Cliente --- UC4
    Cliente --- UC5
    Cliente --- UC6
    Cliente --- UC7
    Cliente --- UC8
    Cliente --- UC9

    Admin --- UC2
    Admin --- UC6
    Admin --- UC10
```

---

## 2. Diagrama de Classes (Domínio)

```mermaid
classDiagram
    direction LR

    class BaseEntity {
        <<abstract>>
        +UUID id
        +Instant createdAt
        +Instant updatedAt
    }

    class User {
        +String name
        +String email
        +String cpf
        +String passwordHash
        +Role role
        +boolean active
    }

    class Wallet {
        +BigDecimal cashBalance
        +Long version
        +credit(BigDecimal) void
        +debit(BigDecimal) void
        +hasSufficientBalance(BigDecimal) boolean
    }

    class Product {
        +String name
        +ProductType type
        +RiskLevel riskLevel
        +BigDecimal annualRatePercent
        +BigDecimal minimumAmount
        +Integer liquidityDays
        +LocalDate maturityDate
        +boolean active
    }

    class Investment {
        +BigDecimal investedAmount
        +InvestmentStatus status
        +Instant appliedAt
        +Long version
    }

    class Transaction {
        +TransactionType type
        +BigDecimal amount
        +BigDecimal balanceAfter
        +String description
    }

    class Role {
        <<enumeration>>
        ADMIN
        CLIENT
    }
    class ProductType {
        <<enumeration>>
        CDB
        TESOURO_DIRETO
        LCI_LCA
        FUNDO
        ACAO
    }
    class RiskLevel {
        <<enumeration>>
        BAIXO
        MEDIO
        ALTO
    }
    class InvestmentStatus {
        <<enumeration>>
        ACTIVE
        REDEEMED
    }
    class TransactionType {
        <<enumeration>>
        DEPOSIT
        WITHDRAWAL
        APPLICATION
        REDEMPTION
    }

    BaseEntity <|-- User
    BaseEntity <|-- Wallet
    BaseEntity <|-- Product
    BaseEntity <|-- Investment
    BaseEntity <|-- Transaction

    User "1" --> "1" Wallet : possui
    User "1" --> "*" Investment : detem
    Product "1" --> "*" Investment : referenciado por
    Wallet "1" --> "*" Transaction : registra
    Investment "1" --> "*" Transaction : origina

    User ..> Role
    Product ..> ProductType
    Product ..> RiskLevel
    Investment ..> InvestmentStatus
    Transaction ..> TransactionType
```

---

## 3. Diagrama Entidade-Relacionamento (Banco)

```mermaid
erDiagram
    TB_USER ||--|| TB_WALLET : "possui"
    TB_USER ||--o{ TB_INVESTMENT : "detem"
    TB_PRODUCT ||--o{ TB_INVESTMENT : "referenciado"
    TB_WALLET ||--o{ TB_TRANSACTION : "registra"
    TB_INVESTMENT ||--o{ TB_TRANSACTION : "origina"

    TB_USER {
        uuid id PK
        varchar name
        varchar email UK
        varchar cpf UK
        varchar password_hash
        varchar role
        boolean active
        timestamptz created_at
        timestamptz updated_at
    }

    TB_WALLET {
        uuid id PK
        uuid user_id FK "UK"
        numeric cash_balance
        bigint version
        timestamptz created_at
        timestamptz updated_at
    }

    TB_PRODUCT {
        uuid id PK
        varchar name
        varchar type
        varchar risk_level
        numeric annual_rate_percent
        numeric minimum_amount
        integer liquidity_days
        date maturity_date
        boolean active
    }

    TB_INVESTMENT {
        uuid id PK
        uuid user_id FK
        uuid product_id FK
        numeric invested_amount
        varchar status
        timestamptz applied_at
        bigint version
    }

    TB_TRANSACTION {
        uuid id PK
        uuid wallet_id FK
        uuid investment_id FK "nullable"
        varchar type
        numeric amount
        numeric balance_after
        varchar description
        timestamptz created_at
    }
```

---

## 4. Diagrama de Componentes (Arquitetura em Camadas)

```mermaid
flowchart TB
    subgraph FE["Frontend — React + TS"]
        UI["Páginas / Componentes"]
        AX["Axios + Interceptors (JWT)"]
        UI --> AX
    end

    subgraph BE["Backend — Spring Boot"]
        direction TB
        SEC["Security<br/>JwtAuthenticationFilter · SecurityConfig"]
        CTRL["Controllers<br/>Auth · Product · Wallet · Investment"]
        SVC["Services<br/>Auth · Product · Wallet · Investment · Valuation"]
        REPO["Repositories<br/>Spring Data JPA"]
        EXC["GlobalExceptionHandler"]

        SEC --> CTRL
        CTRL --> SVC
        SVC --> REPO
        CTRL -.erros.-> EXC
        SVC -.erros.-> EXC
    end

    DB[("PostgreSQL<br/>Flyway migrations")]

    AX -->|"HTTPS / JSON<br/>Bearer JWT"| SEC
    REPO --> DB
```

---

## 5. Sequência — Autenticação (Login)

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant FE as Frontend
    participant AC as AuthController
    participant AM as AuthenticationManager
    participant UDS as AppUserDetailsService
    participant JWT as JwtService
    participant DB as PostgreSQL

    C->>FE: informa e-mail + senha
    FE->>AC: POST /api/v1/auth/login
    AC->>AM: authenticate(email, senha)
    AM->>UDS: loadUserByUsername(email)
    UDS->>DB: findByEmail(email)
    DB-->>UDS: User
    UDS-->>AM: UserDetails
    AM-->>AC: Authentication (ok)
    AC->>JWT: generateToken(userId, email, role)
    JWT-->>AC: accessToken (HS256)
    AC-->>FE: 200 { accessToken, role, ... }
    FE->>FE: armazena token (localStorage)
    Note over FE: requisições seguintes enviam<br/>Authorization: Bearer <token>
```

---

## 6. Sequência — Aplicação em Produto

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant FE as Frontend
    participant F as JwtAuthenticationFilter
    participant IC as InvestmentController
    participant IS as InvestmentService
    participant PR as ProductRepository
    participant WR as WalletRepository
    participant IR as InvestmentRepository
    participant TR as TransactionRepository

    C->>FE: confirma aplicação (produto, valor)
    FE->>F: POST /investments/applications (Bearer JWT)
    F->>F: valida token e popula SecurityContext
    F->>IC: requisição autenticada
    IC->>IS: apply(userId, request)
    activate IS
    IS->>PR: findById(productId)
    PR-->>IS: Product
    alt produto inativo ou valor < mínimo
        IS-->>IC: BusinessException (422)
    else válido
        IS->>WR: findByUserIdForUpdate(userId)
        WR-->>IS: Wallet (lock otimista)
        alt saldo insuficiente
            IS-->>IC: InsufficientBalanceException (422)
        else saldo ok
            IS->>IS: wallet.debit(valor)
            IS->>IR: save(Investment ACTIVE)
            IS->>TR: save(Transaction APPLICATION)
            IS-->>IC: InvestmentPositionResponse
        end
    end
    deactivate IS
    IC-->>FE: 201 Created (posição valorizada)
    FE-->>C: toast de sucesso
```

---

## 7. Sequência — Resgate

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant FE as Frontend
    participant IC as InvestmentController
    participant IS as InvestmentService
    participant VS as ValuationService
    participant IR as InvestmentRepository
    participant WR as WalletRepository
    participant TR as TransactionRepository

    C->>FE: confirma resgate (valor)
    FE->>IC: POST /investments/{id}/redemptions (Bearer JWT)
    IC->>IS: redeem(userId, investmentId, request)
    activate IS
    IS->>IR: findByIdAndUserId(id, userId)
    IR-->>IS: Investment
    alt não ACTIVE / antes da liquidez / valor > saldo bruto
        IS-->>IC: BusinessException (422)
    else válido
        IS->>VS: currentGrossValue(investment, now)
        VS-->>IS: valor bruto atual
        IS->>IS: reduz principal proporcional / status
        IS->>WR: findByUserIdForUpdate(userId)
        WR-->>IS: Wallet
        IS->>IS: wallet.credit(valor)
        IS->>TR: save(Transaction REDEMPTION)
        IS-->>IC: Portfolio atualizado
    end
    deactivate IS
    IC-->>FE: 200 OK
```

---

## 8. Máquina de Estados — Investimento

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : aplicação criada
    ACTIVE --> ACTIVE : resgate parcial<br/>(reduz principal)
    ACTIVE --> REDEEMED : resgate total<br/>(saldo bruto zerado)
    REDEEMED --> [*]
```
