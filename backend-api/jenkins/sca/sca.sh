#!/bin/bash
set -e

echo "******Running SCA with pip-audit******"

docker run --rm \
  -v "/opt/jenkins_home/workspace/PersonalLibrary-CI/backend-api/api:/app" \
  -w /app \
  python:3.11-slim \
  bash -c "pip install pip-audit --quiet && \
           pip install -r requirements.txt --quiet && \
           pip-audit"

echo "******SCA completed OK******"#!/bin/bash
set -e

echo "******Running SCA with pip-audit******"

docker run --rm \
  -v "/opt/jenkins_home/workspace/PersonalLibrary-CI/backend-api/api:/app" \
  -w /app \
  python:3.11-slim \
  bash -c "pip install pip-audit --quiet && \
           pip install -r requirements.txt --quiet && \
           pip-audit"

echo "******SCA completed OK******"