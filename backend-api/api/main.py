from fastapi import FastAPI, HTTPException, Depends, Query
import asyncpg
import os
import time
import logging
from typing import List, Optional
from datetime import datetime
from prometheus_client import Counter, Histogram, Gauge, generate_latest, CONTENT_TYPE_LATEST
from starlette.responses import PlainTextResponse
from pydantic import BaseModel, Field

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Métricas de Prometheus
http_requests_total = Counter(
    'http_requests_total',
    'Total number of HTTP requests',
    ['method', 'endpoint', 'status']
)

http_request_duration_seconds = Histogram(
    'http_request_duration_seconds',
    'HTTP request duration in seconds',
    ['method', 'endpoint']
)

books_total = Gauge(
    'milibrary_books_total',
    'Total number of books in library'
)

api_info = Gauge(
    'milibrary_api_info',
    'API information',
    ['version']
)
api_info.labels(version="1.0.0").set(1)

# Modelos Pydantic
class BookBase(BaseModel):
    google_books_id: Optional[str] = Field(None, alias="googleBooksId")
    title: str
    authors: Optional[str] = None
    description: Optional[str] = None
    thumbnail_url: Optional[str] = Field(None, alias="thumbnailUrl")

class Book(BookBase):
    id: int
    date_added: datetime
    date_updated: datetime

    class Config:
        from_attributes = True
        populate_by_name = True

class HealthCheck(BaseModel):
    status: str
    timestamp: datetime
    database_status: str

# Variable global para el pool de conexiones
db_pool = None

async def get_db_pool():
    global db_pool
    if db_pool is None:
        db_config = {
            "host": os.getenv("DB_HOST", "postgres-service.milibrary.svc.cluster.local"),
            "port": int(os.getenv("DB_PORT", "5432")),
            "user": os.getenv("DB_USER", "library_user"),
            "password": os.getenv("DB_PASSWORD", "securepassword"),
            "database": os.getenv("DB_NAME", "milibrary_db"),
            "min_size": 3,
            "max_size": 10,
        }
        try:
            db_pool = await asyncpg.create_pool(**db_config)
            logger.info("✅ Conexión a base de datos establecida")
        except Exception as e:
            logger.error(f"❌ Error conectando a la base de datos: {e}")
            raise
    return db_pool

# Crear aplicación FastAPI - SIN CORS
app = FastAPI(
    title="Mi Biblioteca API - Mobile App",
    description="API REST para aplicación móvil de biblioteca personal",
    version="1.0.0"
)

# Middleware para logging y métricas
@app.middleware("http")
async def log_and_metrics_middleware(request, call_next):
    start_time = time.time()
    
    response = await call_next(request)
    
    process_time = time.time() - start_time
    endpoint = request.url.path
    method = request.method
    status = str(response.status_code)
    
    # Logging
    logger.info(f"{method} {endpoint} - {status} - {process_time:.3f}s")
    
    # Métricas
    http_requests_total.labels(method=method, endpoint=endpoint, status=status).inc()
    http_request_duration_seconds.labels(method=method, endpoint=endpoint).observe(process_time)
    
    return response

# Dependency para obtener conexión de base de datos
async def get_db():
    pool = await get_db_pool()
    async with pool.acquire() as connection:
        yield connection

# ENDPOINTS PARA APP MÓVIL

@app.get("/health", response_model=HealthCheck)
async def health_check(db: asyncpg.Connection = Depends(get_db)):
    """Health check - usado por Kubernetes y app móvil"""
    try:
        await db.fetchval("SELECT 1")
        return HealthCheck(
            status="healthy",
            timestamp=datetime.now(),
            database_status="connected"
        )
    except Exception as e:
        logger.error(f"Health check failed: {e}")
        raise HTTPException(status_code=500, detail="Database connection failed")

