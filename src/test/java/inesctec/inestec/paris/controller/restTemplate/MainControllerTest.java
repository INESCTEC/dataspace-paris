package inesctec.inestec.paris.controller.restTemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.Assertions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MainControllerTest {

    @Value(value="${server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    @Test
    void getPublicSelfDescription() {
        try {

        ResponseEntity<String> response = this.restTemplate
                .getForEntity("http://localhost:" + 8081 + "/",
                        String.class
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
        assertThat(mapper.readTree(response.getBody())).isEqualTo(mapper.readTree(
                "{\n" +
                "  \"@context\" : {\n" +
                "    \"ids\" : \"https://w3id.org/idsa/core/\",\n" +
                "    \"idsc\" : \"https://w3id.org/idsa/code/\"\n" +
                "  },\n" +
                "  \"@type\" : \"ids:ParIS\",\n" +
                "  \"@id\" : \"https://inesctec/paris\",\n" +
                "  \"ids:description\" : [ {\n" +
                "    \"@value\" : \"IDS Connector reference implementation\",\n" +
                "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n" +
                "  } ],\n" +
                "  \"ids:hasDefaultEndpoint\" : {\n" +
                "    \"@type\" : \"ids:ConnectorEndpoint\",\n" +
                "    \"@id\" : \"https://w3id.org/inesctec/stvgodigital/autogen/connectorEndpoint/e5e2ab04-633a-44b9-87d9-a097ae6da3cf\",\n" +
                "    \"ids:accessURL\" : {\n" +
                "      \"@id\" : \"http://localhost:8081/api/ids/data\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"ids:securityProfile\" : {\n" +
                "    \"@id\" : \"https://w3id.org/idsa/code/BASE_SECURITY_PROFILE\"\n" +
                "  },\n" +
                "  \"ids:maintainer\" : {\n" +
                "    \"@id\" : \"https://www.isst.fraunhofer.de/\"\n" +
                "  },\n" +
                "  \"ids:curator\" : {\n" +
                "    \"@id\" : \"https://www.isst.fraunhofer.de/\"\n" +
                "  },\n" +
                "  \"ids:inboundModelVersion\" : [ \"4.2.6\", \"4.2.7\", \"4.2.0\", \"4.2.1\", \"4.1.2\", \"4.0.0\", \"4.1.0\", \"4.2.4\", \"4.2.5\", \"4.2.2\", \"4.2.3\" ],\n" +
                "  \"ids:outboundModelVersion\" : \"4.2.7\",\n" +
                "  \"ids:publicKey\" : {\n" +
                "    \"@type\" : \"ids:PublicKey\",\n" +
                "    \"@id\" : \"https://w3id.org/inesctec/stvgodigital/autogen/publicKey/0f550ce6-cb93-4cce-b55e-6c9376e870ff\",\n" +
                "    \"ids:keyType\" : {\n" +
                "      \"@id\" : \"https://w3id.org/idsa/code/RSA\"\n" +
                "    },\n" +
                "    \"ids:keyValue\" : \"TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF2TlFGQVRnYWFKTzdLYUxrQ29rcGhjYktLOGpQWkxWZ3BnR3VSSjF3RnQ3bnZ5eTFxUU5NdDd6TVRaaElvREFKMTZPN3BwK24wTnU1aXR4TWJPNFh2VjlheFpuUmVZUmxqWGo1Q3RZUlJ2N2xnckZ2cnRhMHVZTDJKeXBSbCtQNVkwRHhRY1Y2cEI1MjRSQVBhS25HanpYcDhuNVFPZWJ4RWR5SXNEOUNRYUFQV3JJSUlZTDJGVkMrQXgydW9NT1JnQzBSUWN3TXdJUjRWQWhRRnBrU3orME9LaFhRcW95dnpuM2djeWtKOEVTMGtWYmIvSU9yK0pTOTZmYnh3WDJNV0pLZElTMHJmYll5Z2NhaU1tNWxJcGNOdFBZOXhvdmNTNld4R25yM3ZRZFlDbFlFOFMrQmJMbDVwc1NRSWpGMTFCbnM1VjNrNHZ1Zk1Yb2toei9CNHdJREFRQUI=\"\n" +
                "  },\n" +
                "  \"ids:version\" : \"8.0.2\",\n" +
                "  \"ids:title\" : [ {\n" +
                "    \"@value\" : \"ParIS\",\n" +
                "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\n" +
                "  } ]\n" +
                "}"));
        }
        catch (Throwable t) {
          Assertions.fail(t.toString());
        }
    }
}