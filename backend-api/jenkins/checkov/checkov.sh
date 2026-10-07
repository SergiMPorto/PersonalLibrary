docker run --rm \
  -v "${HOST_REPO_ROOT}:/repo:ro" \
  -v "${HOST_REPORT_DIR}:/reports:rw" \
  bridgecrew/checkov:latest \
    --directory /repo/backend-api \
    --config-file /repo/.checkov.yaml  
    --output cli \
    --output junitxml \
    --output-file-path /reports \
    --soft-fail