#!/bin/bash
set -euo pipefail

echo "******Deploying to K3s with Helm******"

helm upgrade --install milibrary-api "${WORKSPACE}/backend-api/helm-charts/milibrary-api" \
  --namespace milibrary \
  --set-string image.tag="${BUILD_TAG}" \
  --wait \
  --timeout 10m

echo "******Smoke test******"
sleep 15
curl -f -H "Host: api.milibrary.home.arpa" http://192.168.1.200/health

echo "******Deploy completado OK******"