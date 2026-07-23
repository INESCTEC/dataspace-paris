package inesctec.inestec.paris.controller.restTemplate;

import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.json.ErrorMessage;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.datatype.DatatypeConfigurationException;
import java.net.URISyntaxException;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql({"/remove_data.sql"})
@TestPropertySource(
    locations = "classpath:application-integrationtest.properties")
class ParticipantDTOControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantDTOControllerTest.class);

    //This makes the code run without having the project running
    @LocalServerPort
    private String port;

    @Value("${spring.security.user.name}")
    private String username;
    @Value("${spring.security.user.password}")
    private String password;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ParticipantService participantService;

    private ParticipantConverter participantConverter;

    @Autowired private EvaluationFacilityService evaluationFacilityService;
    @Autowired private ParticipantCertificationService certificationService;
    @Autowired private PersonService personService;
    @Autowired private ConstraintService constraintService;
    @Autowired private LogicalConstraintService logicalConstraintService;
    @Autowired private BusinessIdentifierService businessIdentifierService;
    @Autowired private ConnectorService connectorService;


    TestUtil testUtil;
    ParticipantFraunDto participant;


    /*@BeforeEach
    void clearDatabase(@Autowired JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "connector");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person");

    }*/

    @BeforeEach
    void setUp() throws DatatypeConfigurationException, URISyntaxException {
        testUtil=new TestUtil();
        participantConverter = new ParticipantConverter(this.participantService, this.certificationService, this.logicalConstraintService, this.personService, this.businessIdentifierService);

        participant=new ParticipantFraunDto(testUtil.fraunhoferPartWithOnlyOneConstraint(), Set.of(testUtil.connector_producer()) );
        
    }

    @Test
    @Order(1)
    public void addParticipant() throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString("http://localhost:" + port + "/api/participants/dto");

        JsonConverter jsonConverter=new JsonConverter();

        System.err.println(participant.toString());
        LOGGER.info("Testing method 'addParticipant'");

        LOGGER.info("Testing with a valid Participant");
        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity("http://localhost:" + port + "/api/participants/dto",
                    participant,
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Participant1 insertedPartOnDB=participantService.findParticipantByParticipantIDSID(participant.getParticipant().getId()).get();

        assertThat(participantService.findParticipantByParticipantIDSID(participant.getParticipant().getId())).isPresent();
        assertThat(participantService.findParticipantByParticipantIDSID(participant.getParticipant().getId()).get().getConnectorSet().size()).isEqualTo(1);
        // Check that the Connector ID is associated with the Participant ID
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(participant.getConnectors().stream().findFirst().get()).get().get(0)).isEqualTo(participant.getParticipant().getId().toString());
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(participant.getConnectors().stream().findFirst().get()).get().size()).isEqualTo(1);

        LOGGER.info("Testing with a Participant that already exists");
       response = this.restTemplate
            .withBasicAuth(username, password)
            .postForEntity(builder.build().toString(),
                participant,
                String.class
            );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());

        String errorMessage=new ErrorMessage("Participant with ID '"+participant.getParticipant().getId()+"' already exists").toJson();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(409));
        assertThat(response.getBody()).isEqualTo(errorMessage);


        LOGGER.info("Test adding another Participant that uses the alerady defined Connector");
        ParticipantFraunDto participant2=new ParticipantFraunDto(testUtil.fraunhoferPartComplete_2(), Set.of(testUtil.connector_producer()) );

        response = this.restTemplate
            .withBasicAuth(username, password)
            .postForEntity("http://localhost:" + port + "/api/participants/dto",
                participant2,
                String.class
            );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        //assertThat(response.getBody()).isEqualTo("Participant with ID '"+participantAlreadyExists.getId()+"' already exists");


        assertThat(participantService.findParticipantByParticipantIDSID(participant2.getParticipant().getId())).isPresent();
        assertThat(participantService.findParticipantByParticipantIDSID(participant2.getParticipant().getId()).get().getConnectorSet().size()).isEqualTo(1);
        // Check that the Connector ID is associated with the Participant ID
        assertThat(connectorService.findParticipantIDsByConnectorIdsID(participant2.getConnectors().stream().findFirst().get()).get().size()).isEqualTo(2);
    }

}