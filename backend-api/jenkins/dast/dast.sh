#!/bin/bash
set -e

echo "******Running DAST Scan******"

mkdir -p /opt/jenkins_home/workspace/PersonalLibrary-Multibranch_main/zap-reports
chmod 777 /opt/jenkins_home/workspace/PersonalLibrary-Multibranch_main/zap-reports

docker run --rm \
  # add DNS enter container to resolve the API hostname to the correct IP address
  --add-host="api.milibrary.home.arpa:192.168.1.200" \
  -v "/opt/jenkins_home/workspace/PersonalLibrary-Multibranch_main/zap-reports:/zap/wrk/:rw" \
  ghcr.io/zaproxy/zaproxy:stable \
  zap-api-scan.py \
  -t http://api.milibrary.home.arpa/openapi.json \
  -f openapi \
  -r zap-report-${BUILD_TAG}.html \
  -I

echo "******DAST scan completed OK******"