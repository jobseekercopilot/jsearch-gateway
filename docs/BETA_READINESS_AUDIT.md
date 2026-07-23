# JSearch Gateway beta-readiness audit

## Blocking findings

- **P0 credential response:** a non-empty API-key default was present in
  current source. It has been removed from this candidate, but rotation and a
  full-history scan are mandatory.
- **P0 provider compliance:** the public product page promotes job-product use
  while general terms restrict copying, aggregation, public display and
  systematic retrieval. The actual account's written permissions, quotas,
  caching, retention, deletion and attribution rules are unresolved.
- **P0 reproducibility:** the System Data generated client is an excluded
  `systemPath` JAR.
- **P1 correctness:** missing credentials are returned as a successful empty
  result, making operator failure indistinguishable from no jobs.
- **P1 API and resilience:** there is no bounded validation, explicit timeout,
  shared deadline, rate limiter, circuit breaker, or controlled retry policy.
- **P1 pagination:** the provider supports cursors but the current Job Service
  path requests only the initial page.
- **P1 coverage/container:** no tests were found; the runtime-only Dockerfile
  uses an unpinned image, runs as root, and has no health check.

## Provider evidence

The audit used the official [JSearch product
page](https://www.openwebninja.com/api/jsearch) and [OpenWeb Ninja
terms](https://www.openwebninja.com/terms). Their apparent tension is not
resolved by inference: account-specific written permission is required.

## Evidence required to close

Clean-clone build/container evidence; versioned contract and drift checks;
credential rotation and full-history scan; written provider permission;
validated queries; deterministic mapping/error/fixture/cursor tests; and
measured deadline, quota, retry and degradation behaviour.

This audit is not a beta-readiness approval.
