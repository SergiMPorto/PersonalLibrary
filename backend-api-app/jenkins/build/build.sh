#!/bin/bash
set -e

echo "*******Building Backend FastAPI Image*******"

cd "$WORKSPACE/backend-api-app/jenkins/build"

docker compose -f docker-compose.yaml build --no-cache


echo "****************************"
echo "** Build completado OK *****"
echo "****************************"