#!/usr/bin/env bash
set -euo pipefail

image="${1:?usage: test-container-health.sh IMAGE PORT}"
port="${2:?usage: test-container-health.sh IMAGE PORT}"
if [[ ! "${port}" =~ ^[0-9]{2,5}$ ]] || (( port < 1 || port > 65535 )); then
  echo "PORT must be an integer between 1 and 65535" >&2
  exit 2
fi

container="jsc-jsearch-health-${$}"
cleanup() {
  docker rm --force "${container}" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker run --detach \
  --name "${container}" \
  --env "SERVER_PORT=${port}" \
  --env EXTERNAL_PROVIDER_MODE=FIXTURE \
  "${image}" >/dev/null

deadline=$((SECONDS + 60))
until docker exec "${container}" \
    wget -q --spider "http://127.0.0.1:${port}/actuator/health"; do
  if [[ "$(docker inspect --format '{{.State.Running}}' "${container}")" != "true" ]]; then
    docker logs "${container}" >&2
    echo "Gateway container stopped before becoming healthy" >&2
    exit 1
  fi
  if (( SECONDS >= deadline )); then
    docker logs "${container}" >&2
    echo "Gateway health command did not pass within 60 seconds" >&2
    exit 1
  fi
  sleep 1
done

# Execute the exact ECS task-definition health command once more after startup.
docker exec "${container}" \
  sh -c "wget -q --spider http://127.0.0.1:${port}/actuator/health || exit 1"
