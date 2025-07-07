### Epic 1: Core URL Shortener MVP

This epic covers the essential functionality required to have a working application.

### **Ticket: URLS-1** ✅

- **Title:** `Setup: Initialize Dropwizard Project with Maven/Gradle`
- **Description:** Create a new Java 21 project and configure it with the necessary Dropwizard dependencies. This includes setting up the main `Application` class, a basic `Configuration` class, and ensuring the project structure is correct.
- **Acceptance Criteria:**
    - The project builds successfully using `mvn clean install` or `./gradlew build`. ✅
    - The application starts without errors via the command line. ✅
    - A default "health check" endpoint provided by Dropwizard is accessible. ✅

### **Ticket: URLS-2**

- **Title:** `Data: Implement Persistence Layer with H2`
- **Description:** Configure the application to use an embedded H2 database for data storage. Create a DAO (Data Access Object) class that will handle all database read/write operations.
- **Acceptance Criteria:**
    - The application creates and connects to an in-memory H2 database on startup. ✅
    - A `LinkDAO` class exists with two methods: `save(Link link)` and `findByShortCode(String shortCode)`. ✅
    - A `Link` entity/model class is created to represent the data. ✅
    - The necessary database table is created automatically when the application starts.

### **Ticket: URLS-3**

- **Title:** `Feat: Develop Base62 Short Code Generation Service`
- **Description:** Create a service class responsible for converting a unique numeric ID into a Base62 string (`[0-9a-zA-Z]`). This logic should be separate from the API and database layers.
- **Acceptance Criteria:**
    - A `Base62Service` class has an `encode(long number)` method.
    - The method correctly converts various numbers to their Base62 equivalent (e.g., `1` -> `1`, `62` -> `10`, `1001` -> `g5`).
    - The logic is covered by JUnit 5 unit tests.

### **Ticket: URLS-4**

- **Title:** `API: Build POST /api/v1/links Endpoint`
- **Description:** Implement the Dropwizard `Resource` to handle the creation of new shortened links. This endpoint will orchestrate calls to the DAO and the Base62 service.
- **Acceptance Criteria:**
    - A `POST` request to `/api/v1/links` with a valid JSON body `{"longUrl": "..."}` returns a `201 Created` status.
    - The response body is `{"shortUrl": "...", "shortCode": "..."}`.
    - A new record is successfully created in the H2 database.
    - Invalid requests (e.g., missing or invalid URL) return a `400 Bad Request` status.

### **Ticket: URLS-5**

- **Title:** `API: Build GET /{shortCode} Redirect Endpoint`
- **Description:** Implement the endpoint that takes a short code from the URL path, finds the corresponding long URL, and redirects the user.
- **Acceptance Criteria:**
    - A `GET` request to `/{validShortCode}` returns a `302 Found` redirect response.
    - The `Location` header in the response contains the correct original long URL.
    - A `GET` request to a non-existent short code returns a `404 Not Found` status.

### **Ticket: URLS-6**

- **Title:** `Test: Implement API Integration Tests`
- **Description:** Use the `dropwizard-testing` framework to write end-to-end tests that simulate real API calls and verify the behavior of the entire service.
- **Acceptance Criteria:**
    - A test successfully creates a link via `POST` and then uses the returned short code to verify the `GET` redirect.
    - A test asserts that a `404` is returned for a non-existent short code.
    - All core application logic is covered by either a unit or integration test.

---

### Epic 2: Advanced Features & Polish (Going Deeper)

These tickets can be worked on after the MVP is complete to add professional-grade features.

### **Ticket: URLS-7**

- **Title:** `Feat: Allow Custom Vanity URLs`
- **Description:** Enhance the `POST /api/v1/links` endpoint to allow users to suggest their own custom short code (e.g., "my-event").
- **Acceptance Criteria:**
    - The endpoint accepts an optional `customCode` field in the request body.
    - If the `customCode` is provided and is not already in use, it is saved as the link's short code.
    - If the `customCode` is already taken, the API returns a `409 Conflict` error.
    - Validation is in place to reject invalid custom codes (e.g., contains spaces or is too long).

### **Ticket: URLS-8**

- **Title:** `Chore: Externalize Settings to config.yml`
- **Description:** Refactor the application to remove hardcoded values, such as the database connection details. These settings should be loaded from the `config.yml` file.
- **Acceptance Criteria:**
    - The H2 database URL, driver, username, and password are all defined in `config.yml`.
    - The application reads these values from the `Configuration` class at startup.
    - The application functions correctly with the externalized configuration.

### **Ticket: URLS-9**

- **Title:** `Feat: Implement Rate Limiting on Link Creation`
- **Description:** Add a server-side rate limit to the `POST /api/v1/links` endpoint to prevent abuse from a single client.
- **Acceptance Criteria:**
    - Using the Guava library, a rate limit (e.g., 20 requests per minute per IP) is implemented.
    - When a client exceeds the limit, the API responds with a `429 Too Many Requests` status code.
    - The rate limit value is configurable via the `config.yml` file.

You will need one primary table. Let's call it `links`. This table will store the core mapping between the unique ID (which you'll use to generate the short code) and the original long URL.

Here are the fields you would need, presented in a clear format:

### **Table: `links`**

| **Field Name** | **Data Type** | **Description / Constraints** |
| --- | --- | --- |
| `id` | `BIGINT` | **Primary Key.** Auto-incrementing number. This is the unique identifier for each link and the **input for your Base62 encoding**. |
| `long_url` | `TEXT` | The original, full URL that the user wants to shorten. `NOT NULL`. We use `TEXT` to accommodate very long URLs. |
| `short_code` | `VARCHAR(12)` | The generated Base62 code. It must be unique, so we add a `UNIQUE` index. `NOT NULL`. The index is **critical** for fast lookups during redirects. |
| `created_at` | `TIMESTAMP` | The timestamp of when the link was created. `NOT NULL`, defaults to the current time. Useful for tracking and debugging. |

---

### **SQL `CREATE TABLE` Statement**

Here is the actual SQL statement you would use to create this table. This can be used with H2 in Phase 1 and will also work perfectly when you migrate to MySQL in Phase 2.

SQL

`CREATE TABLE links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    long_url TEXT NOT NULL,
    short_code VARCHAR(12) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);`

### Why This Structure Works for Phase 1:

- **`id` drives the logic:** The auto-incrementing `id` is the source of truth for uniqueness. You save the link, get this ID back from the database, and then run your Base62 encoding on it to generate the `short_code`.
- **Efficient Lookups:** When a user hits `GET /{shortCode}`, the `UNIQUE` index on the `short_code` column allows the database to find the corresponding `long_url` extremely quickly.
- **All Core Tasks Covered:** This simple table has everything you need to complete all the Phase 1 tickets:
    - It stores the mapping for the `GET` redirect.
    - It provides the ID needed for short code generation.
    - It's the foundation for all your DAO and API logic.