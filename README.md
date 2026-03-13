# Easy-Card

Credit Card Management Application built with Spring Boot (backend) and React (frontend).

## Features

- User registration and authentication with JWT
- Apply for credit cards
- View and manage cards (activate, block, cancel)
- Process transactions
- WhatsApp integration for card cancellation
- Automated expiry notifications via WhatsApp

## Project Structure

```
Easy-Card/
├── .github/
│   └── workflows/
│       └── ci-cd.yml          # GitHub Actions CI/CD pipeline
│
├── docker-compose.yml          # Docker orchestration for local dev
│
├── easy-card-backend/          # Spring Boot backend
│   ├── src/main/java/com/easycard/
│   │   ├── config/             # Security & app configuration
│   │   │   └── SecurityConfig.java
│   │   ├── controller/         # REST API controllers
│   │   │   ├── AuthController.java
│   │   │   ├── CardController.java
│   │   │   ├── TransactionController.java
│   │   │   └── WhatsAppController.java
│   │   ├── dto/
│   │   │   ├── request/        # Request DTOs
│   │   │   └── response/       # Response DTOs
│   │   ├── entity/             # JPA entities
│   │   │   ├── Card.java
│   │   │   ├── Transaction.java
│   │   │   └── User.java
│   │   ├── exception/          # Global exception handling
│   │   ├── repository/         # JPA repositories
│   │   ├── security/           # JWT & authentication
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtTokenProvider.java
│   │   └── service/            # Business logic
│   │       ├── AuthService.java
│   │       ├── CardService.java
│   │       ├── ExpiryNotificationService.java
│   │       ├── TransactionService.java
│   │       └── WhatsAppService.java
│   ├── src/main/resources/
│   │   ├── application.yml     # App configuration
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── application-test.yml
│   ├── Dockerfile              # Backend container image
│   └── pom.xml                 # Maven dependencies
│
├── easy-card-frontend/         # React frontend (Vite)
│   ├── src/
│   │   ├── context/
│   │   │   └── AuthContext.jsx # Auth state management
│   │   ├── pages/
│   │   │   ├── ApplyCard.jsx   # Card application form
│   │   │   ├── Auth.css
│   │   │   ├── Dashboard.jsx   # Main dashboard
│   │   │   ├── Login.jsx       # Login page
│   │   │   └── Register.jsx   # Registration page
│   │   ├── services/
│   │   │   └── api.js          # API client
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── index.css
│   ├── public/
│   ├── Dockerfile              # Frontend container image
│   ├── vercel.json             # Vercel deployment config
│   ├── package.json
│   ├── vite.config.js
│   └── eslint.config.js
│
└── README.md
```

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login user | No |

### Cards

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/cards` | Get user's cards | Yes |
| GET | `/api/cards/{id}` | Get card by ID | Yes |
| POST | `/api/cards/apply` | Apply for new card | Yes |
| PUT | `/api/cards/{id}/activate` | Activate card (admin) | Admin |
| PUT | `/api/cards/{id}/block` | Block card (admin) | Admin |
| PUT | `/api/cards/{id}/limit` | Update credit limit | Yes |
| GET | `/api/cards/admin/all` | Get all cards | Admin |
| GET | `/api/cards/admin/pending` | Get pending cards | Admin |

### Transactions

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/transactions` | Get my transactions | Yes |
| GET | `/api/transactions/card/{cardId}` | Get card transactions | Yes |
| POST | `/api/transactions` | Process transaction | Yes |

### WhatsApp

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/whatsapp/webhook` | WhatsApp webhook verification | No |
| POST | `/api/whatsapp/webhook` | WhatsApp incoming messages | No |
| GET | `/api/whatsapp/cancel-link/{cardId}` | Get WhatsApp cancel link | Yes |
| POST | `/api/whatsapp/notify-expiry` | Trigger expiry notifications | Admin |
| POST | `/api/whatsapp/notify-expiry/{cardId}` | Send expiry notification | Admin |

### Request/Response Examples

#### Register
```json
POST /api/auth/register
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

#### Login
```json
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "user@example.com",
  "role": "USER"
}
```

#### Apply for Card
```json
POST /api/cards/apply
{
  "cardHolderName": "John Doe",
  "creditLimit": 5000
}
```

## Environment Variables

### Backend (`application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/easycard
    username: easycard
    password: easycard123

jwt:
  secret: your-secret-key
  expiration: 86400000

whatsapp:
  phone-number-id: your-phone-number-id
  auth-token: your-auth-token
  verify-token: your-verify-token
  business-phone: your-business-phone
  expiry-notification-days: 30

scheduler:
  cron: "0 0 9 * * *"
```

### Frontend
```env
VITE_API_URL=http://localhost:8080/api
```

## Running the Application

### With Docker Compose
```bash
docker-compose up --build
```

Services:
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

### Manual Setup

#### Backend
```bash
cd easy-card-backend
mvn spring-boot:run
```

#### Frontend
```bash
cd easy-card-frontend
npm install
npm run dev
```

## Vercel Deployment

The frontend is configured for deployment to Vercel.

### Quick Deploy

1. Push code to GitHub
2. Go to https://vercel.com
3. Import the repository
4. Add environment variable:
   - `VITE_API_URL` = Your backend API URL
5. Deploy

### Deploy via CLI
```bash
cd easy-card-frontend
npm install -g vercel
vercel
```

### Vercel Configuration (`vercel.json`)
```json
{
  "buildCommand": "npm run build",
  "devCommand": "npm run dev",
  "installCommand": "npm install",
  "framework": "vite",
  "outputDirectory": "dist"
}
```

## WhatsApp Integration

### Setup

1. Create a Meta Developer account
2. Create a WhatsApp Business app
3. Get credentials:
   - Phone Number ID
   - Access Token
4. Set webhook URL: `https://your-domain/api/whatsapp/webhook`
5. Configure environment variables in backend

### Commands

- `CANCEL <last-4-digits>` - Cancel card subscription
- `HELP` - Show help message

Example: Send `CANCEL 1234` to cancel card ending in 1234

## Technology Stack

- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: PostgreSQL (production), H2 (dev)
- **Auth**: JWT
- **Frontend**: React 19, Vite, React Router
- **HTTP Client**: Axios
- **Deployment**: Docker, Vercel, GitHub Actions

## License

MIT
