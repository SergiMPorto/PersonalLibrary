#!/usr/bin/env bash
set -euo pipefail

echo "=============================="
echo "  Running IaC Scan — Checkov"
echo "  Build: ${BUILD_TAG:-local}"
echo "=============================="

REPO_ROOT="${WORKSPACE}"
REPORT_DIR="${WORKSPACE}/checkov-reports"

JENKINS_HOME_HOST="${JENKINS_HOME_HOST:-/opt/jenkins_home}"
HOST_REPO_ROOT=$(echo "$REPO_ROOT"   | sed "s|^/var/jenkins_home|${JENKINS_HOME_HOST}|")
HOST_REPORT_DIR=$(echo "$REPORT_DIR" | sed "s|^/var/jenkins_home|${JENKINS_HOME_HOST}|")

mkdir -p "$REPORT_DIR"
chmod 777 "$REPORT_DIR"

docker run --rm \
  -v "${HOST_REPO_ROOT}:/repo:ro" \
  -v "${HOST_REPORT_DIR}:/reports:rw" \
  bridgecrew/checkov:latest \
    --directory /repo/backend-api \
    --config-file /repo/.checkov.yaml \
    --output cli \
    --output junitxml \
    --output-file-path /reports

if [ -f "${REPORT_DIR}/results_junitxml.xml" ]; then
  mv "${REPORT_DIR}/results_junitxml.xml" \
     "${REPORT_DIR}/checkov-report-${BUILD_TAG:-local}.xml"
fi

echo "=============================="
echo "  Checkov scan completed OK"
echo "=============================="