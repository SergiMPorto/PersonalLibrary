"""Tests de la API con PostgreSQL mockeada.

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


async def test_get_book_out_of_range_id_returns_422(client, mock_conn):
    # Misma URL que encontró ZAP: el id no cabe en un INTEGER de PostgreSQL
    response = await client.get("/api/books/5734095095508324799")

    assert response.status_code == 422
    mock_conn.fetchrow.assert_not_called()


async def test_search_with_null_byte_is_sanitized(client, mock_conn):
    mock_conn.fetch.return_value = []

    response = await client.get("/api/books", params={"search": "Du\x00ne"})

    assert response.status_code == 200
    assert mock_conn.fetch.call_args.args[1] == "Dune"