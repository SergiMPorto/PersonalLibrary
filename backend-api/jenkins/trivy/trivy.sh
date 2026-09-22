#!/bin/bash
set -e

echo "******Running Image Scan with Trivy******"

docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image \
  --exit-code 0 \
  --no-progress \
  sergimp/milibrary:${BUILD_TAG}

echo "******Trivy scan completed OK******"