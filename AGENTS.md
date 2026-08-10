# AGENTS.md

## Project overview

`dataspace-paris` (ParIS) is INESC TEC's implementation of the International
Data Spaces (IDS) Participant Information Service. It provides a trusted
directory of dataspace participants and connectors in accordance with the IDS
Reference Architecture.

The service is responsible for:

- Storing and retrieving metadata about participant organizations.
- Managing connectors and their relationships with participants.
- Managing people, business identifiers, participant certifications, and
  certification constraints.
- Providing REST endpoints for participant, connector, and certificate
  operations.
- Processing IDS protocol messages for queries, participant updates, and
  description requests.
- Using DAPS tokens, keystores, and truststores for IDS communication.

## Technology stack

- Java 21
- Maven Wrapper
- Spring Boot 2.7.18
- Spring MVC
- Spring Data JPA
- Spring Security with HTTP Basic authentication
- PostgreSQL for the normal runtime
- H2 support for tests
- Springdoc OpenAPI/Swagger
- IDS Messaging Services and IDS Information Model
- Camel IDSCP2 integration
- Docker

The Maven coordinates are:

- Group: `pt.inesctec.idsidentity`
- Artifact: `dataspace-paris`
- POM revision: `2.1.1`

The `CHANGELOG` currently contains releases through `2.1.3`, so verify which
version is authoritative before changing or publishing version metadata.

## Architecture

The main request flow is:

```text
REST controllers / IDS message handlers
                  |
               services
                  |
       Spring Data repositories
                  |
          JPA domain entities
                  |
             PostgreSQL
```

The code uses a conventional layered Spring structure:

- `controller`: REST API entry points.
- `messaging`: handlers for IDS messages.
- `service`: business and persistence operations.
- `repository`: Spring Data repositories.
- `model`: JPA entities.
- `dto`: API and IDS data-transfer representations.
- `converter`: conversion between database, DTO, JSON, and IDS Information
  Model representations.
- `builder`: domain object construction.
- `config`: application, database, and serialization configuration.
- `util`: participant, communication, comparison, and exception helpers.
- `exceptions`: domain-specific exceptions.

## Important files and directories

- `pom.xml`: dependencies, Java level, plugins, and build configuration.
- `src/main/java/inesctec/inestec/paris/ParISApplication.java`: application
  entry point.
- `src/main/java/inesctec/inestec/paris/controller`: REST controllers.
- `src/main/java/inesctec/inestec/paris/messaging`: IDS message handlers.
- `src/main/java/inesctec/inestec/paris/model`: persistent domain model.
- `src/main/resources/application.properties`: runtime, DAPS, certificate,
  security, and IDS settings.
- `src/main/resources/conf/configmodel.json`: IDS connector configuration.
- `src/main/resources/jsonFiles`: participant JSON examples and fixtures.
- `src/main/resources/populatingDatabase`: participant insertion.
- `docker/postgres/docker-compose.yml`: local PostgreSQL service.
- `Dockerfile`: Java 21 runtime image for the packaged application.
- `CHANGELOG`: release history.

## Domain model

The principal entity is `Participant1`, stored in the `participant` table.
Related entities include:

- `Connector`
- `Person`
- `BusinessIdentifier`
- `ParticipantCertification`
- `EvaluationFacility`
- `LogicalConstraint`
- `Constraint`

Participants can have multiple people, identifiers, certifications, and
connectors. A connector may be associated with multiple participants. Preserve
these relationships when modifying entity mappings or service operations.

## REST API

The service runs on port `8081` by default. Swagger UI is configured at
`/api/docs`.

The controllers provide operations for:

- Listing, creating, updating, and deleting participants.
- Managing participant titles and descriptions.
- Listing, creating, updating, and deleting connectors.
- Associating and disassociating connectors and participants.
- Looking up connectors by identifier, endpoint, or keyword.
- Reading participant and connector certificates.
- Health checks.

