#!/bin/bash
set -e

echo "*******Running FastAPI Tests (PostgreSQL mocked)*******"

cd "$WORKSPACE/backend-api/api"

# Se construye una imagen de test aparte para no arrastrar pytest
# a la imagen de produccion. Si algun test falla, docker run devuelve
# un codigo distinto de 0 y la etapa se marca en rojo.
docker build -f Dockerfile.test -t milibrary-api-tests:"${BUILD_TAG:-latest}" .

docker run --rm milibrary-api-tests:"${BUILD_TAG:-latest}"

echo "****************************"
echo "** Tests completados OK ****"
echo "****************************"
