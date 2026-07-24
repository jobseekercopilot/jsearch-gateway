# JSearch Gateway

JSearch Gateway isolates OpenWeb Ninja JSearch authentication, provider query
construction, and provider response mapping behind the Job Seeker Copilot
provider contract. Fixture mode reads only synthetic System Data responses.

Status: **migration candidate; not beta-ready**. Its System Data client is now
generated from a pinned producer contract. Seven deterministic offline tests
cover provider mapping, apply links, cursors, empty/error behavior, the gateway
contract, and populated/empty System Data fixtures. Compliance, validation,
resilience, and remaining beta gaps stay open. See
[`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md).

Its provider-specific ownership and the boundary with canonical Job Service
results are defined in the Infrastructure
[Job Search architecture ADR](https://github.com/jobseekercopilot/infrastructure/blob/develop/docs/adr/0001-job-search-architecture-and-ownership.md).

## Local verification

```bash
./scripts/test-contract-policy.sh
./scripts/verify-contracts.sh
mvn -B clean verify
docker build -t local/jsearch-gateway .
```

The tests use only a loopback synthetic HTTP provider and mocked generated
System Data client. They never call JSearch or require an API key. Referencing
the generated fixture models and method signatures directly makes incompatible
producer contract changes fail compilation.

Live mode requires `JSEARCH_API_KEY`, with no repository default.
`EXTERNAL_PROVIDER_MODE=FIXTURE` is permitted only outside production.

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and `LICENSE`.
