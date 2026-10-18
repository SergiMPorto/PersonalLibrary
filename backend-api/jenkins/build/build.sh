#!/bin/bash
set -euo pipefail

echo "*******Building Backend FastAPI Image (linux/arm64)*******"

IMAGE="sergimp/milibrary"
PLATFORM="linux/arm64"
BUILDER="jenkins-builder"

# 1. Builder multi-arquitectura propio de Jenkins (se crea solo la primera vez)
if ! docker buildx inspect "$BUILDER" > /dev/null 2>&1; then
  docker buildx create --name "$BUILDER" --driver docker-container
fi

# 2. Login antes del build: buildx sube la imagen directamente al registro
echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin

# 3. Build para arm64 y push en un solo paso
docker buildx build \
  --builder "$BUILDER" \
  --platform "$PLATFORM" \
  --no-cache \
  -t "${IMAGE}:${BUILD_TAG}" \
  -t "${IMAGE}:latest" \
  --push \
  "$WORKSPACE/backend-api/api"

docker logout

echo "************************"
echo "******* Build y push OK (${PLATFORM}) *******"
echo "************************"