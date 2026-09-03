#!/bin/bash
set -e

echo "******Running SCA with pip-audit******"
echo "WORKSPACE: $WORKSPACE"

docker run --rm \
  -v "$WORKSPACE/backend-api/api:/app" \
  -w /app \
  python:3.11-slim \
  bash -c "pip install -r requirements.txt --quiet && \
           pip install pip-audit --quiet && \
           pip-audit"

echo "******SCA completed OK******"