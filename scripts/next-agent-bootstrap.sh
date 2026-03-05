#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[next-agent-bootstrap] workspace: $ROOT_DIR"

if [[ ! -x "./gradlew" ]]; then
  echo "ERROR: ./gradlew not found or not executable"
  exit 1
fi

for cmd in docker java; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "ERROR: required command not found: $cmd"
    exit 1
  fi
done

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker daemon is not reachable"
  exit 1
fi

echo "OK: prerequisites satisfied"
echo
echo "Start references:"
echo "1) MEMORY:  $ROOT_DIR/MEMORY_BANK.md"
echo "2) HANDOFF: $ROOT_DIR/NEXT_AGENT_HANDOFF.md"
echo "3) README:  $ROOT_DIR/README.md"
echo
echo "Recommended first actions:"
echo "- Align scope with user (senior pair style: big picture -> milestones)"
echo "- Propose detailed plan and get approval before edits"
echo
echo "Quick run commands:"
echo "- ./scripts/compose-up.sh"
echo "- docker compose ps -a"
echo "- curl http://localhost:8081/actuator/health"
echo "- curl http://localhost:8080/actuator/health"
