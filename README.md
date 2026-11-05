# Banking Dashboard API

A Spring Boot REST API that connects to an existing PostgreSQL database and exposes banking analytics via REST endpoints.

## Features

- Connects to a live PostgreSQL database with 9 tables and 7 materialized views
- Exposes 6 dashboard endpoints for analytics data from materialized views
- Provides DDL introspection endpoint via `information_schema`
- Supports filtering (tier-based, minimum deposits)
- Refresh materialized views on demand
- Swagger/OpenAPI documentation
- Dockerized for easy deployment

## Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL database (existing)

## Quick Start

```bash
# Build
mvn clean package

# Run
java -jar target/dashboard-api-0.0.1-SNAPSHOT.jar

# Or with custom DB config
java -jar target/dashboard-api-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://host:5432/banking \
  --spring.datasource.username=user \
  --spring.datasource.password=pass
```

## Docker

```bash
docker build -t banking-dashboard-api .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/banking \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  banking-dashboard-api
```

## API Endpoints

### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/customer-portfolio` | Customer portfolio data (filter: `?tier=GOLD`) |
| GET | `/api/dashboard/branch-performance` | Branch performance metrics (filter: `?min_deposits=100000`) |
| GET | `/api/dashboard/monthly-transactions` | Monthly transaction volume |
| GET | `/api/dashboard/loan-risk` | Loan risk summary |
| GET | `/api/dashboard/credit-card-utilization` | Credit card utilization |
| GET | `/api/dashboard/top-customers` | Top customers by AUM (filter: `?tier=PLATINUM`) |
| POST | `/api/dashboard/refresh-views` | Refresh all materialized views |

### Schema

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/schema/tables` | All tables and columns in the banking schema |

### Documentation

| URL | Description |
|-----|-------------|
| `/swagger-ui.html` | Swagger UI |
| `/v3/api-docs` | OpenAPI JSON spec |

## Running Tests

```bash
mvn test
```

## Platform deployment

The platform caller pins the chart, policy and pipeline to OtterWorks commit
`19392116c2197d713811213778a7a1d75eb90cc0`. It passes the checkout's absolute path
as `app_dir` because the pinned pipeline derives the app and image names using
`basename`; `.` would not produce a valid app name. EKS deployment is disabled.

Spring Boot 4.0.9 (Spring Framework 7.0.10 and Tomcat 11.0.27) and springdoc 3.0.3
replace vulnerable dependencies flagged by the platform image scan. Java remains
17, existing endpoint tests are retained, and Jackson uses its compatibility
defaults to preserve the previous API serialization behavior.

`deploy/values.yaml` configures the shared platform chart, including ownership,
resource limits and the database Secret reference. Set the datasource URL and
username for the target environment and provision `banking-database` with key
`password` through the secret store. No database credentials are baked into the
image or manifests.

`deploy/values-ci.yaml` points only the kind run at the disposable PostgreSQL
Deployment in `deploy/kind-deps/`. This isolated test database uses trust
authentication and ephemeral storage; never use it for a real environment.
`deploy/kind-secrets.env` contains a synthetic app-side value only. The kind smoke
checks health, schema introspection and OpenAPI without needing production data.

`GET /health` is the Actuator readiness group: it returns 200 only when the app
is accepting traffic and the datasource is healthy, and 503 otherwise. Both chart
probes use it. Logs are newline-delimited JSON on the console, with no file
appender. The JVM heap and native memory budgets are sized for the 256Mi limit;
only `/tmp` needs to be writable.

```bash
docker build --target test .
docker build -t banking-dashboard-api:local .
docker run --rm --read-only --user 10001:10001 --memory 256m --cpus 0.5 \
  --tmpfs /tmp:rw,nosuid,noexec,size=64m,uid=10001,gid=10001 \
  --env-file /path/to/runtime.env -p 8080:8080 banking-dashboard-api:local
```

The build optionally accepts a BuildKit secret named `maven_settings` for local
Maven mirror settings; normal CI uses Maven Central without an override.

## Architecture

```
controller -> service -> repository -> database
```

- **Controller**: REST endpoints with Swagger annotations
- **Service**: Business logic, filtering, view refresh
- **Repository**: Spring Data JPA repositories
- **Entity**: JPA entities mapped to materialized views
- **DTO**: API response wrapper and schema introspection DTOs
