# dataspace-paris


# Unit and integration Tests  
  
Launch the local postgresql instance (use the docker/postgres/docker-compose.yml on a local WSL instance).  
Run the command 'mvnw clean test' on the command line.  

## Getting started


## Common errors:


When creating the jar file (mvn clean install) the following error was being thrown:
```
Failed to execute goal org.apache.maven.plugins:maven-jar-plugin:3.2.2:jar (default-jar) on project inestec.certis: Error assembling JAR

```

This is because the WSL is considered a network mount and it is not possible to create a jar.

To solve this, the project was moved to a windows folder and the problem was solved.

When cloning the git repository into the local repository, files 'src/main/resources/conf/vcese19.inesctec.pt.p12' and 'src/main/resources/conf/truststore.p12' need to be recreated.