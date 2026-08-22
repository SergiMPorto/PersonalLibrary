"""Tests de la API con PostgreSQL mockeada.

No requieren base de datos: se ejecutan en cualquier entorno, incluido
el agente de Jenkins, que no tiene acceso a la PostgreSQL del cluster.
"""
import pytest

pytestmark = pytest.mark.asyncio


async def test_health_check_reports_connected(client, mock_conn):
    mock_conn.fetchval.return_value = 1

    response = await client.get("/health")

    assert response.status_code == 200
    assert response.json()["status"] == "healthy"
    assert response.json()["database_status"] == "connected"


async def test_get_books_returns_empty_list(client, mock_conn):
    mock_conn.fetch.return_value = []

    response = await client.get("/api/books")

    assert response.status_code == 200
    assert response.json() == []


async def test_get_book_missing_id_returns_404(client, mock_conn):
    mock_conn.fetchrow.return_value = None

    response = await client.get("/api/books/999")

    assert response.status_code == 404
    assert response.json()["detail"] == "Libro no encontrado"


async def test_create_book_duplicate_returns_409(client, mock_conn):
    # fetchval devuelve un id -> el libro ya existe en la BD
    mock_conn.fetchval.return_value = 42

    response = await client.post("/api/books", json={
        "googleBooksId": "abc123",
        "title": "Dune",
        "authors": "Frank Herbert",
    })

    assert response.status_code == 409
    assert response.json()["detail"] == "El libro ya existe"


async def test_get_stats_returns_totals_and_recent(client, mock_conn):
    mock_conn.fetchval.return_value = 5
    mock_conn.fetch.return_value = [
        {"title": "Dune"},
        {"title": "Neuromante"},
    ]

    response = await client.get("/api/stats")

    assert response.status_code == 200
    assert response.json()["total_books"] == 5
    assert response.json()["recent_books"] == ["Dune", "Neuromante"]
