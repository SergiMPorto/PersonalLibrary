#!/bin/bash
# start-milibrary-service.sh - Script simplificado para systemd

# Configuración
LOGFILE="/tmp/milibrary-service.log"
PIDFILE="/tmp/milibrary-portforward.pid"

# Función para logging
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOGFILE"
}

# Función para limpiar al salir
cleanup() {
    log  Deteniendo Mi Biblioteca..."
    if [ -f "$PIDFILE" ]; then
        kill $(cat "$PIDFILE") 2>/dev/null
        rm "$PIDFILE"
    fi
    pkill -f "port-forward.*8080" 2>/dev/null
    log " Limpieza completada"
    exit 0
}

# Configurar trap para limpieza
trap cleanup SIGTERM SIGINT

log " Iniciando Mi Biblioteca como servicio..."

# 1. Verificar Minikube
log "1 Verificando Minikube..."
if ! minikube status >/dev/null 2>&1; then
    log "⚡ Iniciando Minikube..."
    minikube start --cpus=2 --memory=4096 >>"$LOGFILE" 2>&1
fi

# 2. Esperar que Minikube esté listo
log " Esperando Minikube..."
for i in {1..30}; do
    if kubectl get nodes | grep Ready >/dev/null 2>&1; then
        log " Minikube listo"
        break
    fi
    sleep 10
done

# 3. Aplicar deployment si no existe
log " Verificando deployment..."
if ! kubectl get deployment milibrary-api-python -n milibrary >/dev/null 2>&1; then
    log " Aplicando deployment..."
    kubectl apply -f /home/sergi/k8s-milabrary/api-python/api-local-deploy.yaml >>"$LOGFILE" 2>&1
fi

# 4. Esperar que el pod esté ejecutándose (sin probar health internamente)
log " Esperando pod..."
for i in {1..40}; do
    if kubectl get pod -n milibrary -l app=milibrary-api-python | grep Running >/dev/null 2>&1; then
        log " Pod ejecutándose"
        break
    fi
    log " Esperando pod... $i/40"
    sleep 15
done

# 5. Dar tiempo adicional para que la aplicación inicie completamente
log " Esperando que la aplicación esté lista..."
sleep 30

# 6. Obtener IP e iniciar port-forward
LOCAL_IP=$(hostname -I | awk '{print $1}')
log "6️⃣ IP local: $LOCAL_IP"

# Limpiar port-forward anterior
pkill -f "port-forward.*8080" 2>/dev/null || true
sleep 2

# Iniciar port-forward
log " Iniciando port-forward..."
kubectl port-forward -n milibrary service/milibrary-api-nodeport 8080:8000 --address=0.0.0.0 >>"$LOGFILE" 2>&1 &
PORT_FORWARD_PID=$!
echo $PORT_FORWARD_PID > "$PIDFILE"

# 7. Verificar que funciona (con timeout)
log " Verificando conexión..."
sleep 10

for i in {1..6}; do
    if timeout 10 curl -f http://$LOCAL_IP:8080/health >/dev/null 2>&1; then
        log  "¡Mi Biblioteca lista en http://$LOCAL_IP:8080!"
        log " Servicio funcionando correctamente"
        break
    else
        log " Verificando conexión... $i/6"
        sleep 10
    fi
done

# 8. Mantener el servicio ejecutándose
log " Servicio Mi Biblioteca ejecutándose"
log " URL: http://$LOCAL_IP:8080"

# Loop infinito para mantener el servicio vivo
while true; do
    if ! kill -0 $PORT_FORWARD_PID 2>/dev/null; then
        log " Port-forward se detuvo, reiniciando..."
        kubectl port-forward -n milibrary service/milibrary-api-nodeport 8080:8000 --address=0.0.0.0 >>"$LOGFILE" 2>&1 &
        PORT_FORWARD_PID=$!
        echo $PORT_FORWARD_PID > "$PIDFILE"
    fi
    sleep 30
done
