# identity-access-service

Identity-access-service is the ArkaB2B microservice responsible for identity, authentication, session management, and access-related capabilities in a multi-tenant B2B context. This repository contains the service foundation, runtime configuration, and infrastructure setup for local and containerized execution.

## Overview
- Provides authentication, session lifecycle, and role-based authorization support.
- Exposes security-related endpoints such as login, refresh/logout, token introspection, and JWKS.
- Integrates with PostgreSQL, Redis, and Kafka for persistence, caching, and asynchronous integration.

## Configuration policy (canonical base)
- `.env` is the primary local value source.
- `docker-compose.yml` is the source of truth for local infrastructure.
- `application*.yml` defines profile structure and property consumption.
- Fallbacks are limited to non-sensitive values.
- `prod` does not allow functional fallback for critical secrets/config.

## Spring profiles
- `local`: app on host (Gradle/IDE), dependencies in Docker.
- `docker`: app in Docker Compose network.
- `prod`: externalized production configuration.

Files:
- `src/main/resources/application.yml` (common neutral base)
- `src/main/resources/application-local.yml`
- `src/main/resources/application-docker.yml`
- `src/main/resources/application-prod.yml`

## Run local (Gradle + dockerized dependencies)
```bash
cp .env.example .env
set -a
source .env
set +a
docker compose up -d postgres redis kafka
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --no-daemon
```

## Run fully containerized
```bash
cp .env.example .env
docker compose up -d --build
```

## Validate local Kafka
```bash
docker compose ps kafka
docker compose logs -f kafka
docker compose exec kafka /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server kafka:9092
```

## Responsibility split
- `.env`: concrete local values.
- `docker-compose.yml`: infra topology, published ports, container env.
- `application*.yml`: how app consumes env by profile.
