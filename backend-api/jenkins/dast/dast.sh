#!/bin/bash
set -euo pipefail

echo "******Running DAST Scan******"

# Carpeta de informes dentro del workspace, vista desde el contenedor de Jenkins
REPORT_DIR="${WORKSPACE}/zap-reports"

# La misma carpeta vista desde el host (Raspberry).
# Docker-outside-of-Docker: el "-v" de docker run lo resuelve el Docker del host.
# Montaje de Jenkins: /opt/jenkins_home (host) -> /var/jenkins_home (contenedor)
JENKINS_HOME_HOST="${JENKINS_HOME_HOST:-/opt/jenkins_home}"
HOST_REPORT_DIR=$(echo "$REPORT_DIR" | sed "s|^/var/jenkins_home|${JENKINS_HOME_HOST}|")

# Crear la carpeta usando la ruta del contenedor, donde Jenkins sí tiene permisos
mkdir -p "$REPORT_DIR"
chmod 777 "$REPORT_DIR"

# El contenedor de ZAP resuelve el host de la API con --add-host (IP de Traefik)
docker run --rm \
  --add-host="api.milibrary.home.arpa:192.168.1.200" \
  -v "${HOST_REPORT_DIR}:/zap/wrk/:rw" \
  ghcr.io/zaproxy/zaproxy:stable \
  zap-api-scan.py \
  -t http://api.milibrary.home.arpa/openapi.json \
  -f openapi \
  -r "zap-report-${BUILD_TAG}.html" \
  -I

echo "******DAST scan completed OK******"