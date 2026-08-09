# JSearch Gateway

## Role in Job Seeker Copilot

| Role | Called by | Calls | Data | Local port |
|---|---|---|---|---:|
| JSearch/RapidAPI provider search boundary | Job Service | JSearch in live mode or System Data fixtures | None | 8102 |

See the central [job-search journey](https://docs.jobseekercopilot.com/journeys/job-search/), [provider integrations](https://docs.jobseekercopilot.com/services/provider-integrations/), and [configuration reference](https://docs.jobseekercopilot.com/operations/configuration/).

JSearch Gateway isolates OpenWeb Ninja JSearch authentication, provider query
construction, and provider response mapping behind the Job Seeker Copilot
provider contract. Fixture mode reads only synthetic System Data responses.

Status: **migration candidate; not beta-ready**. Its System Data client is now
generated from a pinned producer contract. Deterministic offline tests
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

The safe default is `EXTERNAL_PROVIDER_MODE=FIXTURE`, which requires no live
credential and is restricted to non-production use. Enabled `LIVE` mode
requires `JSEARCH_API_KEY` before startup succeeds; it has no non-empty
repository default. `JSEARCH_ENABLED=false` is the provider kill switch.
`JSEARCH_PAGES_PER_SEARCH` defaults to one and is always clamped to the range
one to two. This keeps cursor expansion bounded even when the caller also
follows the returned continuation cursor.
Rotation, restricted evidence, renewal, history verification and incident
procedures are defined in
[`docs/CREDENTIAL_OPERATIONS.md`](docs/CREDENTIAL_OPERATIONS.md).

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and `LICENSE`.
