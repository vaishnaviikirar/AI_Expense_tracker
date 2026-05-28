# 💰AI Expense Tracker & Budget Advisor

A full-stack expense tracking application with JWT authentication and OpenAI-powered budget advice.

---

## 📁 Folder Structure

```
expense-tracker/
│
├── backend/                          ← Spring Boot Project
│   ├── pom.xml                       ← Maven dependencies
│   └── src/main/
│       ├── java/com/expensetracker/
│       │   ├── ExpenseTrackerApplication.java   ← Main class
│       │   │
│       │   ├── entity/               ← Database table mappings
│       │   │   ├── User.java
│       │   │   └── Expense.java
│       │   │
│       │   ├── dto/                  ← Data Transfer Objects
│       │   │   ├── RegisterRequest.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── AuthResponse.java
│       │   │   ├── ExpenseRequest.java
│       │   │   ├── ExpenseResponse.java
│       │   │   └── AiSuggestionResponse.java
│       │   │
│       │   ├── repository/           ← Database access layer
│       │   │   ├── UserRepository.java
│       │   │   └── ExpenseRepository.java
│       │   │
│       │   ├── service/              ← Business logic layer
│       │   │   ├── AuthService.java
│       │   │   ├── ExpenseService.java
│       │   │   └── OpenAIService.java
│       │   │
│       │   ├── controller/           ← REST API endpoints
│       │   │   ├── AuthController.java
│       │   │   └── ExpenseController.java
│       │   │
│       │   ├── security/             ← JWT + Spring Security
│       │   │   ├── JwtUtil.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   │
│       │   ├── config/
│       │   │   └── SecurityConfig.java         ← Security rules
│       │   │
│       │   └── exception/            ← Error handling
│       │       ├── ResourceNotFoundException.java
│       │       ├── BadRequestException.java
│       │       └── GlobalExceptionHandler.java
│       │
│       └── resources/
            └── static/
                └── ├── login.html
│                   ├── register.html
│                   ├── dashboard.html
│                   ├── styles.css
│                   ├── auth.js
│                   └── dashboard.js
│           └── application.properties
│
├── schema.sql                        ← MySQL database setup
└── README.md
```

---

## 🚀 Step-by-Step Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- OpenAI API key (get from https://platform.openai.com)

---

### Step 1: Set Up MySQL Database

```bash
# Login to MySQL
mysql -u root -p

# Run the schema file
source /path/to/expense-tracker/schema.sql;
```

Or manually:
```sql
CREATE DATABASE expense_tracker_db;
```

---

### Step 2: Configure application.properties

Open `backend/src/main/resources/application.properties` and update:

```properties
# Your MySQL credentials
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# Generate a Base64-encoded secret key (min 256 bits):
# Run in terminal: echo -n "your-super-secret-key-must-be-very-long" | base64
app.jwt.secret=eW91ci1zdXBlci1zZWNyZXQta2V5LW11c3QtYmUtdmVyeS1sb25n

# Your OpenAI API key
openai.api.key=sk-proj-xxxxxxxxxxxxxxxx
```

**How to generate a proper JWT secret:**
```bash
# Linux/Mac:
echo -n "Super-Secret-JWT-Key-2024-Must-Be-256-bits" | base64

# Windows (PowerShell):
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("SpendWise-Super-Secret-JWT-Key-2024"))
```

---

### Step 3: Build and Run Backend

```bash
cd expense-tracker/backend

# Build (skipping tests for first run)
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

You should see: `Started ExpenseTrackerApplication on port 8080`

---

### Step 4: Run Frontend

No build step needed! Simply open the HTML files in a browser.

**Option A - Open directly:**
```bash
# Just open login.html in your browser
open frontend/login.html   # Mac
start frontend/login.html  # Windows
```

**Option B - Use VS Code Live Server (recommended):**
1. Install "Live Server" extension in VS Code
2. Right-click `login.html` → "Open with Live Server"
3. Opens at http://localhost:5500/login.html

**Option C - Simple Python server:**
```bash
cd frontend
python -m http.server 3000
# Visit http://localhost:3000/login.html
```

---

### Step 5: Test the Application

1. Open `http://localhost:3000/login.html` (or wherever frontend runs)
2. Click "Create one" to register
3. Fill in name, email, password
4. You'll be redirected to the dashboard
5. Add some expenses
6. Click "✨ Get AI Suggestion" to see AI advice

