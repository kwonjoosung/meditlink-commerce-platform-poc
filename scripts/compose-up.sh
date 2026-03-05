#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -z "${JIB_TARGET_ARCH:-}" ]]; then
  case "$(uname -m)" in
    arm64|aarch64) JIB_TARGET_ARCH="arm64" ;;
    x86_64|amd64) JIB_TARGET_ARCH="amd64" ;;
    *) JIB_TARGET_ARCH="arm64" ;;
  esac
fi

echo "[1/2] Jib 로컬 이미지 빌드"
echo " - target arch: ${JIB_TARGET_ARCH}"
./gradlew :meditlink-commerce-core:jibDockerBuild :meditlink-commerce-client:jibDockerBuild -PjibTargetArch="${JIB_TARGET_ARCH}"

echo "[2/2] Docker Compose 기동"
docker compose up -d

echo "완료: client=http://localhost:8080, core=http://localhost:8081, grpc=localhost:9090"