@app.get("/api/books", response_model=List[Book])
async def get_books(
    search: Optional[str] = Query(None, description="Buscar libros"),
    limit: int = Query(20, ge=1, le=100, description="Límite de resultados"),
    offset: int = Query(0, ge=0, description="Offset para paginación"),
    db: asyncpg.Connection = Depends(get_db)
):
    """Obtener libros - optimizado para app móvil"""
    try:
        if search:
            query = """
                SELECT * FROM books
                WHERE title ILIKE '%' || $1 || '%'
                   OR authors ILIKE '%' || $1 || '%'
                ORDER BY date_added DESC
                LIMIT $2 OFFSET $3
            """
            rows = await db.fetch(query, search, limit, offset)
        else:
            query = """
                SELECT * FROM books
                ORDER BY date_added DESC
                LIMIT $1 OFFSET $2
            """
            rows = await db.fetch(query, limit, offset)

        return [dict(row) for row in rows]
    except Exception as e:
        logger.error(f"Error obteniendo libros: {e}")
        raise HTTPException(status_code=500, detail="Error al obtener libros")

@app.post("/api/books", response_model=Book, status_code=201)
async def create_book(book: BookBase, db: asyncpg.Connection = Depends(get_db)):
    """Crear libro - desde app móvil"""
    try:
        # Verificar si ya existe
        if book.google_books_id:
            existing = await db.fetchval(
                "SELECT id FROM books WHERE google_books_id = $1",
                book.google_books_id
            )
            if existing:
                raise HTTPException(status_code=409, detail="El libro ya existe")

        # Insertar nuevo libro
        query = """
            INSERT INTO books
            (google_books_id, title, authors, description, thumbnail_url)
            VALUES ($1, $2, $3, $4, $5)
            RETURNING *
        """
        
        row = await db.fetchrow(
            query,
            book.google_books_id, book.title, book.authors, 
            book.description, book.thumbnail_url
        )

        logger.info(f"✅ Libro creado desde app móvil: {book.title}")
        return dict(row)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error creando libro: {e}")
        raise HTTPException(status_code=500, detail="Error al crear libro")

@app.get("/api/books/{book_id}", response_model=Book)
async def get_book(book_id: int, db: asyncpg.Connection = Depends(get_db)):
    """Obtener libro específico"""
    try:
        row = await db.fetchrow("SELECT * FROM books WHERE id = $1", book_id)
        
        if not row:
            raise HTTPException(status_code=404, detail="Libro no encontrado")
            
        return dict(row)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error obteniendo libro {book_id}: {e}")
        raise HTTPException(status_code=500, detail="Error al obtener libro")

@app.delete("/api/books/{book_id}")
async def delete_book(book_id: int, db: asyncpg.Connection = Depends(get_db)):
    """Eliminar libro"""
    try:
        row = await db.fetchrow(
            "DELETE FROM books WHERE id = $1 RETURNING title",
            book_id
        )
        
        if not row:
            raise HTTPException(status_code=404, detail="Libro no encontrado")
            
        logger.info(f"✅ Libro eliminado desde app móvil: {row['title']}")
        return {"message": f"Libro '{row['title']}' eliminado"}
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error eliminando libro {book_id}: {e}")
        raise HTTPException(status_code=500, detail="Error al eliminar libro")

# ENDPOINT PARA MÉTRICAS (solo Prometheus lo usa)
@app.get("/metrics", response_class=PlainTextResponse)
async def metrics():
    """Métricas para Prometheus - no usado por app móvil"""
    try:
        pool = await get_db_pool()
        async with pool.acquire() as connection:
            total_books = await connection.fetchval("SELECT COUNT(*) FROM books")
            books_total.set(total_books or 0)
    except Exception as e:
        logger.error(f"Error updating metrics: {e}")
    
    return generate_latest().decode('utf-8')

# APP MÓVIL: Endpoints adicionales útiles
@app.get("/api/stats")
async def get_mobile_stats(db: asyncpg.Connection = Depends(get_db)):
    """Estadísticas para mostrar en app móvil"""
    try:
        total_books = await db.fetchval("SELECT COUNT(*) FROM books")
        recent_books = await db.fetch(
            "SELECT title FROM books ORDER BY date_added DESC LIMIT 3"
        )
        
        return {
            "total_books": total_books or 0,
            "recent_books": [book['title'] for book in recent_books]
        }
    except Exception as e:
        logger.error(f"Error obteniendo stats: {e}")
        raise HTTPException(status_code=500, detail="Error al obtener estadísticas")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8000")),
        reload=False
    )
