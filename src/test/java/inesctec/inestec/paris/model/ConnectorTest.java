package inesctec.inestec.paris.model;

import inesctec.inestec.paris.dto.ConnectorDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConnectorTest {

  @Test
  void toJson() {
    String expectedResult="{\n" +
            "  \"connectorUuid\" : \"https://connector/ids/id/1\",\n" +
            "  \"connectorID\" : \"https://connector/id/1\",\n" +
            "  \"connectorIdsEndpoint\" : \"https://connector/endpoint/1\"\n" +
            "}";
    // Creates a new Connector
    Connector connector=new Connector(URI.create("https://connector/id/1"), "https://connector/ids/id/1",
            URI.create("https://connector/endpoint/1"));

    System.out.println(expectedResult);
    System.out.println(connector.toJson());

    assertEquals(connector.toJson(), expectedResult.replaceAll("\n", System.lineSeparator()));
  }

  @Test
  void toSimpleJson() {
    String expectedResult="{\"connectorUuid\":\"https://connector/ids/id/1\",\"connectorID\":\"https://connector/id/1\",\"connectorIdsEndpoint\":\"https://connector/endpoint/1\"}";
    // Creates a new Connector
    Connector connector=new Connector(URI.create("https://connector/id/1"), "https://connector/ids/id/1",
            URI.create("https://connector/endpoint/1"));

    System.out.println(connector.toSimpleJson());
    assertEquals(connector.toSimpleJson(), expectedResult);
  }

  @Test
  void convertToConnectorDto() {

    // Creates a new Connector
    Connector connector=new Connector(URI.create("https://connector/id/1"), "https://connector/ids/id/1",
            URI.create("https://connector/endpoint/1"));

    // Creates the Result Connector -> Expected Result
    ConnectorDto connector1=new ConnectorDto();
    connector1.setConnectorID(URI.create("https://connector/id/1"));
    connector1.setConnectorUuid("https://connector/ids/id/1");
    connector1.setConnectorIdsEndpoint(URI.create("https://connector/endpoint/1"));

    assertEquals(connector.convertToConnectorDto().toString(), connector1.toString());
  }

  @Test
  void equal() {
    // Creates a new Connector
    Connector connector1=new Connector(URI.create("https://connector/id/1"), "https://connector/ids/id/1",
            URI.create("https://connector/endpoint/1"));

    Connector connector2=new Connector(URI.create("https://connector/id/1"), "https://connector/ids/id/1",
            URI.create("https://connector/endpoint/1"));

    assertEquals(connector1, connector2);
    assertTrue(connector1.equals(connector2));

    connector1.setConnectorUuid("test");
    assertNotEquals(connector1, connector2);

    connector2.setConnectorUuid("test");
    assertEquals(connector1, connector2);
    assertTrue(connector1.equals(connector2));
  }
}