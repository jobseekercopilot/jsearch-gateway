# JSearch Gateway

JSearch Gateway isolates OpenWeb Ninja JSearch authentication, provider query
construction, and provider response mapping behind the Job Seeker Copilot
provider contract. Fixture mode reads only synthetic System Data responses.

Status: **migration candidate; not beta-ready**. Its System Data client is now
generated from a pinned producer contract; compliance, validation, resilience,
and coverage remain open. See
[`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md).

## Local verification

```bash
./scripts/test-contract-policy.sh
./scripts/verify-contracts.sh
mvn -B clean verify
docker build -t local/jsearch-gateway .
```

Live mode requires `JSEARCH_API_KEY`, with no repository default.
`EXTERNAL_PROVIDER_MODE=FIXTURE` is permitted only outside production.

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and `LICENSE`.
