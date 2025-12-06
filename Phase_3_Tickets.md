## **Epic: Core Event-Driven Refactoring**

### **Ticket 3.3: Add Kafka Producer to Main Service**

**Priority:** High

**Estimated Effort:** 3-4 hours

**Prerequisites:** Tickets 3.1, 3.2 completed

**Description:**
Integrate Kafka producer into the main service to publish click events instead of directly updating the database.

**Tasks:**

- [ ]  Add Kafka client dependencies to main service
- [ ]  Create `EventPublisher` class with proper error handling
- [ ]  Refactor `GET /{shortCode}` endpoint to publish events
- [ ]  Remove direct database insert from the redirect endpoint
- [ ]  Add configuration for Kafka producer settings
- [ ]  Implement proper logging for published events

**Acceptance Criteria:**

- [ ]  `GET /{shortCode}` endpoint publishes event to Kafka topic
- [ ]  Events contain all required fields (linkId, shortCode, timestamp, userAgent, ipAddress)
- [ ]  Producer is properly configured for reliability

**Testing Strategy:**

- [ ]  Unit tests for `EventPublisher` class
- [ ]  Integration tests with embedded Kafka
- [ ]  If kafka down propagate the error upstream so that it's easier to debug

---

### **Ticket 3.4: Create Analytics Consumer Service Structure**

**Priority:** High

**Estimated Effort:** 2-3 hours

**Prerequisites:** Ticket 3.3 completed

**Description:**
Create the basic structure for the separate analytics consumer service using Dropwizard.

**Tasks:**

- [ ]  Create new `telemetry-consumer` Dropwizard application
- [ ]  Set up project structure with proper package organization
- [ ]  Add necessary dependencies (Kafka client, MySQL connector)
- [ ]  Create basic configuration classes
- [ ]  Set up logging and basic health checks
- [ ]  Add to Docker Compose setup

**Acceptance Criteria:**

- [ ]  New service starts successfully as a separate application
- [ ]  Service has proper health check endpoints
- [ ]  Configuration is externalized and environment-specific
- [ ]  Service can connect to both Kafka and MySQL
- [ ]  Proper logging is configured

**Best Practices:**

- [ ]  Follow same project structure as main service
- [ ]  Use dependency injection for testability
- [ ]  Implement proper configuration validation

---

### **Ticket 3.5: Implement Kafka Consumer Logic**

**Priority:** High

**Estimated Effort:** 4-5 hours

**Prerequisites:** Ticket 3.4 completed

**Description:**
Implement the core Kafka consumer that processes click events and updates the database.

**Tasks:**

- [ ]  Create `ClickEventConsumer` class
- [ ]  Implement message deserialization and validation
- [ ]  Add database insertion logic for click events
- [ ]  Implement proper offset management
- [ ]  Add consumer configuration (batch size, timeout, etc.)
- [ ]  Create metrics for processed messages

**Acceptance Criteria:**

- [ ]  Consumer processes messages from `link_clicks` topic
- [ ]  Click events are properly inserted into MySQL `clicks` table
- [ ]  Consumer handles malformed messages gracefully
- [ ]  Offset commits happen only after successful processing
- [ ]  Consumer can be stopped and restarted without data loss
- [ ]  Metrics show processing rates and errors

**Testing Strategy:**

- [ ]  Unit tests for message processing logic
- [ ]  Integration tests with test Kafka topics
- [ ]  Database transaction tests
- [ ]  Consumer group rebalancing tests

---

## **Epic: CI/CD Pipeline**

### **Ticket 3.6: Set Up GitHub Actions Workflow**

**Priority:** Medium

**Estimated Effort:** 2-3 hours

**Prerequisites:** Core functionality working

**Description:**
Create a CI pipeline that builds and tests both services automatically.

**Tasks:**

- [ ]  Create `.github/workflows/ci.yml` file
- [ ]  Set up matrix build for both services
- [ ]  Configure test database for integration tests
- [ ]  Add test coverage reporting
- [ ]  Set up proper caching for dependencies

**Acceptance Criteria:**

- [ ]  Pipeline runs on every push and pull request
- [ ]  Both services build successfully
- [ ]  All unit and integration tests pass
- [ ]  Test coverage is reported
- [ ]  Build artifacts are stored appropriately

**Pipeline Structure:**

```yaml
jobs:
  test-main-service:
    # Main service tests
  test-consumer-service:
    # Consumer service tests
  integration-tests:
    # End-to-end tests with Kafka
