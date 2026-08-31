#!/bin/bash
set -e

echo "******Running SAST with SonarCloud******"

docker run --rm \
  -e SONAR_TOKEN=$SONAR_TOKEN \
  -v "$WORKSPACE:/usr/src" \
  sonarsource/sonar-scanner-cli:latest \
  -Dsonar.projectKey=SergiMPorto_PersonalLibrary \
  -Dsonar.organization=sergimporto \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=$SONAR_TOKEN \
  -Dsonar.sources=. \
  -Dsonar.inclusions=**/*.py,**/*.kt \
  -Dsonar.exclusions=**/.venv/**,**/__pycache__/**,**/build/**,**/.gradle/**

echo "******SAST completed OK******"