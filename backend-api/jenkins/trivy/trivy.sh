#!/bin/bash
set -euo pipefail

echo "******Running Image Scan with Trivy (linux/arm64)******"

docker run --rm \
  -v /tmp/trivy-cache:/root/.cache/trivy \
  aquasec/trivy image \
  --image-src remote \
  --platform linux/arm64 \
  --exit-code 0 \
  --no-progress \
  --timeout 15m \
  sergimp/milibrary:${BUILD_TAG}

echo "******Trivy scan completed OK******"