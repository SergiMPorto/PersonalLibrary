#!/bin/bash
set -euo pipefail

echo "******Running SCA with pip-audit******"

# Code path as seen from the Jenkins container
APP_DIR="${WORKSPACE}/backend-api/api"

# Same path as seen from the host (Docker-outside-of-Docker)
JENKINS_HOME_HOST="${JENKINS_HOME_HOST:-/opt/jenkins_home}"
HOST_APP_DIR=$(echo "$APP_DIR" | sed "s|^/var/jenkins_home|${JENKINS_HOME_HOST}|")

# Check that the file exists before running the scan
if [ ! -f "${APP_DIR}/requirements.txt" ]; then
  echo "ERROR: no se encuentra ${APP_DIR}/requirements.txt"
  exit 1
fi

docker run --rm \
  -v "${HOST_APP_DIR}:/app:ro" \
  -w /app \
  python:3.11-slim \
  bash -c "pip install --quiet pip-audit && \
           pip-audit -r requirements.txt || true"

echo "******SCA completed OK******"
