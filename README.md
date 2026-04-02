# PulseDesk

A backend application that collects user comments and uses AI to automatically triage them into support tickets.

Built with Java, Spring Boot, H2 and Hugging Face AI — IBM Internship Technical Challenge.

---

## How it works

1. User submits a comment via `POST /comments`
2. The app sends the comment to Mistral AI (via Hugging Face)
3. AI decides if the comment is a real issue or just a compliment
4. If it's a real issue — a ticket is automatically created with a title, category, priority and summary
5. Tickets are available via `GET /tickets`

---

## Project structure

```
pulsedesk/
├── backend/                  Spring Boot application
│   ├── src/main/java/com/pulsedesk/
│   │   ├── controller/       REST endpoints
│   │   ├── service/          Business logic + AI integration
│   │   ├── repository/       Database access
│   │   ├── model/            Comment and Ticket entities
│   │   └── dto/              Request/response objects
│   └── src/test/             Unit tests
└── frontend/                 Reserved for future UI
```

---

## Requirements

- Java 17
- Maven (included via `mvnw`)
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

**3. Run the application**

```bash
cd backend
HF_TOKEN=your_token_here ./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`

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
HF_TOKEN=any_value ./mvnw test
```

All 9 tests should pass. Tests use mocks — no real API calls are made.

---

## H2 Database console

While the app is running, visit:

```
http://localhost:8080/h2-console
```

- JDBC URL: `jdbc:h2:mem:pulsedeskdb`
- Username: `sa`
- Password: (leave empty)

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
