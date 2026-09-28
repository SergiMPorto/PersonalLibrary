#!/bin/bash
set -e

echo "******Running Image Scan with Trivy******"

docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v /tmp/trivy-cache:/root/.cache/trivy \
  aquasec/trivy image \
  --exit-code 0 \
  --no-progress \
  --timeout 15m \
  sergimp/milibrary:${BUILD_TAG}

echo "******Trivy scan completed OK******"