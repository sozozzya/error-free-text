# Error Free Text

Monolithic REST application that accepts user text, creates an asynchronous correction task, and returns a spell-checked result using the Yandex Speller API.

## Tech stack

- Java 17+
- Spring Boot 3
- Gradle
- PostgreSQL
- Docker
- Docker Compose
- Yandex Speller API

## Architecture

```text
REST API
   │
   ▼
TaskController
   │
   ▼
TaskService ──► TaskRepository ──► PostgreSQL
   │
Scheduler
   │
   ▼
TaskProcessor
   │
   ▼
TextCorrectionService
   │
   ▼
SpellerClient (YandexSpellerClient)
   │
   ▼
Yandex Speller API
```

- `TaskController` exposes REST endpoints and input validation.
- `TaskService` creates tasks and maps results to API DTOs.
- `TaskScheduler` periodically starts background processing.
- `TaskProcessor` claims `NEW` tasks, updates status, and isolates per-task errors.
- `TextCorrectionService` splits long texts, calculates Speller options, and applies corrections.
- `SpellerClient` isolates the HTTP integration with Yandex Speller.
- `TaskRepository` persists tasks in PostgreSQL. Schema is managed by Flyway.

## Run

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`.

OpenAPI UI: `http://localhost:8080/swagger-ui.html`

To run only the database locally:

```bash
docker compose up postgres
```

Then start the application with Gradle after exporting the database environment variables.

## API

### Create a correction task

```http
POST /tasks
Content-Type: application/json

{
  "text": "синхрафазатрон в дубне",
  "language": "RU"
}
```

Response `201 Created`:

```json
{
  "id": "44bd78dc-d08c-41c6-b87d-fb82046bd470"
}
```

`language` accepts only `RU` or `EN`. Text must contain at least 3 characters and at least one letter.

### Get task result

```http
GET /tasks/{id}
```

Completed:

```json
{
  "status": "COMPLETED",
  "text": "синхрофазотрон в Дубне"
}
```

In progress:

```json
{
  "status": "PROCESSING"
}
```

Failed:

```json
{
  "status": "FAILED",
  "error": "Yandex Speller returned HTTP 503"
}
```

Missing task returns HTTP 404:

```json
{
  "errorMessage": "Task with id: 44bd78dc-d08c-41c6-b87d-fb82046bd470 not found",
  "errorCode": 40401,
  "timestamp": "2026-10-07T12:00:00Z",
  "path": "/tasks/44bd78dc-d08c-41c6-b87d-fb82046bd470"
}
```

## Configuration

| Variable | Description | Default |
| --- | --- | --- |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `error_free_text` |
| `DB_USER` | Database user | `errorfreetext` |
| `DB_PASSWORD` | Database password | `errorfreetext` |
| `SCHEDULER_DELAY` | Scheduler delay in milliseconds | `5000` |

Do not commit production secrets. Override these values through environment variables.

## Testing

```bash
./gradlew test
```

On Windows:

```bash
gradlew.bat test
```
