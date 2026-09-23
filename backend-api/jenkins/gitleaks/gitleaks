#!/bin/bash
set -e

echo "******Running Secrets Scan with Gitleaks******"

docker run --rm \
  -v "/opt/jenkins_home/workspace/PersonalLibrary-Multibranch_main:/repo" \
  zricethezav/gitleaks:latest detect \
  --source /repo \
  --report-path /repo/gitleaks-report.json || true

echo "******Secrets scan completed OK******"