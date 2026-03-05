#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Linux 배포용(amd64) 이미지 빌드"
./gradlew :meditlink-commerce-core:jibDockerBuild :meditlink-commerce-client:jibDockerBuild -PjibTargetArch=amd64
