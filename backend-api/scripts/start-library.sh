#!/bin/bash
#start-library.sh - Reiniciar Minikube y recrear Mi Biblioteca desde cero

echo "🔄 REINICIO COMPLETO DE MI BIBLIOTECA"
echo "===================================="
BACKUP_FILE="$HOME/milibrary_backup.sql"

# 0. Backup antes de borrar (si existe cluster y DB)
echo "💾 Haciendo backup de la base de datos si existe..."
if kubectl get pods -n milibrary -l app=postgres 2>/dev/null | grep Running >/dev/null; then
    POD_NAME=$(kubectl get pod -n milibrary -l app=postgres -o jsonpath="{.items[0].metadata.name}")
    kubectl exec -n milibrary "$POD_NAME" -- \
        pg_dump -U library_user milibrary_db > "$BACKUP_FILE"
    echo "✅ Backup guardado en $BACKUP_FILE"
else
    echo "⚠️ No se encontró base de datos en ejecución, no se hará backup."
fi
# 1. Detener y limpiar Minikube
echo "1️⃣ Deteniendo Minikube..."
minikube stop 2>/dev/null || true

echo "2️⃣ Eliminando cluster corrupto..."
minikube delete 2>/dev/null || true

echo "3️⃣ Limpiando cache..."
rm -rf ~/.minikube/cache 2>/dev/null || true
rm -rf ~/.kube/cache 2>/dev/null || true

# 2. Iniciar Minikube limpio
minikube start --cpus=2 --memory=2020 --driver=docker
if [ $? -ne 0 ]; then
    echo "❌ Error iniciando Minikube"
    exit 1
fi

echo "5️⃣ Verificando Minikube..."
minikube status
kubectl get nodes

# 3. Crear namespace
echo "6️⃣ Creando namespace..."
kubectl create namespace milibrary

# 4. Aplicar configuraciones de PostgreSQL
echo "7️⃣ Desplegando PostgreSQL..."
cd ~/k8s-milabrary

# Verificar que los archivos existen
for file in postgres-secret.yaml postgres-configmap.yaml postgres-pv.yaml postgres-pvc.yaml postgres-deployment.yaml postgres-service.yaml; do
    if [ ! -f "$file" ]; then
        echo "❌ Archivo $file no encontrado"
        exit 1
    fi
done

kubectl apply -f postgres-secret.yaml
kubectl apply -f postgres-configmap.yaml
kubectl apply -f postgres-pv.yaml
kubectl apply -f postgres-pvc.yaml
kubectl apply -f postgres-deployment.yaml
kubectl apply -f postgres-service.yaml

echo "8️⃣ Esperando que PostgreSQL esté listo..."
kubectl wait --for=condition=ready pod -l app=postgres -n milibrary --timeout=180s

if [ $? -ne 0 ]; then
    echo "❌ PostgreSQL no arrancó correctamente"
    kubectl get pods -n milibrary
    exit 1
fi
echo "⏳ Verificando que PostgreSQL acepte conexiones..."

for i in {1..10}; do
    kubectl exec -n milibrary "$POD_NAME" -- \
        psql -U library_user -d milibrary_db -c "\q" 2>/dev/null && break

    echo "🔁 Esperando que PostgreSQL esté disponible... ($i/10)"
    sleep 5
done

if [ -f "$BACKUP_FILE" ]; then
    echo "♻️ Restaurando backup de la base de datos..."
    

    POD_NAME=$(kubectl get pod -n milibrary -l app=postgres -o jsonpath="{.items[0].metadata.name}")
    echo "📦 Pod de PostgreSQL: $POD_NAME"
    

    echo "📤 Copiando backup al pod..."
    kubectl cp "$BACKUP_FILE" "milibrary/$POD_NAME:/tmp/milibrary_backup.sql"
    if [ $? -ne 0 ]; then
        echo "❌ Error al copiar el archivo dentro del pod"
        exit 1
    fi


    echo "📥 Ejecutando restauración con psql..."
    kubectl exec -n milibrary "$POD_NAME" -- \
        psql -h localhost -U library_user milibrary_db -f /tmp/milibrary_backup.sql

    if [ $? -eq 0 ]; then
        echo "✅ Backup restaurado correctamente"
        
        echo "🔎 Verificando tablas restauradas..."
        kubectl exec -n milibrary "$POD_NAME" -- \
            psql -U library_user -d milibrary_db -c "\dt"
    else
        echo "❌ Fallo al restaurar la base de datos"
        exit 1
    fi
else
    echo "⚠️ No se encontró archivo de backup, base de datos vacía."
fi











# 5. Configurar Docker y construir API
echo "9️⃣ Configurando Docker para Minikube..."
eval $(minikube docker-env)

echo "🔟 Construyendo imagen de la API..."
cd ~/k8s-milabrary/api-python

