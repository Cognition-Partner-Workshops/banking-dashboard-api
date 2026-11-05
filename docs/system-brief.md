# System Brief: Banking Dashboard API

*One-page summary for CTO/board review. Claims cite repo files; items marked **(inference)** are not directly stated in code.*

## Purpose

A read-mostly analytics backend that exposes pre-computed banking metrics over REST ([README.md](../README.md#L3)). It serves customer portfolios, branch performance, monthly transaction volume, loan risk, credit-card utilization and top customers by assets under management (AUM), plus a schema-introspection endpoint ([README.md](../README.md#L48-L66)). Consumers are dashboard/BI front ends used by banking operations and management **(inference: no client is in the repo)**.

## Architecture

- **Language/framework:** Java 17 on Spring Boot 3.2.5 with Spring Web, Spring Data JPA and Bean Validation ([pom.xml](../pom.xml#L7-L30)).
- **Layers:** controller → service → repository → database ([README.md](../README.md#L81-L91)). Two controllers: [DashboardController.java](../src/main/java/com/banking/dashboard/controller/DashboardController.java) and [SchemaController.java](../src/main/java/com/banking/dashboard/controller/SchemaController.java); a central error handler in [GlobalExceptionHandler.java](../src/main/java/com/banking/dashboard/config/GlobalExceptionHandler.java).
- **API docs:** SpringDoc OpenAPI / Swagger UI at `/swagger-ui.html` and `/v3/api-docs` ([pom.xml](../pom.xml#L42-L46), [application.yml](../src/main/resources/application.yml#L20-L25)).
- **Deployment:** multi-stage Docker image (JDK build stage, JRE 17 Alpine runtime) listening on port 8080 ([Dockerfile](../Dockerfile)). The Docker build skips tests (`-DskipTests`, [Dockerfile](../Dockerfile#L5)). No CI pipeline, Kubernetes, or IaC definitions exist in the repo (observed: no `.github/` or deploy manifests).
- **Tests:** controller-level tests only ([DashboardControllerTest.java](../src/test/java/com/banking/dashboard/controller/DashboardControllerTest.java), [SchemaControllerTest.java](../src/test/java/com/banking/dashboard/controller/SchemaControllerTest.java)) against in-memory H2 ([test application.yml](../src/test/resources/application.yml#L3)).

## Data stores

- **PostgreSQL (external, pre-existing)**, schema `banking`; Hibernate schema management is disabled (`ddl-auto: none`), so the database is owned elsewhere ([application.yml](../src/main/resources/application.yml#L1-L13)).
- Entities map to six materialized views, e.g. `mv_customer_portfolio` and `mv_top_customers_aum` ([CustomerPortfolio.java](../src/main/java/com/banking/dashboard/entity/CustomerPortfolio.java#L11), [TopCustomerAum.java](../src/main/java/com/banking/dashboard/entity/TopCustomerAum.java#L11)). README cites 9 tables and 7 views overall ([README.md](../README.md#L7)).
- Views contain customer PII and balances: name, email, location, credit score, deposit/loan/card balances ([CustomerPortfolio.java](../src/main/java/com/banking/dashboard/entity/CustomerPortfolio.java#L16-L52)).
- Views are refreshed by calling the DB function `banking.refresh_all_views()` ([ViewRefreshService.java](../src/main/java/com/banking/dashboard/service/ViewRefreshService.java#L16)).

## Integrations

- **Inbound:** unauthenticated HTTP/JSON REST; six GETs, one state-changing `POST /api/dashboard/refresh-views` ([DashboardController.java](../src/main/java/com/banking/dashboard/controller/DashboardController.java#L93-L104)), and `GET /api/schema/tables` ([SchemaController.java](../src/main/java/com/banking/dashboard/controller/SchemaController.java#L26-L31)).
- **Outbound:** PostgreSQL via JDBC only ([pom.xml](../pom.xml#L31-L35)). No other external systems observed.
- **Auth:** no Spring Security or any auth dependency is declared ([pom.xml](../pom.xml#L24-L56)). DB credentials are injected via environment variables (`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`) ([application.yml](../src/main/resources/application.yml#L3-L5)); no secret values are stored in the main config.

## Top 3 operational risks

1. **High — Unauthenticated access to customer PII and admin actions.**
   Any caller that can reach the service can download every customer's name, email, credit score and balances, trigger a DB-wide view refresh, and map the database schema ([pom.xml](../pom.xml#L24-L56), [DashboardController.java](../src/main/java/com/banking/dashboard/controller/DashboardController.java#L39-L104), [SchemaController.java](../src/main/java/com/banking/dashboard/controller/SchemaController.java#L26)). Business impact: data-breach, regulatory (GDPR/GLBA-type) and reputational exposure.
   *Mitigation:* add Spring Security with OAuth2/JWT and role-gate `refresh-views` and `/api/schema`; restrict network exposure.

2. **Medium — Unbounded queries and on-demand refresh threaten availability.**
   Every endpoint loads the full view (`findAll()`) and filters in memory, with no pagination ([DashboardService.java](../src/main/java/com/banking/dashboard/service/DashboardService.java#L46-L56)). Refresh runs synchronously inside a request with no throttling ([ViewRefreshService.java](../src/main/java/com/banking/dashboard/service/ViewRefreshService.java#L14-L17)). **(Inference)** As data grows, or if refresh is called repeatedly, dashboards slow down or time out and the shared DB comes under load.
   *Mitigation:* add pagination with DB-side filtering; move refresh to a scheduled job (with `CONCURRENTLY` where supported) and rate-limit the endpoint.

3. **Medium — Internal error details leak, and the delivery pipeline has no gates.**
   Error responses return raw exception messages to clients ([GlobalExceptionHandler.java](../src/main/java/com/banking/dashboard/config/GlobalExceptionHandler.java#L28-L32), [DashboardController.java](../src/main/java/com/banking/dashboard/controller/DashboardController.java#L100-L102)), which can expose SQL/schema details. The image build skips tests ([Dockerfile](../Dockerfile#L5)), there is no CI in the repo, and tests run on H2 rather than PostgreSQL ([test application.yml](../src/test/resources/application.yml#L3)). Tier filtering uses hard-coded balance thresholds ([DashboardService.java](../src/main/java/com/banking/dashboard/service/DashboardService.java#L89-L96)). Business impact: information disclosure, and regressions can ship to production unnoticed.
   *Mitigation:* return generic error messages with correlation IDs; add CI that runs tests against a Testcontainers PostgreSQL before the image is built.