Most application endpoints are under `/api`. Review controller annotations for
the exact path, request parameters, and response type instead of assuming CRUD
conventions.

## IDS messaging

The handlers in `messaging` support:

- `QueryMessageHandler`: accepts SQL-like IDS queries for participants or
  connectors and returns IDS result messages.
- `ParticipantUpdateMessageHandler`: creates participant records from IDS
  participant-update messages.
- `DescriptionRequestMessageHandler`: returns the description of a requested
  participant. The source notes that query messages supersede this behavior.

Message responses include DAPS Dynamic Attribute Tokens and IDS correlation,
issuer, model-version, and sender metadata. Changes to these handlers should
preserve IDS response semantics and error responses.

## Security and configuration

Spring Security uses HTTP Basic authentication. The following routes are
currently public:

- `/`
- `/api/ids/data`
- `/health`

Other routes require authentication. CSRF is disabled.

The application uses certificate files, a keystore, a truststore, and DAPS
configuration. The files
`src/main/resources/conf/vcese19.inesctec.pt.p12` and
`src/main/resources/conf/truststore.p12` are not expected to be recoverable
from a normal clone and must be recreated or supplied locally.

Never commit real passwords, private keys, certificates, tokens, or local
keystore material. Avoid exposing configured secret values in logs, tests, or
documentation.

The default active profile is `production`. IDSCP2 is disabled by default in
`application.properties`; check the active environment before relying on that
setting.

## Build and run

Use the Maven Wrapper so builds use the repository's expected Maven setup.

On Windows:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

On Unix-like systems:

```sh
./mvnw clean package
./mvnw spring-boot:run
```

To build without running tests:

```powershell
.\mvnw.cmd clean package -DskipTests
```

To run the packaged application:

```powershell
java -jar target/*.jar
```

The repository includes `package.cmd`, `run.cmd`, and `run-jar.cmd` wrappers for
common commands.

## Deploy

Use these two scrips to build and deploy the correspondent docker image:

```powershell
build-publish-snapshot-docker-image.sh
build-publish-versioned-docker-image.sh
```

## Local database

Tests and local development may require PostgreSQL. Start the provided service
from `docker/postgres`:

```powershell
docker compose up -d
```

The compose file exposes PostgreSQL on port `5432` and creates the participant
database. Its credentials are development defaults only and must not be reused
for deployed environments.

## Tests and verification

Run the complete test suite with:

```powershell
.\mvnw.cmd clean test
```

Tests cover controllers, services, domain models, converters, participant JSON
fixtures, certificate handling, and IDS message handlers. When changing a
specific layer, run its focused tests first and then the complete suite when
practical.

For changes that affect persistence or integration behavior, ensure the local
PostgreSQL service is available as described in `README.md`.

After code changes:

1. Compile the project.
2. Run focused tests for the changed behavior.
3. Run the full suite when practical.
4. Check that REST DTOs and IDS Information Model conversion still agree.
5. Verify that participant-connector relationships are not unintentionally
   replaced or removed.

## Development guidance

- Keep controllers focused on HTTP concerns and place business logic in
  services or domain utilities.
- Use repositories through the service layer unless the existing local pattern
  clearly requires otherwise.
- Preserve transaction boundaries for operations that create a participant and
  its related entities.
- Treat `Participant1`, `ParticipantFraunDto`, and the IDS Information Model's
  `Participant` as distinct representations; use the existing converters.
- Preserve UTF-8 handling. Previous releases fixed corruption of accented
  Portuguese characters.
- Be careful with date changes: the application uses `ZonedDateTime`, and
  previous fixes addressed UTC input.
- Update or add tests when changing controller behavior, entity relationships,
  conversion logic, or IDS message responses.
- Do not edit generated files under `target`.

## Known environment caveat

The README reports that building a JAR from a project located on a WSL-mounted
network path can fail in `maven-jar-plugin`. If this occurs, build from a normal
Windows filesystem location.
