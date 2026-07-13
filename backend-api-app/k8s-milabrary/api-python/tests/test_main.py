import pytest
from httpx import AsyncClient, ASGITransport
from unittest.mock import AsyncMock
from main import app, get_db

pytestmark = pytest.mark.asyncio

def get_mock_db(mock_conn):
    async def override_get_db():
        yield mock_conn
    return override_get_db


async def test_health_check():
    mock_conn = AsyncMock()
    mock_conn.fetchval.return_value = 1
    app.dependency_overrides[get_db] = get_mock_db(mock_conn)

    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as client:
        response = await client.get("/health")

    app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json()["status"] == "healthy"
    assert response.json()["database_status"] == "connected"


async def test_get_books_empty():
    mock_conn = AsyncMock()
    mock_conn.fetch.return_value = []
    app.dependency_overrides[get_db] = get_mock_db(mock_conn)

    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as client:
        response = await client.get("/api/books")

    app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json() == []


async def test_get_book_not_found():
    mock_conn = AsyncMock()
    mock_conn.fetchrow.return_value = None
    app.dependency_overrides[get_db] = get_mock_db(mock_conn)

    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as client:
        response = await client.get("/api/books/999")

    app.dependency_overrides.clear()

    assert response.status_code == 404
    assert response.json()["detail"] == "Libro no encontrado"


async def test_get_books_existing_id():
    mock_conn = AsyncMock()
    mock_conn.fetchval.return_value = 42
    app.dependency_overrides[get_db] = get_mock_db(mock_conn)

    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as client:
        response = await client.post("/api/books", json={
            "title": "Dune",
            "googleBooksId": "abc123",
            "authors": "Frank Herbert"
        })

    app.dependency_overrides.clear()

    assert response.status_code == 409
    assert response.json()["detail"] == "El libro ya está guardado"


async def test_get_stats():
    mock_conn = AsyncMock()
    mock_conn.fetchval.side_effect = [5, 3, 2, "Dune"]
    app.dependency_overrides[get_db] = get_mock_db(mock_conn)

    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as client:
        response = await client.get("/api/stats")

    app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json()["total_books"] == 5
    assert response.json()["total_authors"] == 3
    assert response.json()["total_categories"] == 2
    assert response.json()["most_popular_book"] == "Dune"