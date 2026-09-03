#!/bin/bash

set -e

echo "Running SCA scan..."

docker run --rm -v "$WORKSPACE/backend-api:/app" -w /app python:3.11-slim bash -c "pip install pip-audit && pip-audit -r requirements.txt"

echo "SCA scan completed."