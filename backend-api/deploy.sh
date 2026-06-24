#!/bin/bash
set -e

echo "Desplegando PersonalLibrary en Minikube..."

kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/postgres-configmap.yaml
kubectl apply -f kubernetes/postgres-secret.yaml
kubectl apply -f kubernetes/postgres-pv.yaml
kubectl apply -f kubernetes/postgres-pvc.yaml
kubectl apply -f kubernetes/postgres-statefulset.yaml
kubectl apply -f kubernetes/postgres-service.yaml

echo "Esperando a que Postgres este listo..."
kubectl wait --for=condition=ready pod \
  -l app=postgres -n milibrary --timeout=120s

echo "Inicializando base de datos..."
kubectl apply -f kubernetes/db-init-job.yaml
kubectl wait --for=condition=complete job/db-init \
  -n milibrary --timeout=60s

echo "Desplegando API..."
kubectl apply -f kubernetes/api-deployment.yaml
kubectl apply -f kubernetes/api-service.yaml

kubectl wait --for=condition=ready pod \
  -l app=milibrary-api -n milibrary --timeout=60s

echo ""
echo "Despliegue completado!"
echo "URL de la API:"
minikube service milibrary-api-service -n milibrary --url
