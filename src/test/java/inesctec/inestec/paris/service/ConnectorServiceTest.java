package inesctec.inestec.paris.service;

import de.fraunhofer.iais.eis.CertificationLevel;
import inesctec.inestec.paris.DataLoader;
import inesctec.inestec.paris.builder.ParticipantBuilder;
import inesctec.inestec.paris.builder.ParticipantCertificationBuilder;
import inesctec.inestec.paris.controller.ConnectorController;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.model.BusinessIdentifier;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.ParticipantUtil;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(
    locations = "classpath:application-integrationtest.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql({"/remove_data.sql"})
class ConnectorServiceTest {
    @Autowired
    JdbcTemplate jdbcTemplate;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConnectorServiceTest.class);


    @Autowired
    DataLoader dataLoader;
    /*@Mock
    private ConnectorRepository repository;*/
    @Autowired ConnectorService connectorService;
    @Autowired ParticipantService participantService;
    @Autowired ParticipantCertificationService certificationService;

    @Autowired LogicalConstraintService logicalConstraintService;
    @Autowired PersonService personService;
    @Autowired BusinessIdentifierService businessIdentifierService;

    @Autowired
    ParticipantUtil participantUtil;
    ParticipantConverter participantConverter;

    @BeforeEach
    void setUp() {
        dataLoader.loadDatabase();
    }

    @Test
    void update() throws Exception {
        participantConverter=new ParticipantConverter(participantService, certificationService, logicalConstraintService, personService, businessIdentifierService);
        TestUtil testUtil=new TestUtil();
        Connector oldConnector= testUtil.connector_producer();
        Connector updatedConnector=testUtil.connector_producer();

        // Test with only a Connector
        connectorService.save(oldConnector);
        assertEquals(
                oldConnector,
                connectorService.findConnectorByConnectorIdsID(oldConnector.getConnectorID().toString()).get());

        updatedConnector.setConnectorUuid("test");

        LOGGER.info("Test Updating a Connector without Participant Association...");
        // There are no Participants associated
        connectorService.update(updatedConnector);
        assertThat(updatedConnector)
                .isEqualTo(
                        connectorService.findConnectorByConnectorIdsID(oldConnector.getConnectorID().toString()).get());
        // Asserts that the Connector is not associated with a Participant
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(updatedConnector).get().isEmpty())
                .isTrue();

        LOGGER.info("... Test passed.");

        LOGGER.info("Preparing system...");
        // Clear database
        clearDatabase();
        System.out.println(connectorService.size() + " - " + participantService.size());

        // Test with a Connector and Participant
        Participant1 producer=testUtil.addProducer();
        oldConnector.setParticipantSet(producer);


        //  Save participant and connector
        ParticipantCertification certification=producer.getParticipantCertification();
        certification.setParticipant(producer);

        //producer.setBusinessIdentifier(testUtil.businessIdentifier_Producer());

        ParticipantFraunDto part=new ParticipantFraunDto(participantConverter.convert(producer), oldConnector);
        part.convertConnectorSetToMutable();
        participantUtil.addParticipant(part);
        /*participantService.save(producer);
        connectorService.save(oldConnector);*/
        producer.setConnectorSet(oldConnector);

        Participant1 participantOnDB_AfterInsert=participantService.findParticipantByParticipantIDSID(producer.getParticipantIdsID()).get();
        System.out.println(participantOnDB_AfterInsert.getBusinessIdentifier().toString());
        System.out.println(producer.getLegalName().toString() + " - " + participantOnDB_AfterInsert.getLegalName().toString());
        System.out.println(producer.getCorporateEmailAddress().toString() + " - " + participantOnDB_AfterInsert.getCorporateEmailAddress().toString());
        System.out.println(producer.getBusinessIdentifier().toString() + " - " + participantOnDB_AfterInsert.getBusinessIdentifier().toString());
        System.out.println(producer.getName().toString() + " - " + participantOnDB_AfterInsert.getName().toString());
        System.out.println(producer.getDescription().toString() + " - " + participantOnDB_AfterInsert.getDescription().toString());

        Connector connectorOnDB_AfterInsert=connectorService.findConnectorByConnectorIdsID(oldConnector.getConnectorID().toString()).get();
        assertEquals(
                oldConnector,
                connectorOnDB_AfterInsert);
        assertTrue(CompareUtil.compareDBParticipants(
                producer,
                participantOnDB_AfterInsert));
        assertEquals(
                oldConnector,
                participantOnDB_AfterInsert.getConnectorSet().stream().findFirst().get());
        assertEquals(
                1,
                participantOnDB_AfterInsert.getConnectorSet().size());

        LOGGER.info("...System prepared.");
        LOGGER.info("Test Updating a Connector that has Participant Association...");

        updatedConnector=oldConnector;
        updatedConnector.setConnectorUuid("test");

        connectorService.update(updatedConnector);
        Participant1 participantOnDB_AfterUpdate=participantService.findParticipantByParticipantIDSID(producer.getParticipantIdsID()).get();

        assertEquals(
                updatedConnector,
                connectorService.findConnectorByConnectorIdsID(oldConnector.getConnectorID().toString()).get());
        assertTrue(CompareUtil.compareDBParticipants(
                producer,
                participantOnDB_AfterUpdate));
        assertEquals(
                updatedConnector,
                participantOnDB_AfterUpdate.getConnectorSet().stream().findFirst().get());
        assertEquals(
                1,
                participantOnDB_AfterUpdate.getConnectorSet().size());
        assertEquals(
                1,
                connectorService.size());
        // Asserts that the Connector is still associated with a Participant
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(updatedConnector).get()
                .contains(participantOnDB_AfterUpdate.getParticipantIdsID().toString()))
                .isTrue();

        // Test with a Connector that does not exist

            // Clear database
        /*connectorService.remove();
        participantService.remove();*/
        clearDatabase();
        System.out.println(connectorService.size() + " - " + participantService.size());

        Connector finalUpdatedConnector = updatedConnector;
        Exception exception=assertThrows(ResourceNotFoundException.class,
                () -> connectorService.update(finalUpdatedConnector));
        assertEquals(exception.getMessage(), "Connector not found with ID : '"+ updatedConnector.getConnectorID()+ "'");
    }

    @Test @Order(2)
    void update_test2() {
        TestUtil testUtil=new TestUtil();

        Connector connector= connectorService.findConnectorByConnectorIdsID("https://stvgodigital.pt/ids/connector/id/producer-01").get();
        Participant1 participant=participantService.findParticipantByParticipantIDSID(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01")).get();

        LOGGER.info("Test Updating a Connector that has Participant Association...");

        Connector updatedConnector=connector;
        updatedConnector.setConnectorUuid("test");

        connectorService.update(updatedConnector);
        Participant1 participantOnDB_AfterUpdate=participantService.findParticipantByParticipantIDSID(participant.getParticipantIdsID()).get();

        assertEquals(
                updatedConnector,
                participantOnDB_AfterUpdate.getConnectorSet().stream().findFirst().get());
        assertEquals(
                1,
                participantOnDB_AfterUpdate.getConnectorSet().size());
        assertEquals(
                2,
                connectorService.size());
        // Asserts that the Connector is still associated with a Participant
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(updatedConnector).get()
                .contains(participantOnDB_AfterUpdate.getParticipantIdsID().toString()))
                .isTrue();
    }

    @Test @Order(1)
    void findParticipantIDsByConnectorIdsID(){
        System.out.println(connectorService.findAll().toString());
        System.out.println(participantService.findAll().toString());
        Connector connector= connectorService.findConnectorByConnectorIdsID("https://stvgodigital.pt/ids/connector/id/producer-01").get();
        Participant1 participant=participantService.findParticipantByParticipantIDSID(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01")).get();

        System.out.println("Participant Contains the Connector: "+participantService
                .findParticipantByParticipantIDSID(participant.getParticipantIdsID()).get()
                .getConnectorSet().contains(connector));

        System.out.println("ParticipantID's: "+connectorService
                .findParticipantIDsByConnectorIdsID(connector).get());

        assertThat(connectorService.findParticipantIDsByConnectorIdsID(connector).get()
                .isEmpty())
                .isFalse();

        assertThat(connectorService.findParticipantIDsByConnectorIdsID(connector).get()
                .size())
                .isEqualTo(1);

    }

    public void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "constraint_rule");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "part_refinement");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "logical_constraint");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_certification");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "evaluation_facility");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person_email");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person_phone_number");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_person");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "business_identifier");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_connector");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "connector");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_email");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_legal_name");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_name");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_description");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant");
    }
}