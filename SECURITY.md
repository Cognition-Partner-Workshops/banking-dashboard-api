# Security Policy

## Supported Versions

The Banking Dashboard API is released from the `main` branch. Only the latest
code on `main` (currently `0.0.1-SNAPSHOT`, Spring Boot 3.2.x on Java 17)
receives security fixes.

| Version | Supported |
| ------- | --------- |
| `main` (latest) | :white_check_mark: |
| Older commits / tags | :x: |

## Reporting a Vulnerability

**Please do not open public GitHub issues, pull requests, or discussions for
security vulnerabilities.**

Report vulnerabilities privately using GitHub Private Vulnerability Reporting:

1. Go to the repository's **Security** tab.
2. Click **Report a vulnerability** (Security advisories → New draft advisory).
3. Include the affected endpoint(s) (e.g. `/api/dashboard/*`, `/api/schema/*`,
   `POST /api/dashboard/refresh-views`), steps to reproduce, impact, and any
   suggested fix.

The maintainers listed in `.github/CODEOWNERS` will triage the report.

### What to expect

- Acknowledgement within 3 business days.
- An initial assessment and severity rating within 10 business days.
- Fixes are developed in a private fork attached to the advisory and released
  to `main`; a GitHub Security Advisory (and CVE, where applicable) is published
  once a fix is available.

### Scope

Of particular interest for this service:

- Exposure of customer, account, loan or card data from the PostgreSQL
  materialized views.
- Unauthorized triggering of materialized view refreshes.
- Information disclosure through schema introspection (`information_schema`).
- SQL injection via query parameters (e.g. `tier`, `min_deposits`).
- Leakage of database credentials or other secrets via configuration, logs,
  or error responses.

Vulnerabilities in third-party dependencies should be reported upstream;
Dependabot is configured to keep Maven, Docker and GitHub Actions dependencies
up to date.