# Verificar archivos de la API
if [ ! -f "main.py" ]; then
    echo "❌ main.py no encontrado. Creando versión básica..."
    cat > main.py << 'EOF'
from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import asyncpg
import os
from typing import Optional
from datetime import datetime
import logging
from pydantic import BaseModel

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class HealthCheck(BaseModel):
    status: str
    timestamp: datetime
    database_status: str

db_pool = None

async def get_db_pool():
    global db_pool
    if db_pool is None:
        db_config = {
            "host": os.getenv("DB_HOST", "postgres-service"),
            "port": int(os.getenv("DB_PORT", "5432")),
            "user": os.getenv("DB_USER", "library_user"),
            "password": os.getenv("DB_PASSWORD", "tu_password_seguro"),
            "database": os.getenv("DB_NAME", "milibrary_db"),
            "min_size": 1,
            "max_size": 5,
        }
        try:
            db_pool = await asyncpg.create_pool(**db_config)
            logger.info("✅ Conexión a base de datos establecida")
        except Exception as e:
            logger.error(f"❌ Error conectando a la base de datos: {e}")
            raise
    return db_pool

@asynccontextmanager
async def lifespan(app: FastAPI):
    await get_db_pool()
    yield
    if db_pool:
        await db_pool.close()

app = FastAPI(title="Mi Biblioteca API", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

async def get_db():
    pool = await get_db_pool()
    async with pool.acquire() as connection:
        yield connection

@app.get("/health", response_model=HealthCheck)
async def health_check(db: asyncpg.Connection = Depends(get_db)):
    try:
        await db.fetchval("SELECT 1")
        return HealthCheck(
            status="healthy",
            timestamp=datetime.now(),
            database_status="connected"
        )
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        return HealthCheck(
            status="unhealthy",
            timestamp=datetime.now(),
            database_status="disconnected"
        )

@app.get("/api/books")
async def get_books():
    return []

@app.post("/api/books")
async def create_book():
    return {"message": "Book created"}

@app.get("/api/stats")
async def get_stats():
    return {"total_books": 0, "total_authors": 0, "total_languages": 0}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000)
EOF
fi

if [ ! -f "requirements.txt" ]; then
    echo "📄 Creando requirements.txt..."
    cat > requirements.txt << 'EOF'
fastapi==0.104.1
uvicorn[standard]==0.24.0
asyncpg==0.29.0
pydantic==2.5.0
python-multipart==0.0.6
EOF
fi

# Construir imagen
docker build -t milibrary-api-python:v1.1.0 .

if [ $? -ne 0 ]; then
    echo "❌ Error construyendo imagen"
    exit 1
fi

echo "1️⃣1️⃣ Desplegando API..."
kubectl apply -f api-local-deploy.yaml

echo "1️⃣2️⃣ Esperando que la API esté lista..."
for i in {1..20}; do
    if kubectl get pod -n milibrary -l app=milibrary-api-python | grep Running > /dev/null; then
        echo "✅ API está Running!"
        break
    else
        echo "⏳ Esperando API... $i/20"
        sleep 15
    fi
    
    if [ $i -eq 20 ]; then
        echo "❌ API no arrancó. Verificando estado:"
        kubectl get pods -n milibrary
        kubectl describe pod -n milibrary -l app=milibrary-api-python
        exit 1
    fi
done

# 6. Iniciar port-forward
LOCAL_IP=$(hostname -I | awk '{print $1}')
echo "1️⃣3️⃣ Iniciando port-forward..."
pkill -f "port-forward.*8080" 2>/dev/null || true
sleep 3

kubectl port-forward -n milibrary service/milibrary-api-nodeport 8080:8000 --address=0.0.0.0 &
PF_PID=$!

echo "1️⃣4️⃣ Verificando funcionamiento..."
sleep 10

if curl -f http://$LOCAL_IP:8080/health --max-time 15 >/dev/null 2>&1; then
    echo ""
    echo "🎉 ¡MI BIBLIOTECA FUNCIONANDO DESDE CERO!"
    echo "========================================"
    echo "📱 URL para Android: http://$LOCAL_IP:8080"
    echo "🌐 URL para navegador: http://localhost:8080"
    echo "🔄 Port-forward PID: $PF_PID"
    echo ""
    echo "🧪 Prueba:"
    curl -s http://$LOCAL_IP:8080/health | python3 -m json.tool 2>/dev/null || curl -s http://$LOCAL_IP:8080/health
    echo ""
    echo "🎯 ¡Tu app Android ya puede conectarse!"
    echo ""
    echo "Presiona Ctrl+C para detener..."
    wait $PF_PID
else
    echo "❌ La API no responde"
    kubectl get pods -n milibrary
    kubectl logs -n milibrary -l app=milibrary-api-python --tail=20
    kill $PF_PID 2>/dev/null
    exit 1
fi
