#!/bin/bash
set -e

echo "******Running DAST Scan******"

# Convert the container path to the host path
HOST_WORKSPACE=$(echo $WORKSPACE | sed 's|/var/jenkins_home|/opt/jenkins_home|g')

mkdir -p $HOST_WORKSPACE/zap-reports
chmod 777 $HOST_WORKSPACE/zap-reports

# add DNS ins container to resolve the API hostname
docker run --rm \
  --add-host="api.milibrary.home.arpa:192.168.1.200" \
  -v "$HOST_WORKSPACE/zap-reports:/zap/wrk/:rw" \
  ghcr.io/zaproxy/zaproxy:stable \
  zap-api-scan.py \
  -t http://api.milibrary.home.arpa/openapi.json \
  -f openapi \
  -r zap-report-${BUILD_TAG}.html \
  -I

echo "******DAST scan completed OK******"