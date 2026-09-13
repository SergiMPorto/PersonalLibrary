#!/bin/bash
set -e

echo "******Deploying to K3s with Helm******"

helm upgrade milibrary-api \
  $WORKSPACE/backend-api/helm-charts/milibrary-api \
  --set image.tag=${BUILD_TAG} \
  --namespace milibrary \
  --wait

echo "******Deploy completado OK******"