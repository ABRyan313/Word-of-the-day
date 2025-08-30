# Word of the Day

A Spring Boot application that provides a **random word of the day** along with its definitions and part of speech. The application uses external APIs:

- [Random Word API](https://random-word-api.herokuapp.com/) for fetching random words.
- [Dictionary API](https://dictionaryapi.dev/) for retrieving definitions.

---

## Features

- Fetch a random word daily.
- Retrieve multiple definitions and their part of speech.
- Clean, structured JSON responses.
- One-day caching to reduce external API calls.
- Swagger/OpenAPI integration for API documentation.
- Unit tests for service and controller layers.

---

## Project Structure

```

src/main/java
├── com.example.word
│   ├── controller       # REST controllers
│   ├── service          # Business logic, WebClient calls
│   ├── model
│   │   ├── domain       # DTOs for API responses
│   │   └── dto          # DTOs for mapping external APIs

````

---

## Setup & Usage

### Prerequisites

- Java 17+
- Maven 3+
- IDE: IntelliJ / Eclipse / VS Code
- Internet connection (for external APIs)

### Clone & Build

```bash
git clone https://github.com/yourusername/word-of-the-day.git
cd word-of-the-day
mvn clean install
````

### Run Application

```bash
mvn spring-boot:run
```

The application will start on **[http://localhost:8080](http://localhost:8080)**.

---

## API Endpoints

| Method | Endpoint      | Description                      | Response Example |
| ------ | ------------- | -------------------------------- | ---------------- |
| GET    | `/word/daily` | Get random word with definitions | See below        |

### Example Response

```json
{
  "word": "serendipity",
  "definitions": [
    {
      "definition": "the occurrence of events by chance in a happy or beneficial way",
      "partOfSpeech": "noun"
    },
    {
      "definition": "an aptitude for making desirable discoveries by accident",
      "partOfSpeech": "noun"
    }
  ]
}
```

---

##  Caching

* The application uses **Spring Cache** to store the word and definitions for **24 hours (1 day)**.
* This reduces repeated calls to external APIs and ensures the “Word of the Day” remains consistent.

---

## API Documentation

* Swagger UI is available at:

  ```
  http://localhost:8080/swagger-ui.html
  ```
* OpenAPI JSON:

  ```
  http://localhost:8080/v3/api-docs
  ```

---

## 💻 Testing

### Unit Tests (Mandatory)

Tests are written for:

* `WordService` methods (`getWord`, `getWordDefinitions`, `getDefinitionAndPos`)
* `WordController` endpoints

Run tests:

```bash
mvn test
```

---

## Notes

* External APIs are required for normal operation; fallback words are used if API fails.
* The project uses **Spring WebClient** for asynchronous HTTP calls.
* DTOs are mapped using **Lombok** for simplicity.

---

## References

* [Spring Boot Documentation](https://spring.io/projects/spring-boot)
* [Spring WebFlux WebClient](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client)
* [Dictionary API](https://dictionaryapi.dev/)
* [Random Word API](https://random-word-api.herokuapp.com/)

```


