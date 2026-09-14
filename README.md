# PersonalLibrary
## Arquitectura

<p align="center">
  <a href="./images/arquitectura.png">
    <img src="./images/arquitectura.png" alt="Personal Library - Infrastructure" width="100%">
  </a>
</p>
## Branching strategy

- `main` — production; protected, updated only through pull requests
- `develop` — integration branch, default branch of the repository
- `feature/*` — branches off `develop`, merges back into `develop` (squash)
- `hotfix/*` — branches off `main`, merges into `main` and is back-merged into `develop`


## Requirements

- Python 3.11+
- Docker
- Helm 3
- Kubernetes (K3s)

## Structure
- Helm