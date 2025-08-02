# Phase 2: Production Database & Telemetry - Project Tickets

## Epic: Migrate to Production-Ready MySQL Database with Telemetry

### 🎯 Epic Goals
- Replace in-memory/H2 database with MySQL
- Implement proper schema design with indexing
- Add telemetry capture for click tracking
- Apply database performance best practices

---

## 📋 Tickets

### TICKET-001: Set Up MySQL with Docker Compose
**Priority:** High  
**Story Points:** 3  
**Type:** Infrastructure

**Description:**  
Create a Docker Compose configuration to run MySQL locally for development.

**Acceptance Criteria:**
- [ ] Create `docker-compose.yml` file with MySQL 8.0+ service
- [ ] Configure MySQL with:
    - Custom database name: `urlshortener`
    - Root password stored in `.env` file (not committed)
    - Port mapping: 3306:3306
    - Volume mount for data persistence
- [ ] Add `.env.example` file with template environment variables
- [ ] Update README with instructions for starting MySQL

**Technical Notes:**
```yaml
# Example structure
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: urlshortener
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"
```

---

### TICKET-002: Configure Dropwizard MySQL Connection
**Priority:** High  
**Story Points:** 2  
**Type:** Backend
**Dependencies:** TICKET-001

**Description:**  
Update Dropwizard application to connect to MySQL instead of H2/in-memory storage.

**Acceptance Criteria:**
- [ ] Add MySQL JDBC driver dependency to `pom.xml` or `build.gradle`
- [ ] Update `config.yml` with MySQL connection parameters
- [ ] Implement connection pooling (HikariCP recommended)
- [ ] Add health check for database connectivity
- [ ] Ensure graceful fallback if database is unavailable on startup

**Technical Notes:**
- Use Dropwizard's built-in database configuration
- Connection string format: `jdbc:mysql://localhost:3306/urlshortener`
- Consider connection pool settings: min/max connections, timeout values

---

### TICKET-003: Create Links Table Schema
**Priority:** High  
**Story Points:** 2  
**Type:** Database
**Dependencies:** TICKET-002

**Description:**  
Design and create the `links` table with proper schema and indexing.

**Acceptance Criteria:**
- [ ] Create table with schema:
  ```sql
  CREATE TABLE links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    long_url TEXT NOT NULL,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    click_count INT DEFAULT 0
  );
  ```
- [ ] Add index on `short_code` column for fast lookups
- [ ] Add index on `created_at` for potential future queries
- [ ] Document schema decisions in technical design doc

**Technical Notes:**
- Consider UTF8MB4 character set for full Unicode support
- The `click_count` field addresses the requirement from the first section

---

### TICKET-004: Implement Schema Migration Tool
**Priority:** Medium  
**Story Points:** 3  
**Type:** Infrastructure
**Dependencies:** TICKET-003

**Description:**  
Integrate Liquibase or Flyway for professional schema version management.

**Acceptance Criteria:**
- [ ] Choose and integrate migration tool (recommend Flyway for simplicity)
- [ ] Convert existing schema to migration files:
    - `V1__create_links_table.sql`
    - `V2__add_click_count_to_links.sql`
- [ ] Configure auto-migration on application startup
- [ ] Add migration status endpoint for monitoring
- [ ] Document migration process in README

**Technical Notes:**
- Store migration files in `src/main/resources/db/migration/`
- Consider adding a pre-deployment migration check

---

### TICKET-005: Create Clicks Telemetry Table
**Priority:** High  
**Story Points:** 2  
**Type:** Database
**Dependencies:** TICKET-004

**Description:**  
Design and create the `clicks` table for capturing telemetry data.

**Acceptance Criteria:**
- [ ] Create table with schema:
  ```sql
  CREATE TABLE clicks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    link_id BIGINT NOT NULL,
    click_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_agent TEXT,
    ip_address VARCHAR(45),
    referer TEXT,
    FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE,
    INDEX idx_link_id (link_id),
    INDEX idx_timestamp (click_timestamp)
  );
  ```
- [ ] Create migration file: `V3__create_clicks_table.sql`
- [ ] Consider partitioning strategy for future scaling (document only)

**Technical Notes:**
- VARCHAR(45) supports both IPv4 and IPv6 addresses
- Added `referer` field for richer analytics

---

### TICKET-006: Update GET Endpoint for Telemetry Capture
**Priority:** High  
**Story Points:** 5  
**Type:** Backend
**Dependencies:** TICKET-005

**Description:**  
Modify the redirect endpoint to capture click telemetry before redirecting.

