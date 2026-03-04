#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[1/2] Jib 로컬 이미지 빌드"
./gradlew :meditlink-commerce-core:jibDockerBuild :meditlink-commerce-client:jibDockerBuild

echo "[2/2] Docker Compose 기동"
docker compose up -d

echo "완료: client=http://localhost:8080, core=http://localhost:8081, grpc=localhost:9090"
