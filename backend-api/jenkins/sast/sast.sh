#!/bin/bash
set -e

echo "******Running SAST with SonarCloud******"

sonar-scanner \
  -Dsonar.projectKey=SergiMPorto_PersonalLibrary \
  -Dsonar.organization=sergimporto \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=$SONAR_TOKEN \
  -Dsonar.sources=. \
  -Dsonar.inclusions=**/*.py,**/*.kt \
  -Dsonar.exclusions=**/.venv/**,**/__pycache__/**,**/build/**,**/.gradle/**

echo "******SAST completed OK******"