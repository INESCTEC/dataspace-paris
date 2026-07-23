package inesctec.inestec.paris.controller;

import inesctec.inestec.paris.DataLoader;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.service.ConnectorService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConnectorControllerTest {

    @Autowired
    ConnectorService service;

    @Test @Order(2)
    void getConnectors() {
        List<URI> connectorsID=new ArrayList();
        service.findAll().forEach(p -> connectorsID.add(p.getConnectorID()));


        connectorsID.forEach(id -> {
            System.out.println(id);
            Optional<Connector> connector=service
                    .findConnectorByConnectorIdsID(String.valueOf(id));
        });

        assertEquals(connectorsID.size(), 2);


    }

    @Test @Order(1)
    void addConnectors() {

        int firstSize=service.size();

        URI producerURI=URI.create("https://stvgodigital.pt/ids/connector/id/producer-05");
        URI subcontractedURI=URI.create("https://stvgodigital.pt/ids/connector/id/subcontracted-05");

        List<Connector> connectorList=new ArrayList<>();
        Connector producer=new Connector(producerURI, "90c63-d2dc-4c98-9d62-a9ba56d5f8ff",
                URI.create("https://vcese11.inesctec.pt:8080/producer-05/api/ids/data"));
        Connector subcontracted=new Connector(subcontractedURI, "cebe2-694e-49c5-b2d9-8070e5b09c69",
                URI.create("https://vcese12.inesctec.pt:8080/subcontracted-05/api/ids/data"));
        connectorList.add(producer);
        connectorList.add(subcontracted);

        service.save(connectorList);

        Optional<Connector> foundConnector = service
                .findConnectorByConnectorIdsID(String.valueOf(producer.getConnectorID()));
        System.out.println(foundConnector.isPresent());
        assertNotNull(foundConnector);
        assertEquals(producer.getConnectorUuid(), foundConnector.get().getConnectorUuid());

        assertEquals(service.size(), (firstSize+connectorList.size()));
    }

    @Test @Order(3)
    void getConnector_UsingID_Unknown() {

        URI producerURI=URI.create("https://stvgodigital.pt/ids/connector/id/test");

        Optional<Connector> foundConnector = service.findConnectorByConnectorIdsID(String.valueOf(producerURI));
        assertEquals(foundConnector.isEmpty(), true);


    }
}