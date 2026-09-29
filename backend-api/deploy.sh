#!/bin/bash
set -e

# Despliega la infraestructura base de PersonalLibrary en K3s:
# namespace, PostgreSQL e inicialización de la base de datos.
# La API se despliega con Helm: helm-charts/milibrary-api

echo "Desplegando infraestructura base de PersonalLibrary en K3s..."

kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/postgres-configmap.yaml
kubectl apply -f kubernetes/postgres-secret.yaml
kubectl apply -f kubernetes/postgres-pv.yaml
kubectl apply -f kubernetes/postgres-pvc.yaml
kubectl apply -f kubernetes/postgres-statefulset.yaml
kubectl apply -f kubernetes/postgres-service.yaml

echo "Esperando a que Postgres este listo..."
kubectl wait --for=condition=ready pod \
  -l app=postgres -n milibrary --timeout=180s

echo "Inicializando base de datos..."
kubectl apply -f kubernetes/db-init-job.yaml
kubectl wait --for=condition=complete job/db-init \
  -n milibrary --timeout=60s

echo ""
echo "Infraestructura base desplegada."
echo "Para desplegar la API:"
echo "  helm upgrade --install milibrary-api helm-charts/milibrary-api -n milibrary --set-string image.tag=<TAG>"
