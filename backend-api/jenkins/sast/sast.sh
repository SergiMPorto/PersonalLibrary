#!/bin/bash
set -e

echo "******Running SAST with SonarCloud******"

sonar-scanner \
  -Dsonar.projectKey=SergiMPorto_PersonalLibrary \
  -Dsonar.organization=sergimporto \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=$SONAR_TOKEN \
  -Dsonar.sources=$WORKSPACE \
  -Dsonar.inclusions=**/*.py \
  -Dsonar.exclusions=**/.venv/**,**/__pycache__/**,**/tests/**,**/build/**,**/.gradle/**,**/node_modules/**

echo "******SAST completed OK******"