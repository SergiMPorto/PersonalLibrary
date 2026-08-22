"""Fixtures compartidas por todos los tests.

El objetivo es que ningun test tenga que acordarse de limpiar
dependency_overrides: la fixture lo hace siempre, incluso si el test falla.
"""
import pytest
from unittest.mock import AsyncMock
from httpx import AsyncClient, ASGITransport

from main import app, get_db


def get_mock_db(mock_conn):
    """Devuelve la funcion override que FastAPI usara en lugar de get_db.

    Sigue el mismo patron generador que la dependencia real: cede la
    conexion con yield en vez de devolverla con return.
    """
    async def override_get_db():
        yield mock_conn
    return override_get_db


@pytest.fixture
def mock_conn():
    """Conexion falsa a PostgreSQL. Cada test configura lo que necesite."""
    return AsyncMock()


@pytest.fixture
async def client(mock_conn):
    """Cliente HTTP contra la app con la BD mockeada."""
    app.dependency_overrides[get_db] = get_mock_db(mock_conn)
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        yield ac
    app.dependency_overrides.clear()
