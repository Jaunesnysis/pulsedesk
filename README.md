# PulseDesk

A backend application that collects user comments and uses AI to automatically triage them into support tickets.

---

## How it works

1. User submits a comment via the UI or `POST /comments`
2. The app sends the comment to Mistral AI (via Hugging Face)
3. AI decides if the comment is a real issue or just a compliment
4. If it's a real issue ticket is automatically created with a title, category, priority and summary
5. Tickets are available via the UI or `GET /tickets`

---

## Project structure

```
pulsedesk/
├── backend/                  Spring Boot application
│   ├── src/main/java/com/pulsedesk/
│   │   ├── controller/       REST endpoints
│   │   ├── service/          Business logic + AI
│   │   ├── repository/       Database access
│   │   ├── model/            Comment and Ticket
│   │   ├── dto/              Request/response objects
│   │   ├── config/           CORS configuration
│   │   └── exception/        Global error handling
│   └── src/test/             Unit tests
└── frontend/                 React + Vite frontend
```

---

## Requirements

- Java 17
- Maven (included via `mvnw`)
- Node.js 18+ (for frontend)
- Hugging Face account + API token with **Inference Providers** permission

---

## Setup

**1. Clone the repository**

```bash
git clone https://github.com/Jaunesnysis/pulsedesk.git
cd pulsedesk
```

**2. Get a Hugging Face token**

- Go to huggingface.co → Settings → Access Tokens
- Create a Fine-grained token
- Enable **"Make calls to Inference Providers"**
- Copy the token

**3. Create your environment file**

```bash
cd backend
echo "HF_TOKEN=your_token_here" > .env
```

**4. Run the backend**

```bash
cd backend
./mvnw spring-boot:run
```

**5. Run the frontend in second terminal**

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173`

---

## Interactive API docs

While the backend is running, visit:

```
http://localhost:8080/swagger-ui/index.html
```

This gives you a full interactive interface to explore and test all endpoints directly in the browser — no curl needed.

---

## API endpoints

### Submit a comment

```
POST /comments
Content-Type: application/json

{
  "content": "The app keeps crashing when I upload a file!",
  "source": "web-form"
}
```

### Get all comments

```
GET /comments
```

### Get all tickets

```
GET /tickets
```

### Get ticket by ID

```
GET /tickets/{id}
```

---

## Example

Submit a comment:

```bash
curl -X POST http://localhost:8080/comments \
  -H "Content-Type: application/json" \
  -d '{"content": "The app crashes when uploading files!", "source": "web-form"}'
```

Response:

```json
{
  "id": 1,
  "content": "The app crashes when uploading files!",
  "source": "web-form",
  "createdAt": "2026-04-01T15:29:32",
  "convertedToTicket": true
}
```

Check the generated ticket:

```bash
curl http://localhost:8080/tickets
```

Response:

```json
[
  {
    "id": 1,
    "title": "App crashes during file upload",
    "category": "bug",
    "priority": "high",
    "summary": "Users report the application crashes when uploading files.",
    "createdAt": "2026-04-01T15:29:35"
  }
]
```

---

## Running tests

```bash
cd backend
./mvnw test
```

All tests should pass. Tests use mocks — no real API calls are made.

---

## Tech stack

| Layer     | Technology                         |
| --------- | ---------------------------------- |
| Language  | Java 17                            |
| Framework | Spring Boot 3.5                    |
| Database  | H2 (in-memory)                     |
| ORM       | Spring Data JPA / Hibernate        |
| AI        | Hugging Face — Mistral-7B-Instruct |
| Testing   | JUnit 5 + Mockito                  |
| Build     | Maven                              |
| Frontend  | React + Vite                       |