**Acceptance Criteria:**
- [ ] Extract telemetry data from request:
    - User-Agent header
    - Client IP address (consider X-Forwarded-For)
    - Referer header
    - Timestamp
- [ ] Insert telemetry record into `clicks` table
- [ ] Increment `click_count` in `links` table
- [ ] Ensure redirect still happens quickly (consider async processing)
- [ ] Add error handling - redirect should work even if telemetry fails
- [ ] Add unit tests for telemetry capture logic

**Technical Notes:**
```java
// Consider using @Timed annotation for metrics
@GET
@Path("/{shortCode}")
@Timed
public Response redirect(@PathParam("shortCode") String shortCode,
                        @Context HttpServletRequest request) {
    // Implementation here
}
```

---

### TICKET-007: Database Performance Analysis
**Priority:** Medium  
**Story Points:** 2  
**Type:** Performance
**Dependencies:** TICKET-006

**Description:**  
Analyze and document database query performance using EXPLAIN.

**Acceptance Criteria:**
- [ ] Run EXPLAIN on the main lookup query:
  ```sql
  EXPLAIN SELECT id, long_url FROM links WHERE short_code = ?;
  ```
- [ ] Verify index usage on `short_code` column
- [ ] Run EXPLAIN on telemetry insert query
- [ ] Document findings in `docs/performance-analysis.md`
- [ ] Add performance baseline metrics (query execution time)
- [ ] Create simple load test script to generate sample data

**Technical Notes:**
- Use `EXPLAIN ANALYZE` for actual execution statistics
- Consider adding slow query log configuration

---

### TICKET-008: Implement Connection Pooling Monitoring
**Priority:** Low  
**Story Points:** 2  
**Type:** Observability
**Dependencies:** TICKET-002

**Description:**  
Add monitoring and metrics for database connection pool.

**Acceptance Criteria:**
- [ ] Expose HikariCP metrics via Dropwizard metrics
- [ ] Add dashboard/endpoint showing:
    - Active connections
    - Idle connections
    - Connection wait time
    - Connection creation/destruction rate
- [ ] Add alerts for connection pool exhaustion
- [ ] Document monitoring setup

---

### TICKET-009: Documentation - Scaling Strategy
**Priority:** Medium  
**Story Points:** 3  
**Type:** Documentation
**Dependencies:** All database tickets

**Description:**  
Document theoretical scaling approaches for the database layer.

**Acceptance Criteria:**
- [ ] Create `docs/scaling-strategy.md` covering:
    - Read replica setup for scaling reads
    - Sharding strategy based on short_code hash
    - Caching layer considerations (Redis)
    - Telemetry data archival strategy
    - Connection pooling optimization
- [ ] Include architecture diagrams
- [ ] Provide specific MySQL configuration recommendations
- [ ] Reference concepts from "High Performance MySQL" book

**Technical Notes:**
- Focus on practical, implementable strategies
- Include trade-offs for each approach

---

### TICKET-010: Integration Tests for Database Layer
**Priority:** Medium  
**Story Points:** 3  
**Type:** Testing
**Dependencies:** TICKET-006

**Description:**  
Create comprehensive integration tests for database operations.

**Acceptance Criteria:**
- [ ] Set up Testcontainers for MySQL in tests
- [ ] Test cases for:
    - URL creation and retrieval
    - Concurrent click tracking
    - Foreign key constraints
    - Migration execution
    - Connection pool behavior under load
- [ ] Add data-testid attributes where applicable
- [ ] Achieve >80% code coverage for DAO layer

---

## 📊 Sprint Planning Suggestions

### Sprint 1 (Database Foundation)
- TICKET-001: Docker Compose Setup
- TICKET-002: MySQL Connection
- TICKET-003: Links Table Schema
- TICKET-004: Migration Tool

### Sprint 2 (Telemetry Implementation)
- TICKET-005: Clicks Table
- TICKET-006: Telemetry Capture
- TICKET-007: Performance Analysis
- TICKET-010: Integration Tests

### Sprint 3 (Polish & Documentation)
- TICKET-008: Connection Monitoring
- TICKET-009: Scaling Documentation
- Bug fixes and refinements

---

## 🎓 Learning Checkpoints

After completing these tickets, you should be able to discuss:

1. **Database Design Trade-offs:** Why VARCHAR(10) vs TEXT for short_code?
2. **Performance Optimization:** How indexes impact query performance
3. **Schema Evolution:** Benefits of migration tools in team environments
4. **Scaling Patterns:** Read replicas vs sharding strategies
5. **Telemetry Design:** Synchronous vs asynchronous data capture

Remember to commit frequently with clear messages and maintain a project journal documenting your decisions and learnings!