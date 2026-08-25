#!/bin/bash
set -e

echo "*******Building Backend FastAPI Image*******"

cd "$WORKSPACE/backend-api/jenkins/build"

docker compose -f docker-compose.yaml build --no-cache

docker tag "personal-library-api:${BUILD_TAG}" "sergimp/milibrary:${BUILD_TAG}"
docker tag "personal-library-api:${BUILD_TAG}" "sergimp/milibrary:latest"

echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker push "sergimp/milibrary:${BUILD_TAG}"
docker push "sergimp/milibrary:latest"

docker logout

echo "************************"
echo "******* Build y push OK *******"
echo "************************"