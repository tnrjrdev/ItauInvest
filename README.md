# ItauInvest — Plataforma de Investimentos

Aplicação full‑stack de uma plataforma de investimentos: gestão de carteira de
caixa, catálogo de produtos (CDB, Tesouro, LCI/LCA, Fundos, Ações), aplicações e
resgates com valorização, posição consolidada e extrato (ledger) auditável.

> Projeto de referência com padrões de engenharia de nível produção: arquitetura
> em camadas, SOLID, Clean Code, segurança JWT, tratamento global de exceções,
> testes automatizados (cobertura ≥ 80%) e containerização.

---

## 🧱 Stack

| Camada | Tecnologias |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3, Spring Security, JWT (jjwt), Spring Data JPA, Flyway, Bean Validation |
| **Frontend** | React 18, TypeScript, Vite, TailwindCSS, React Router, Axios |
| **Banco** | PostgreSQL 16 |
| **Infra** | Docker, Docker Compose, Nginx |
| **Docs** | OpenAPI / Swagger UI |
| **Testes** | JUnit 5, Mockito, Spring Test (MockMvc), JaCoCo |

---

## 🏛️ Arquitetura

```
┌───────────────┐      HTTP/JSON       ┌──────────────────────────────────────┐
│   Frontend    │  ───────────────►    │              Backend (API)           │
│ React + Vite  │   Bearer JWT         │  Controller → Service → Repository   │
│   (Nginx)     │  ◄───────────────    │   DTO · Security (JWT)                │
└───────────────┘                      │   Global Exception Handler           │
                                       └───────────────────┬──────────────────┘
                                                           │ JPA
                                                   ┌───────▼────────┐
                                                   │  PostgreSQL    │
                                                   └────────────────┘
```

**Camadas (backend):** `controller` (REST, sem regra de negócio) · `service`
(regras de negócio, `@Transactional`) · `repository` (Spring Data JPA) · `dto`
(contratos de entrada/saída) · `domain` (entidades + enums) · `security` (JWT) ·
`config` (Security, OpenAPI, seed) · `exception` (tratamento global).

### Modelo de dados

```
User (1)───(1) Wallet (1)───(N) Transaction (N)───(0..1) Investment
  │                                                          │
  └────────────────────(N) Investment (N)───(1) Product ────┘
```

- **User 1—1 Wallet**: cada cliente tem uma carteira de caixa.
- **User 1—N Investment**: cada aplicação cria uma posição (lote) com data‑base própria.
- **Product 1—N Investment**: um produto pode estar em várias posições.
- **Wallet 1—N Transaction**: ledger imutável (append‑only) com `balanceAfter`.
- **Investment 1—N Transaction**: lançamentos de aplicação/resgate referenciam a posição.

### Decisões de arquitetura

| Tema | Decisão | Motivo |
|---|---|---|
| Precisão monetária | `BigDecimal(19,4)` | Evita erros de ponto flutuante em dinheiro |
| Concorrência no saldo | `@Version` (optimistic lock) + `@Transactional` | Protege saldo contra corrida; conflito → HTTP 409 |
| Valorização | Juros compostos pro‑rata por dias corridos, calculados na leitura | Sempre coerente com o tempo decorrido |
| Auditabilidade | `Transaction` append‑only com `balanceAfter` | Rastreabilidade total do caixa |
| Chave primária | UUID | Evita enumeração sequencial de recursos financeiros |
| Soft delete de produto | flag `active` | Preserva integridade histórica das posições |

> **Fórmula da valorização (simulação determinística):**
> `valorBruto = principal × (1 + taxaAnual/100)^(dias/365)`

📐 **Diagramas UML** (casos de uso, classes, ER, componentes, sequência e estados):
veja [`docs/DIAGRAMS.md`](docs/DIAGRAMS.md) (Mermaid) ou as imagens em
[`docs/uml/`](docs/uml/README.md). Visão geral do sistema:

![Visão Geral](docs/uml/0-overview.png)

---

## 🚀 Como executar

### Opção 1 — Docker Compose (recomendado)

Pré‑requisitos: Docker + Docker Compose.

```bash
cp .env.example .env          # ajuste as variáveis se desejar
docker compose up --build
```

Serviços:

| Serviço | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend (API) | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |

O backend cria o schema via **Flyway**, popula o catálogo de produtos e provisiona
os usuários de demonstração automaticamente.

### Opção 2 — Local (desenvolvimento)

**Banco:**
```bash
docker compose up -d db
```

**Backend** (Java 21 + Maven):
```bash
cd backend
mvn spring-boot:run
# API em http://localhost:8080
```

**Frontend** (Node 20+):
```bash
cd frontend
npm install
npm run dev
# App em http://localhost:5173 (proxy /api → :8080)
```

---

## 🔑 Credenciais de demonstração

| Perfil | E‑mail | Senha | Observação |
|---|---|---|---|
| **ADMIN** | `admin@itau.com.br` | `Admin@123` | Gerencia produtos |
| **CLIENT** | `cliente@itau.com.br` | `Cliente@123` | Saldo inicial de R$ 10.000 |

