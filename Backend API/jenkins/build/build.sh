#!/bin/bash
set -e

echo "*******Building Backend FastAPI Image*******"

cd "$WORKSPACE/Backend API/jenkins/build"

docker compose -f docker-compose.yml build --no-cache


echo "****************************"
echo "** Build completado OK *****"
echo "****************************"