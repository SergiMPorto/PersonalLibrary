#!/bin/bash
set -e

echo "*******Building Backend FastAPI Image*******"

cd "$WORKSPACE/backend-api/jenkins/build"

docker compose -f docker-compose.yaml build --no-cache



echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker push "sergimp/milibrary:${BUILD_TAG}"
docker push "sergimp/milibrary:latest"

echo "Usuario: $DOCKER_USERNAME"
echo "Token longitud: ${#DOCKER_TOKEN}"
docker logout

echo "************************"
echo "******* Build y push OK *******"
echo "************************"