---

## 🔐 JWT Authentication Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        JWT AUTH FLOW                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. REGISTRATION / LOGIN                                        │
│     Frontend ──POST /api/auth/login──► AuthController           │
│                  { email, password }                            │
│                                                                 │
│  2. CREDENTIAL VERIFICATION                                     │
│     AuthController ──► AuthService ──► AuthenticationManager   │
│                              │                                  │
│                              ▼                                  │
│                     UserDetailsService                          │
│                   (loads user from DB)                          │
│                              │                                  │
│                              ▼                                  │
│                  BCryptPasswordEncoder                          │
│                 (compares hashed passwords)                     │
│                                                                 │
│  3. TOKEN GENERATION (on success)                               │
│     JwtUtil.generateToken(userDetails)                          │
│     → Creates: header.payload.signature                         │
│     → Payload contains: { sub: "user@email.com", exp: ... }    │
│                                                                 │
│  4. TOKEN RETURNED TO FRONTEND                                  │
│     ◄── { token: "eyJhbGci...", name: "...", email: "..." }    │
│     Frontend stores token in localStorage                       │
│                                                                 │
│  5. SUBSEQUENT SECURED REQUESTS                                 │
│     Frontend ──GET /api/expenses──► JwtAuthenticationFilter     │
│               Authorization: Bearer <token>                     │
│                              │                                  │
│                              ▼                                  │
│                     JwtUtil.extractUsername()                   │
│                   → Gets email from token payload               │
│                              │                                  │
│                              ▼                                  │
│                    JwtUtil.isTokenValid()                        │
│                   → Checks signature + expiry                   │
│                              │                                  │
│                              ▼                                  │
│               SecurityContextHolder.setAuthentication()         │
│               (marks user as authenticated for this request)    │
│                              │                                  │
│                              ▼                                  │
│                     ExpenseController                           │
│              @AuthenticationPrincipal UserDetails               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Request Flow (Add Expense)

```
Frontend (dashboard.js)
    │
    │  POST /api/expenses
    │  Headers: Authorization: Bearer eyJhbGci...
    │  Body: { title, amount, category, date }
    │
    ▼
JwtAuthenticationFilter (runs on every request)
    │  1. Extract token from "Authorization" header
    │  2. Extract email from token payload
    │  3. Load user from database
    │  4. Validate token (signature + expiry)
    │  5. Set authentication in SecurityContext
    │
    ▼
SecurityFilterChain
    │  Checks: is this endpoint secured?
    │  Yes → Is user authenticated? (set in SecurityContext above)
    │  Yes → Allow request to proceed
    │
    ▼
ExpenseController.addExpense()
    │  @AuthenticationPrincipal UserDetails → gets email from JWT
    │  @Valid @RequestBody → validates request data
    │
    ▼
ExpenseService.addExpense()
    │  Find user by email
    │  Create Expense entity
    │  Link to user
    │
    ▼
ExpenseRepository.save()
    │  Spring Data JPA → generates SQL
    │  INSERT INTO expenses (title, amount, ...) VALUES (...)
    │
    ▼
MySQL Database
    │  Stores the record
    │
    ▼ (return path)
ExpenseResponse DTO ← mapToResponse(expense)
    │  Never expose raw entity!
    │
    ▼
ResponseEntity<ExpenseResponse>
    │  HTTP 201 Created
    │
    ▼
Frontend receives JSON
    │  Updates UI without page reload
```

---

## 🤖 AI Integration Flow

```
User clicks "Get AI Suggestion"
    │
    ▼
dashboard.js: fetch GET /api/expenses/ai-suggestion
    │
    ▼
ExpenseController.getAiSuggestion()
    │
    ▼
ExpenseService.getCategoryWiseSummary()
    │  SELECT category, SUM(amount) FROM expenses
    │  WHERE user_id = ? GROUP BY category
    │  Returns: "Food: ₹4500.00, Travel: ₹2000.00, ..."
    │
    ▼
OpenAIService.generateBudgetAdvice(summary)
    │
    │  Builds prompt:
    │  "Analyze my expenses: Food: ₹4500, Travel: ₹2000...
    │   Please provide: 1) Spending analysis 2) Areas of concern..."
    │
    ▼
HTTP POST to OpenAI API
    │  https://api.openai.com/v1/chat/completions
    │  Headers: Authorization: Bearer sk-proj-xxx
    │  Body: { model: "gpt-3.5-turbo", messages: [...] }
    │
    ▼
OpenAI returns response
    │  { choices: [{ message: { content: "📊 SPENDING ANALYSIS: ..." } }] }
    │
    ▼
Parse and extract advice string
    │
    ▼
Return AiSuggestionResponse { suggestion: "..." }
    │
    ▼
Frontend displays in scrollable AI container
```