> Novos clientes podem se cadastrar pela tela de registro (perfil `CLIENT`).

---

## 🔐 Fluxo de autenticação

```
1. POST /api/v1/auth/register  ou  /api/v1/auth/login
   └─► retorna { accessToken (JWT), tokenType: "Bearer", expiresIn, ... }

2. O frontend persiste o token (localStorage) e injeta em toda requisição:
   Authorization: Bearer <accessToken>

3. JwtAuthenticationFilter valida assinatura/expiração e popula o SecurityContext.

4. Em 401, o interceptor do Axios limpa a sessão e redireciona para /login.
```

- Senhas: **BCrypt** (strength 12).
- Tokens: **HS256**, expiração configurável (`JWT_EXPIRATION_MS`).
- Autorização por perfil via `@EnableMethodSecurity` e regras no `SecurityConfig`.

---

## 📡 Endpoints principais

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Público | Cadastro de cliente |
| POST | `/api/v1/auth/login` | Público | Login |
| GET | `/api/v1/products` | Público | Lista produtos (filtro `?type=`) |
| GET | `/api/v1/products/{id}` | Público | Detalha produto |
| POST | `/api/v1/products` | ADMIN | Cria produto |
| PUT | `/api/v1/products/{id}` | ADMIN | Atualiza produto |
| DELETE | `/api/v1/products/{id}` | ADMIN | Desativa produto |
| GET | `/api/v1/wallet` | Autenticado | Saldo da carteira |
| POST | `/api/v1/wallet/deposits` | Autenticado | Depósito |
| POST | `/api/v1/wallet/withdrawals` | Autenticado | Saque |
| GET | `/api/v1/wallet/statement` | Autenticado | Extrato paginado |
| POST | `/api/v1/investments/applications` | Autenticado | Aplicar em produto |
| POST | `/api/v1/investments/{id}/redemptions` | Autenticado | Resgatar (parcial/total) |
| GET | `/api/v1/investments/positions` | Autenticado | Posições ativas |
| GET | `/api/v1/investments/portfolio` | Autenticado | Carteira consolidada |

Contrato completo e interativo: **Swagger UI** (`/swagger-ui.html`).

### Exemplo (cURL)

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"cliente@itau.com.br","password":"Cliente@123"}' | jq -r .accessToken)

# Depósito
curl -X POST http://localhost:8080/api/v1/wallet/deposits \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"amount":1000.00,"description":"PIX"}'

# Carteira consolidada
curl http://localhost:8080/api/v1/investments/portfolio -H "Authorization: Bearer $TOKEN"
```

---

## ⚙️ Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/itau_invest` | JDBC do PostgreSQL |
| `DB_USERNAME` / `DB_PASSWORD` | `itau` / `itau` | Credenciais do banco |
| `JWT_SECRET` | (ver `.env.example`) | Chave Base64 HS256 |
| `JWT_EXPIRATION_MS` | `3600000` | Validade do token (ms) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Origens permitidas (CSV) |
| `SERVER_PORT` | `8080` | Porta do backend |
| `VITE_API_BASE_URL` | `/api/v1` | Base URL da API no frontend |

---

## 🧪 Testes & cobertura

```bash
cd backend
mvn verify          # roda testes + gate de cobertura JaCoCo (≥ 80%)
```

- **31 testes**: unitários (Mockito) das regras de negócio e segurança +
  integração ponta a ponta (MockMvc + H2) do fluxo completo.
- Relatório de cobertura: `backend/target/site/jacoco/index.html`.
- Casos cobertos: sucesso e erro (saldo insuficiente, abaixo do mínimo, produto
  inativo, resgate acima do saldo, liquidez, credenciais inválidas, duplicidade).

```bash
cd frontend
npm run build       # type-check (tsc) + build de produção (vite)
```

---

## 📁 Estrutura do projeto

```
itau_dev/
├── backend/
│   ├── src/main/java/com/itau/invest/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Regras de negócio
│   │   ├── repository/      # Spring Data JPA
│   │   ├── domain/          # entity/ + enums/
│   │   ├── dto/             # Contratos (records)
│   │   ├── security/        # JWT, filtros, UserDetails
│   │   ├── config/          # Security, OpenAPI, seed
│   │   └── exception/       # Handler global + exceções
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/    # Flyway (V1 schema, V2 seed)
│   ├── src/test/            # JUnit 5 + Mockito + integração
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── pages/           # Login, Register, Dashboard, Products, Wallet
│   │   ├── components/      # Layout, ProtectedRoute, UI
│   │   ├── context/         # AuthContext
│   │   ├── services/        # Cliente da API (Axios)
│   │   ├── lib/             # http (interceptors), format
│   │   └── types/
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 🛣️ Melhorias futuras (roadmap)

- Idempotency‑Key para operações financeiras (evitar duplo débito em retry).
- Refresh token + rotação e blacklist de tokens.
- Marcação a mercado real para renda variável.
- Paginação/ordenação avançada e cache de catálogo.
- Observabilidade: tracing distribuído e métricas de negócio.

---

## 📜 Licença

Projeto de referência para fins educacionais/demonstração.
