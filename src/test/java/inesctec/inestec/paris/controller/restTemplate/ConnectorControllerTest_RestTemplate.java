package inesctec.inestec.paris.controller.restTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.CertificationLevel;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.SiteBuilder;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.DataLoader;
import inesctec.inestec.paris.dto.ConnectorWithListOfParticipantsDto;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.ConnectorService;
import inesctec.inestec.paris.service.ParticipantService;
import inesctec.inestec.paris.util.TestUtil;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConnectorControllerTest_RestTemplate {

    //This makes the code run without having the project running
    @LocalServerPort
    private String port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConnectorService connectorService;

    @Autowired
    private ParticipantService participantService;

    @Value("${spring.security.user.name}")
    private String username;
    @Value("${spring.security.user.password}")
    private String password;

    TestUtil testUtil;
    Participant fraunParticipant;
    Connector connectorToUse;

    /*@BeforeEach
    void clearDatabase(@Autowired JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "connector");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person");

    }*/

    @BeforeEach
    void setUp() throws URISyntaxException, DatatypeConfigurationException {
        testUtil=new TestUtil();
        connectorToUse=new Connector(URI.create("https://connector/controller/test/connector/id/1"), "test-connector",
                URI.create("https://localhost:8080/connector/1/api/ids/data"));

        fraunParticipant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer"))
                ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
                ._legalName_("Fraunhofer-Institut")
                ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
                ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
                ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
                ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
                ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/01"))
                        ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        ._lastValidDate_(
                                DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0)).build())
                ._memberPerson_(Util.asList(new de.fraunhofer.iais.eis.PersonBuilder()
                        ._familyName_("Mueller")
                        ._givenName_("Peter")
                        ._emailAddress_(Util.asList("mueller.peter@emailcontact.de"))
                        ._homepage_("https://MemberPerson.de/mueller_peter")
                        ._phoneNumber_(Util.asList("017894561237"))
                        .build()))
                ._version_("3.0")
                ._legalForm_("e.V.")
                .build();

    }

    @Test
    public void getListOfConnectors() throws Exception {
        HttpEntity<Participant> request =
                new HttpEntity<>(testUtil.fraunhoferPartWithOnlyOneConstraint());

        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .getForEntity("http://localhost:" + port + "/api/connectors",
                        String.class
                );

        Iterable<Connector> connectorsOnDb=connectorService.findAll();
        List<String> json = new ArrayList<>();
        AtomicReference<String> res= new AtomicReference<>("");
        connectorsOnDb.forEach(connector -> json.add(connector.toJson_ToString()));
        connectorsOnDb.forEach(connector -> res.getAndSet(connector.toJson_ToString()));


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
        System.out.println(json.toString().replace(" ", ""));
        System.out.println(res.get().toString());
       // assertThat(response.getBody()).isEqualTo(json.toString().replace(" ", ""));
    }

    @Test  @WithMockUser(value = "testUser")
    public void getConnector(){
        URI connectorID=URI.create("https://texpact.pt/ids/connector/id/producer-01");
        HttpEntity<Participant> request =
                new HttpEntity<>(fraunParticipant);

        Map<String, String> map = new HashMap<>();
        map.put("connectorId", connectorID.toString());

        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .getForEntity("http://localhost:" + port + "/api/connector?connectorId={connectorId}",
                        String.class,
                        map
                );

        Optional<Connector> connectorOnDb=connectorService.findConnectorByConnectorIdsID(connectorID.toString());
        assertTrue(connectorOnDb.isPresent());
        //String json = connectorOnDb.get().toJson_ToString();
        String json = new ConnectorWithListOfParticipantsDto(connectorOnDb.get()).toJson_ToString();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
        System.out.println(json);
        assertThat(response.getBody()).isEqualTo(json);
    }

    @Test  @WithMockUser(value = "testUser")
    @Order(1)
    public void addConnectors() {
        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity("http://localhost:" + port + "/api/connector",
                        connectorToUse,
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(connectorToUse.toJson_ToString());

        assertThat(connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString()).isPresent()).isTrue();
    }

    @Test  @WithMockUser(value = "testUser")
    @Order(2)
    /**
     * Try to register a Connector that already exists connector
     * */
    public void addConnectors_ConnectorAlreadyExists() throws Exception {
        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity("http://localhost:" + port + "/api/connector",
                        connectorToUse,
                        String.class
                );


        System.out.println(response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        System.out.println(response.getBody());
        assertThat(response.getBody()).isEqualTo("Connector with ID : 'https://connector/controller/test/connector/id/1' already exists");
    }

    @Test  @WithMockUser(value = "testUser")
    @Order(3)
    public void associateConnectorWithParticipant() {
        String participantID="https://texpact.pt/ids/participant/id/subcontracted-01";
        Map<String, String> map = new HashMap<>();
        map.put("participantId", participantID);
        map.put("connectorId", connectorToUse.getConnectorID().toString());

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector/participant")
                .queryParam("participantId", "https://texpact.pt/ids/participant/id/subcontracted-01")
                .queryParam("connectorId", connectorToUse.getConnectorID().toString());

        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity(builder.build().toString(),
                        null,
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"connectorUuid\":\"test-connector\",\"connectorID\":\"https://connector/controller/test/connector/id/1\",\"connectorIdsEndpoint\":\"https://localhost:8080/connector/1/api/ids/data\",\"participants\":[\"https://texpact.pt/ids/participant/id/subcontracted-01\"]}");

        System.out.println("ParticipantID's: "+connectorService.findParticipantIDsByConnectorIdsID(connectorToUse).get());
        System.out.println("Participant contains Connector: "+
                participantService.findParticipantByParticipantIDSID(URI.create(participantID)).get()
                .getConnectorSet().toString());

        assertThat(connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString()).isPresent()).isTrue();
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(connectorToUse)).isPresent();
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(connectorToUse).get().size())
                .isEqualTo(1);
    }

    @Test  @WithMockUser(value = "testUser")
    @Order(4)
    /**
     * Try to associate a Connector to a Participant that already has that same connector
     * */
    public void associateConnectorWithParticipant_AddConnectorToParticipantThatIsAlreadyRegistered() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("participantId", "https://texpact.pt/ids/participant/id/subcontracted-01");
        map.put("connectorId", connectorToUse.getConnectorID().toString());

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector/participant")
                .queryParam("participantId", "https://texpact.pt/ids/participant/id/subcontracted-01")
                .queryParam("connectorId", connectorToUse.getConnectorID().toString());

        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity(builder.build().toString(),
                        null,
                        String.class
                );

        System.out.println(response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        System.out.println(response.getBody());
        assertThat(response.getBody()).isEqualTo("Connector with ID : 'https://connector/controller/test/connector/id/1' already registered on this Participant");
    }

    @Test  @WithMockUser(value = "testUser")
    @Order(5)
    public void associateConnectorWithParticipant_ParticipantDoesNotExist() {

        String participantID="https://test/participant/that/does/not/exists";
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector/participant")
                .queryParam("participantId", participantID)
                .queryParam("connectorId", connectorToUse.getConnectorID().toString());

        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity(builder.build().toString(),
                        null,
                        String.class
                );

        System.out.println(response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println(response.getBody());
        assertThat(response.getBody()).isEqualTo("Participant not found with ID : '"+participantID+"'");
    }

    @Test  @WithMockUser(value = "testUser")
    @Order(6)
    public void associateConnectorWithParticipant_ConnectorDoesNotExist() {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector/participant")
                .queryParam("participantId", "https://texpact.pt/ids/participant/id/producer-01")
                .queryParam("connectorId", "http://connector/does/not/exis");

        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity(builder.build().toString(),
                        null,
                        String.class
                );

        System.out.println(response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println(response.getBody());
        assertThat(response.getBody()).isEqualTo("Connector not found with ID : 'http://connector/does/not/exis'");
    }

    @Order(7)
    @Test  @WithMockUser(value = "testUser")
    public void getParticipants() {
        // Made like this to avoid the error:
        //"java.lang.IllegalArgumentException: Not enough variable values available to expand 'connectorID'"

        // Use the Producer Connector to facilitate testing
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connectors/participants")
                .queryParam("connectorID", "https://texpact.pt/ids/connector/id/producer-01");

        List<ParticipantFraunDto> response = this.restTemplate
                .withBasicAuth(username, password)
                .getForObject(builder.build().toUri(),
                        List.class
                );

    }

    @Test  @WithMockUser(value = "testUser")
    @Order(8)
    /*
    Now, add the Connector to the Producer Participant.
    This connector should now belong to two participants (producer and subcontracted)
    * */
    public void associateConnectorWithParticipant_AssociateConnectorToAnotherParticipant()  {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector/participant")
                .queryParam("participantId", "https://texpact.pt/ids/participant/id/producer-01")
                .queryParam("connectorId", connectorToUse.getConnectorID().toString());

        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity(builder.build().toString(),
                        null,
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Optional<Connector> insertedConnector_Op=connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString());

        assertThat(insertedConnector_Op.isPresent())
                .isTrue();

        // The Connector is now associated with 2 participants
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(insertedConnector_Op.get())).isPresent();
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(insertedConnector_Op.get()).get().size())
                .isEqualTo(2);

        assertThat(connectorService.findParticipantIDsByConnectorIdsID(insertedConnector_Op.get()).get()
                .contains("https://texpact.pt/ids/participant/id/subcontracted-01"))
                .isTrue();

        // The Previous Participant should still have the Connector
        assertThat(participantService.findParticipantByParticipantIDSID(URI.create("https://texpact.pt/ids/participant/id/subcontracted-01")).get()
                .getConnectorSet()
                .contains(insertedConnector_Op.get())
        ).isTrue();
        // This Participant should also have the Connector
        assertThat(participantService.findParticipantByParticipantIDSID(URI.create("https://texpact.pt/ids/participant/id/producer-01")).get()
                .getConnectorSet()
                .contains(insertedConnector_Op.get())
        ).isTrue();
    }

    @Order(9)
    @Test  @WithMockUser(value = "testUser")
    public void updateConnector() throws Exception {
        System.out.println("Connector Before test: \n" +
                connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString()).get().toJson_ToString());


        Connector connectorToUpdate=connectorToUse;
        connectorToUpdate.setConnectorUuid("updated-value");

        System.out.println("Connector To update: \n" +
                connectorToUpdate.toJson_ToString());

        System.out.println("Participant List: \n" +
                connectorService.findParticipantIDsByConnectorIdsID(connectorToUpdate).get().toString());
        //"java.lang.IllegalArgumentException: Not enough variable values available to expand 'connectorID'"
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector");

        // I cannot use a path variable. If it belongs to various participants, it rewrites the participants to only use the one on the variable
        // It is better to: 1) create participant, 2) create connector, 3) associate participant and connector
        this.restTemplate
                .withBasicAuth(username, password)
                .put(builder.build().toUri(),
                        connectorToUpdate
                );


        System.out.println("Connector After test: \n" +
                connectorService.findConnectorByConnectorIdsID(connectorToUpdate.getConnectorID().toString()).get().toJson_ToString());


        assertThat(
                connectorService.findConnectorByConnectorIdsID(connectorToUpdate.getConnectorID().toString())
                        .isEmpty())
                .isFalse();

        // Assert that the stored connector is the updated conenctor
        assertThat(
                connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString())
                        .get())
                .isEqualTo(new Connector(URI.create("https://connector/controller/test/connector/id/1"),
                        "updated-value",
                        URI.create("https://localhost:8080/connector/1/api/ids/data")));

        System.out.println("Participant List: \n" +
                connectorService.findParticipantIDsByConnectorIdsID(connectorToUpdate).get().toString());


        List<String> participantIDs=List.of("https://texpact.pt/ids/participant/id/producer-01", "https://texpact.pt/ids/participant/id/subcontracted-01");
        // Assert that the Connector still belongs to the Participants
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(connectorToUpdate).get()
                .containsAll(participantIDs)
        ).isTrue();
    }

    @Order(10)
    @Test  @WithMockUser(value = "testUser")
    public void updateConnector_TriesToUpdateConnectorID() throws Exception {
        Optional<Connector> connectorOnDatabase=connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString());
        System.out.println("Connector Before test: \n" + connectorOnDatabase.get().toJson_ToString());


        Connector connectorToUpdate=connectorOnDatabase.get();
        connectorToUpdate.setConnectorID(URI.create("https://connector/test/should/fail"));

        System.out.println("Connector To Update: \n" + connectorOnDatabase.get().toJson_ToString());

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector");

        // I cannot use a path variable. If it belongs to various participants, it rewrites the participants to only use the one on the variable
        // It is better to: 1) create participant, 2) create connector, 3) associate participant and connector

        /*this.restTemplate
                .withBasicAuth(username, password)
                .put(builder.build().toUri(),
                        connectorToUpdate
                );*/

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(username, password);

        HttpEntity<Connector> requestUpdate = new HttpEntity<>(connectorToUpdate, headers);
        var response=restTemplate.exchange(builder.build().toString(), HttpMethod.PUT, requestUpdate, String.class);

        System.out.println(response.toString());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Connector with ID : 'https://connector/test/should/fail' does not exist");

        // Assert that the stored connector is the updated conenctor
        assertThat(
                connectorService.findConnectorByConnectorIdsID(connectorToUpdate.getConnectorID().toString())
                        .isPresent())
                .isFalse();

    }

    @Order(11)
    @Test  @WithMockUser(value = "testUser")
    public void removeAssociationBetweenConnectorAndParticipant() throws Exception {
        String participantID="https://texpact.pt/ids/participant/id/producer-01";
        // First, assert that the Connector and Participant exist
        assertThat(connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString()).isEmpty())
                .isFalse();
        assertThat(participantService.findParticipantByParticipantIDSID(URI.create(participantID)).isEmpty())
                .isFalse();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector/participant")
                .queryParam("participantId", participantID)
                .queryParam("connectorId", connectorToUse.getConnectorID().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(username, password);

        HttpEntity<Connector> requestUpdate = new HttpEntity<>(headers);
        var response=restTemplate.exchange(builder.build().toString(), HttpMethod.DELETE,
                requestUpdate, String.class);

        System.out.println(response.toString());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Assert that the Participant is not on the Connector's list of Participants
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(connectorToUse).get().contains(participantID))
                .isFalse();
    }

    @Order(12)
    @Test  @WithMockUser(value = "testUser")
    public void removeAssociationBetweenConnectorAndParticipant_ConnectorDoesNotExist() throws Exception {
        String participantID="https://texpact.pt/ids/participant/id/producer-01";

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector/participant")
                .queryParam("participantId", participantID)
                .queryParam("connectorId", "https://connector/does/not/exists");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(username, password);

        HttpEntity<Connector> requestUpdate = new HttpEntity<>(headers);
        var response=restTemplate.exchange(builder.build().toString(), HttpMethod.DELETE,
                requestUpdate, String.class);

        System.out.println(response.toString());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Connector not found with ID : 'https://connector/does/not/exists'");
    }

    @Order(13)
    @Test  @WithMockUser(value = "testUser")
    public void removeAssociationBetweenConnectorAndParticipant_ParticipantDoesNotExist() throws Exception {
        String participantID="https://part/does/not/exist";

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector/participant")
                .queryParam("participantId", participantID)
                .queryParam("connectorId", connectorToUse.getConnectorID().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(username, password);

        HttpEntity<Connector> requestUpdate = new HttpEntity<>(headers);
        var response=restTemplate.exchange(builder.build().toString(), HttpMethod.DELETE,
                requestUpdate, String.class);

        System.out.println(response.toString());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Participant not found with ID : '" + participantID + "'");
    }

    @Order(14)
    @Test  @WithMockUser(value = "testUser")
    public void removeConnector_ConnectorDoesNotExist() throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector")
                .queryParam("connectorId", "https://connector/does/not/exists");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(username, password);

        HttpEntity<Connector> requestUpdate = new HttpEntity<>(headers);
        var response=restTemplate.exchange(builder.build().toString(), HttpMethod.DELETE,
                requestUpdate, String.class);

        System.out.println(response.toString());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Connector not found with ID : 'https://connector/does/not/exists'");
    }

    @Order(15)
    @Test  @WithMockUser(value = "testUser")
    public void removeConnector() throws Exception {
        // First, assert that the Connector exists
        assertThat(connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString()).isEmpty())
                .isFalse();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/connector")
                .queryParam("connectorId", connectorToUse.getConnectorID());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(username, password);

        HttpEntity<Connector> requestUpdate = new HttpEntity<>(headers);
        var response=restTemplate.exchange(builder.build().toString(), HttpMethod.DELETE,
                requestUpdate, String.class);

        System.out.println(response.toString());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(connectorService.findConnectorByConnectorIdsID(connectorToUse.getConnectorID().toString())
                .isEmpty())
                .isTrue();
        //assertThat(response.getBody()).isEqualTo(json);
    }


}