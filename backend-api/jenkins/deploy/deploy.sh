#!/bin/bash
set -e
echo "******Deploying to K3********"

kubectl set image deployment/milibrary-api \ 
milibrary-api=sergimp/milibrary:${BUILD_TAG} \ 
-n milibrary

kubectl rollout status deployment/milibrary-api -n milibrary

echo "******Deployment completed********"