---

## 📡 API Endpoints

| Method | Endpoint                      | Auth Required | Description                |
|--------|-------------------------------|:-------------:|----------------------------|
| POST   | /api/auth/register            | ❌ No         | Register new user          |
| POST   | /api/auth/login               | ❌ No         | Login, returns JWT token   |
| POST   | /api/expenses                 | ✅ Yes        | Add a new expense          |
| GET    | /api/expenses                 | ✅ Yes        | Get all user expenses      |
| DELETE | /api/expenses/{id}            | ✅ Yes        | Delete an expense          |
| GET    | /api/expenses/ai-suggestion   | ✅ Yes        | Get AI budget advice       |

### Example Requests

**Register:**
```json
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john@example.com",
  "name": "John Doe",
  "userId": 1
}
```

**Add Expense:**
```json
POST /api/expenses
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
{
  "title": "Lunch at Swiggy",
  "amount": 350.00,
  "category": "Food",
  "date": "2024-01-15"
}
```

---

## 🗄️ Database Relationships

```
users (1) ──────────────────── (M) expenses
  id PK                             id PK
  name                              title
  email UNIQUE                      amount
  password (BCrypt)                 category
                                    date
                                    user_id FK → users.id
```

**Relationship Type:** One-to-Many
- One User can have MANY Expenses
- Each Expense belongs to EXACTLY ONE User
- Configured with `@OneToMany` / `@ManyToOne` in JPA entities
- `ON DELETE CASCADE`: if a user is deleted, all their expenses are deleted

---

## 🛡️ Security Features

| Feature | Implementation |
|---------|---------------|
| Password Hashing | BCryptPasswordEncoder (cost factor 10) |
| Authentication | JWT (JSON Web Tokens, HS256 algorithm) |
| Authorization | Spring Security filter chain |
| Session | Stateless (SESSIONLESS) |
| CORS | Configured for frontend origins |
| Input Validation | Bean Validation (@NotBlank, @NotNull, @Positive) |
| SQL Injection | Prevented by Spring Data JPA (parameterized queries) |
| XSS Prevention | Frontend escapes HTML in user content |
| Ownership Check | Expenses verified against authenticated user |

---

## 🐛 Troubleshooting

**"Unable to connect to server"**
- Make sure Spring Boot is running: `mvn spring-boot:run`
- Check port 8080 is not occupied by another process

**"401 Unauthorized" on all requests**
- JWT secret in properties might be misconfigured
- Try clearing localStorage and logging in again

**"AI service unavailable"**
- Check your OpenAI API key in application.properties
- Make sure the key has available credits at platform.openai.com

**MySQL connection refused**
- Make sure MySQL service is running
- Check username/password in application.properties
- Run the schema.sql file first

**BCrypt error / BadCredentials**
- This happens if you manually inserted a plaintext password in DB
- Always use the register endpoint (it hashes automatically)

---

## 💡 Key Learning Points (For Interviews)

1. **Why JWT over Sessions?** JWT is stateless — no server-side storage needed. Each request carries authentication info.

2. **Why BCrypt?** One-way hash with salt. Can't reverse. Slow by design (prevents brute force).

3. **Why DTOs?** Never expose entities directly. Control what data reaches clients. Prevent over-fetching.

4. **Why @RestControllerAdvice?** Centralized exception handling. Consistent error format. Separation of concerns.

5. **Why JPA over raw SQL?** Type safety, auto-generated queries, database portability, reduced boilerplate.

6. **OneToMany vs ManyToOne?** User (1) → Expenses (M). `@ManyToOne` in Expense owns the relationship (has FK column).

7. **CORS?** Browsers block cross-origin requests by default. Backend must explicitly allow them.
