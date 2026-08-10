# dataspace-paris


# Unit and integration Tests  
  
Launch the local postgresql instance (use the docker/postgres/docker-compose.yml on a local WSL instance).  
Run the command 'mvnw clean test' on the command line.  

## Getting started

At startup, ParIS always checks the filesystem directory configured by
`PARTICIPANTS_IMPORT_DIRECTORY` and imports every `.json` and `.jsonc` file it
finds. The default for local execution is `./populatingDatabase`. If the
directory is absent or empty, no participant is imported. Files may contain
either a raw IDS Participant or the
`{ "participant": ..., "connectors": [...] }` structure. Already-imported
participant versions are skipped.

The Docker image defaults to `/data/participants`. A future Compose service can
mount a host directory read-only:

```yaml
services:
  paris:
    volumes:
      - ./populatingDatabase:/data/participants:ro
```

## Common errors:


When creating the jar file (mvn clean install) the following error was being thrown:
```
Failed to execute goal org.apache.maven.plugins:maven-jar-plugin:3.2.2:jar (default-jar) on project inestec.certis: Error assembling JAR

```

This is because the WSL is considered a network mount and it is not possible to create a jar.

To solve this, the project was moved to a windows folder and the problem was solved.

When cloning the git repository into the local repository, files 'src/main/resources/conf/vcese19.inesctec.pt.p12' and 'src/main/resources/conf/truststore.p12' need to be recreated.
