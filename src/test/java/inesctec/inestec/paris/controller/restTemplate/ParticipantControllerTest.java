package inesctec.inestec.paris.controller.restTemplate;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.json.ErrorMessage;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParticipantControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantControllerTest.class);

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


    TestUtil testUtil;
    Participant fraunParticipant;


    /*@BeforeEach
    void clearDatabase(@Autowired JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "connector");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person");

    }*/

    @BeforeEach
    void setUp() throws URISyntaxException, DatatypeConfigurationException {
        testUtil=new TestUtil();
        participantConverter = new ParticipantConverter(this.participantService, this.certificationService, this.logicalConstraintService, this.personService, this.businessIdentifierService);

        fraunParticipant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer"))
                ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
                ._legalName_("Fraunhofer-Institut")
                ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
                ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
                ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
                ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
                ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/01"))
                        ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                       .build())
                ._memberPerson_(Util.asList(new de.fraunhofer.iais.eis.PersonBuilder(URI.create("https://person/id/test/connector/fraunhofer"))
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

    @Test  @WithMockUser(value = "testUser")
    public void getListOfParticipants() throws Exception {
        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .getForEntity("http://localhost:" + port + "/api/participants",
                        String.class
                );

        Iterable<Participant1> participantsOnDB=participantService.findAll();
        /*List<String> json = new ArrayList<>();
        AtomicReference<String> res= new AtomicReference<>("");
        participantsOnDB.forEach(p -> json.add(p.toJson_ToString()));
        participantsOnDB.forEach(p -> res.getAndSet(p.toJson_ToString()));

*/
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
        /*System.out.println(json.toString().replace(" ", ""));
        System.out.println(res.get().toString());*/
       // assertThat(response.getBody()).isEqualTo(json.toString().replace(" ", ""));
    }

    @Test  @WithMockUser(value = "testUser")
    public void getParticipant(){
        LOGGER.info("Testing method 'getParticipant'");
        URI participantID=URI.create("https://texpact.pt/ids/participant/id/producer-01");

        Map<String, String> map = new HashMap<>();
        map.put("participantId", "https://test/participant");

        LOGGER.info("Testing with an invalid participant");
        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .getForEntity("http://localhost:" + port + "/api/participants?participantId={participantId}",
                        String.class,
                        map
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Participant not found with ID : 'https://test/participant'");

        LOGGER.info("Testing with a valid participant");
        map.clear();
        map.put("participantId", participantID.toString());
        response = this.restTemplate
                .withBasicAuth(username, password)
                .getForEntity("http://localhost:" + port + "/api/participants?participantId={participantId}",
                        String.class,
                        map
                );
        Optional<Participant1> optionalParticipant=participantService.findParticipantByParticipantIDSID(participantID);
        assertTrue(optionalParticipant.isPresent());


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
        System.out.println(participantConverter.convertToParticipantFraunDto(optionalParticipant.get()));

    }

    @Test  @WithMockUser(value = "testUser")
    @Order(1)
    public void addParticipant() {
        LOGGER.info("Testing method 'getParticipant'");

        Participant participantAlreadyExists=participantConverter.convert(
                participantService.findParticipantByParticipantIDSID(URI.create("https://texpact.pt/ids/participant/id/subcontracted-01")).get()
        );

        LOGGER.info("Testing with a Participant that already exists");
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/participants");
        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity(builder.build().toString(),
                        participantAlreadyExists,
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());

        String errorMessage=new ErrorMessage("Participant with ID '"+participantAlreadyExists.getId()+"' already exists").toJson();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.valueOf(409));
        assertThat(response.getBody()).isEqualTo(errorMessage);


        LOGGER.info("Testing with a valid Participant");
        response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity("http://localhost:" + port + "/api/participants",
                        fraunParticipant,
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        //assertThat(response.getBody()).isEqualTo("Participant with ID '"+participantAlreadyExists.getId()+"' already exists");

    }


    @Order(3)
    @Test  @WithMockUser(value = "testUser")
    public void getConnectorsByParticipant() {
        LOGGER.info("Testing method 'getConnectorsByParticipant'");
        // Made like this to avoid the error:
        //"java.lang.IllegalArgumentException: Not enough variable values available to expand 'connectorID'"

        LOGGER.info("Testing with participant that does not exist");
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/participant/connectors")
                .queryParam("participantId", "https://texpact.pt/ids/participant/id/test");

        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .getForEntity(builder.build().toUri(),
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Participant not found with ID : 'https://texpact.pt/ids/participant/id/test'");


        LOGGER.info("Testing with participant that exists");
        builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/participant/connectors")
                .queryParam("participantId", "https://texpact.pt/ids/participant/id/producer-01");

        LOGGER.info("Testing with participant that exists");
        response = this.restTemplate
                .withBasicAuth(username, password)
                .getForEntity(builder.build().toUri(),
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[{\"connectorUuid\":\"b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff\",\"connectorID\":\"https://texpact.pt/ids/connector/id/producer-01\",\"connectorIdsEndpoint\":\"https://vcese11.inesctec.pt:8080/producer/api/ids/data\"}]");

    }

    @Order(4)
    @Test  @WithMockUser(value = "testUser")
    public void removeParticipant() throws Exception {

        Optional<Participant1> participantOptional=participantService.findParticipantByParticipantIDSID(fraunParticipant.getId());
        // First, assert that the Participant exists
        assertThat(participantOptional.isEmpty())
                .isFalse();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/participants")
                .queryParam("participantId", fraunParticipant.getId());

        this.restTemplate
                .withBasicAuth(username, password)
                .delete(String.valueOf(builder.build().toUri())
                );


        assertThat(participantService.findParticipantByParticipantIDSID(fraunParticipant.getId()).isEmpty())
                .isTrue();

        // Checks that removing a Participant does not remove the Person. Only the rows containing the participant from 'participant_person' table
        assertThat(personService.findPersonsByParticipant(participantOptional.get()).isEmpty()).isTrue();
        fraunParticipant.getMemberPerson().forEach(person -> {
            System.out.println(person.getId());
            System.out.println(personService.findPersonByPersonIdsID(person.getId().toString()));
            System.out.println(personService.findPersonsByParticipant(participantOptional.get()).toString());

            // Make sure that the Person was not removed
            assertThat(personService.findPersonByPersonIdsID(person.getId().toString()).isPresent()).isTrue();

        });

        /*assertThat(participantService.size()).isEqualTo(participantSize-1);
        assertThat(certificationService.size()).isEqualTo(certificationSize-1);
        assertThat(logicalConstraintService.size()).isEqualTo(logicalConsSize-1);
        assertThat(constraintService.size()).isEqualTo(consSize-2); // There were 2 constraints
        assertThat(businessIdentifierService.size()).isEqualTo(businessIdentifierSize-1);

        //Assert That the Person Table is the same AND That there are no Persons associated with the Removed Participant
        assertThat(personService.size()).isEqualTo(personSize);
        assertThat(personService.findPersonsByParticipant(participantToRemove_dbFormat.get()).size()).isEqualTo(0);

        // Assert That No evaluation Facility was removed
        assertThat(evaluationFacilityService.size()).isEqualTo(evSize);*/
    }

    @Test  @WithMockUser(value = "testUser")
    @Order(5)
    public void addParticipant_With_UTC_OnDate() throws Exception {
        LOGGER.info("Testing adding a Participant with UTC on the dates");


        String participantToInsert="{\n" +
                "\"@context\" : {\n" +
                "    \"ids\" : \"https://w3id.org/idsa/core/\",\n" +
                "    \"idsc\" : \"https://w3id.org/idsa/code/\"\n" +
                "  },\n"+
                "      \"@type\": \"ids:Participant\",\n" +
                "      \"@id\": \"https://test/test1\",\n" +
                "      \"ids:version\": null,\n" +
                "      \"ids:participantRefinement\": null,\n" +
                "      \"ids:legalName\": [\n" +
                "        \"Impetus Test Portugal Têxteis, S.A.\"\n" +
                "      ],\n" +
                "      \"ids:corporateEmailAddress\": [\n" +
                "        \"geral@impetus.test1.pt\"\n" +
                "      ],\n" +
                "      \"ids:corporateHomepage\": \"https://www.impetus.test1.pt\",\n" +
                "      \"ids:memberPerson\": [\n" +
                "        {\n" +
                "          \"@type\": \"ids:Person\",\n" +
                "          \"@id\": \"https://test/ids/person/id/gvsdsssss-dwiqe-4cc1-b144-dnalwdq2-1\",\n" +
                "          \"ids:familyName\": \"Fonseca\",\n" +
                "          \"ids:givenName\": \"Serafim\",\n" +
                "          \"ids:emailAddress\": [\n" +
                "            \"sfonseca@impetus.pt\"\n" +
                "          ],\n" +
                "          \"ids:homepage\": null,\n" +
                "          \"ids:phoneNumber\": [\n" +
                "            \"+351 253859210\"\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"ids:businessIdentifier\": [\n" +
                "        {\n" +
                "          \"@type\": \"ids:BusinessIdentifier\",\n" +
                "          \"@id\": \"https://test/ids/businessIdentifier/id/test1\",\n" +
                "          \"ids:identifierNumber\": \"businessIdentifierNumber\",\n" +
                "          \"ids:identifierSystem\": \"businessIdentifierSystem\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"ids:primarySite\": {\n" +
                "        \"@type\": \"ids:Site\",\n" +
                "        \"@id\": \"https://test/site/bafbcb50-b551-40e0-b51f-48d5b2bb15c9\",\n" +
                "        \"ids:siteAddress\": \"Estrada da Praia, n.º 1755, 4740-696 Barqueiros, Portugal\"\n" +
                "      },\n" +
                "      \"ids:vatID\": \"500736685\",\n" +
                "      \"ids:legalForm\": \"e.V.\",\n" +
                "      \"ids:participantCertification\": {\n" +
                "        \"@type\": \"ids:ParticipantCertification\",\n" +
                "        \"@id\": \"https://test/ids/participant/certification/id/test1\",\n" +
                "        \"ids:version\": null,\n" +
                "        \"ids:evaluationFacility\": {\n" +
                "          \"@type\": \"ids:EvaluationFacility\",\n" +
                "          \"@id\": \"https://test/ids/evaluation/facility/id/texpact\"\n" +
                "        },\n" +
                "        \"ids:membershipEnd\": \"2025-03-23T12:23:22.000\",\n" +
                "        \"ids:certificationLevel\": {\n" +
                "          \"properties\": null,\n" +
                "          \"@id\": \"https://w3id.org/idsa/code/PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK\"\n" +
                "        },\n" +
                "        \"ids:description\": [\n" +
                "          {\n" +
                "            \"@value\": \"test1\",\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#string\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"ids:lastValidDate\": \"2022-03-23T12:23:22.000\",\n" +
                "        \"ids:title\": [\n" +
                "          {\n" +
                "            \"@value\": \"test1\",\n" +
                "            \"@type\": \"http://www.w3.org/2001/XMLSchema#string\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      \"ids:description\": [\n" +
                "        {\n" +
                "          \"@value\": \"test1 (fictitious company used by TEST1 for testing purposes)\",\n" +
                "          \"@type\": \"http://www.w3.org/2001/XMLSchema#string\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"ids:jurisdiction\": \"Portugal\",\n" +
                "      \"ids:title\": [\n" +
                "        {\n" +
                "          \"@value\": \"test1\",\n" +
                "          \"@type\": \"http://www.w3.org/2001/XMLSchema#string\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"ids:memberParticipant\": []\n" +
                "    }";
        Serializer serializer=new Serializer();

        JsonConverter jsonConverter=new JsonConverter();
        Participant part= jsonConverter.json2Object(Participant.class, jsonConverter.string2Json(participantToInsert));
        ResponseEntity<String> response = this.restTemplate
                .withBasicAuth(username, password)
                .postForEntity("http://localhost:" + port + "/api/participants",
                        part,
                        String.class
                );

        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        LOGGER.info("Removing Inserted Participant");
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("http://localhost:" + port + "/api/participants")
                .queryParam("participantId", "https://test/test1");

        this.restTemplate
                .withBasicAuth(username, password)
                .delete(String.valueOf(builder.build().toUri())
                );


        assertThat(participantService.findParticipantByParticipantIDSID(
                        URI.create("https://test/test1"))
                .isEmpty())
                .isTrue();

        // Remove Participant
        builder = UriComponentsBuilder
            .fromUriString("http://localhost:" + port + "/api/participants")
            .queryParam("participantId", part.getId());

        this.restTemplate
            .withBasicAuth(username, password)
            .delete(String.valueOf(builder.build().toUri())
            );


        assertThat(participantService.findParticipantByParticipantIDSID(part.getId()).isEmpty())
            .isTrue();
    }

    @Test @Order(2)
    public void updateParticipant() throws Exception {
        URI participantID=URI.create("https://participant/id/test/connector/fraunhofer");
        // Remove Participant, if it exists
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString("http://localhost:" + port + "/api/participants")
            .queryParam("participantId", participantID);
        this.restTemplate
            .withBasicAuth(username, password)
            .delete(String.valueOf(builder.build().toUri())
            );
        assertThat(participantService.findParticipantByParticipantIDSID(participantID).isEmpty())
            .isTrue();

        de.fraunhofer.iais.eis.Constraint constraint= new ConstraintBuilder(URI.create("https://contraint/update/1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.ENDPOINT)
            ._operator_(BinaryOperator.GT)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        de.fraunhofer.iais.eis.Constraint constraint2= new ConstraintBuilder(URI.create("https://contraint/update/2"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.ENDPOINT)
            ._operator_(BinaryOperator.GT)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();
        Participant participant=new de.fraunhofer.iais.eis.ParticipantBuilder(participantID)
            ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
            ._legalName_("Fraunhofer-Institut")
            ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
            ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
            ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
            ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
            ._memberParticipant_(Util.asList(new de.fraunhofer.iais.eis.ParticipantBuilder(
                new URI("https://www.fraunhofer.de/"))
                ._legalForm_("e.V.").build()))
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/01"))
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(
                    DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0))
                ._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                .build())
            ._memberPerson_(Util.asList(new de.fraunhofer.iais.eis.PersonBuilder()
                ._familyName_("Mueller")
                ._givenName_("Peter")
                ._emailAddress_(Util.asList("mueller.peter@emailcontact.de"))
                ._homepage_("https://MemberPerson.de/mueller_peter")
                ._phoneNumber_(Util.asList("017894561237"))
                .build(),
                new de.fraunhofer.iais.eis.PersonBuilder()
                    ._familyName_("Chaves")
                    ._givenName_("Ana")
                    ._emailAddress_(Util.asList("test@emailcontact.de"))
                    ._homepage_("https://MemberPerson.de/test")
                    ._phoneNumber_(Util.asList("65431987"))
                    .build()))
            ._version_("3.0")
            ._legalForm_("e.V.")
            ._businessIdentifier_(new BusinessIdentifierBuilder()
                ._identifierSystem_("CAE")
                ._identifierNumber_("number")
                .build())
            ._vatID_("Vat")
            ._jurisdiction_("PT")
            ._participantRefinement_(
                new LogicalConstraintBuilder(URI.create("https://logical/contraint/id/1"))
                    ._and_(List.of(constraint, constraint2))
                    .build())
            .build();

        ResponseEntity<String> response = this.restTemplate
            .withBasicAuth(username, password)
            .postForEntity("http://localhost:" + port + "/api/participants",
                participant,
                String.class
            );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Participant1 insertedPart=participantService.findParticipant1ByParticipantIdsID(participantID).get();

        // Assert That all values are present
        assertThat(insertedPart.getParticipantIdsID()).isNotNull();
        assertThat(insertedPart.getLegalName()).isNotNull();
        assertThat(insertedPart.getName()).isNotNull();
        assertThat(insertedPart.getDescription()).isNotNull();
        assertThat(insertedPart.getCorporateEmailAddress()).isNotNull();
        assertThat(insertedPart.getCorporateHomepage()).isNotNull();
        assertThat(insertedPart.getPrimarySite()).isNotNull();
        assertThat(insertedPart.getVatID()).isNotNull();
        assertThat(insertedPart.getRegistryCourt()).isNotNull();
        assertThat(insertedPart.getLegalForm()).isNotNull();

        assertThat(insertedPart.getParticipantRefinement()).isNotNull();
        assertThat(insertedPart.getMemberPerson().size()).isEqualTo(2);
        assertThat(insertedPart.getParticipantCertification()).isNotNull();
        assertThat(insertedPart.getBusinessIdentifier()).isNotNull();
        assertThat(insertedPart.getConnectorSet().isEmpty()).isTrue();

        assertThat(insertedPart.getParticipantCertification().getEvaluationFacility()).isNotNull();


        // Start test
        LOGGER.info("Start test");
        participant.setDescription(List.of(new TypedLiteral("Test")));
        participant.setTitle(List.of(new TypedLiteral("Test")));

        participant.getMemberPerson().stream().filter(person -> person.getFamilyName().equals("Chaves")).findAny().get().setFamilyName("Test Update");
        participant.getBusinessIdentifier().stream().filter(bi -> bi.getIdentifierSystem().equals("CAE")).findAny().get().setIdentifierSystem("CAE 2");
        ((LogicalConstraint)participant.getParticipantRefinement()).setAnd(new ArrayList<>());
        ((LogicalConstraint)participant.getParticipantRefinement()).setOr(List.of(
            new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("https://contraint/update/1"))
                ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
                ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                ._operator_(BinaryOperator.EQUALS)
                ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build(),
            new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("https://contraint/update/2"))
                ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
                ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                ._operator_(BinaryOperator.EQUALS)
                ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build()));


        response=this.restTemplate
            .withBasicAuth(username, password)
            .exchange("http://localhost:" + port + "/api/participant", HttpMethod.PUT, new HttpEntity<>(participant),
                String.class
            );


        System.out.println(response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
        
        Participant1 convertedParticipant=participantConverter.convertToDatabaseParticipant(participant);
        Participant1 updatedParticipantOnDB=participantService.findParticipant1ByParticipantIdsID(participantID).get();
        System.out.println(convertedParticipant.toJson());
        System.err.println(updatedParticipantOnDB.toJson());

        assertThat(updatedParticipantOnDB.getParticipantIdsID()).isEqualTo(convertedParticipant.getParticipantIdsID());
        assertTrue(CompareUtil.compareStrings(updatedParticipantOnDB.getLegalName(),convertedParticipant.getLegalName()));
        assertTrue(CompareUtil.compareStrings(updatedParticipantOnDB.getName(), convertedParticipant.getName()));
        assertThat(updatedParticipantOnDB.getDescription().toString()).isEqualTo(convertedParticipant.getDescription().toString());
        assertTrue(CompareUtil.compareStrings(updatedParticipantOnDB.getCorporateEmailAddress() , convertedParticipant.getCorporateEmailAddress()));
        assertThat(updatedParticipantOnDB.getCorporateHomepage()).isEqualTo(convertedParticipant.getCorporateHomepage());
        assertThat(updatedParticipantOnDB.getPrimarySite()).isEqualTo(convertedParticipant.getPrimarySite());
        assertThat(updatedParticipantOnDB.getVatID()).isEqualTo(convertedParticipant.getVatID());
        assertThat(updatedParticipantOnDB.getRegistryCourt()).isEqualTo(convertedParticipant.getRegistryCourt());
        assertThat(updatedParticipantOnDB.getLegalForm()).isEqualTo(convertedParticipant.getLegalForm());

        assertThat(updatedParticipantOnDB.getParticipantRefinement().getLogicalOperator()).isEqualTo(convertedParticipant.getParticipantRefinement().getLogicalOperator());
        assertThat(updatedParticipantOnDB.getParticipantRefinement().getIdsID()).isEqualTo(convertedParticipant.getParticipantRefinement().getIdsID());

        assertThat(updatedParticipantOnDB.getParticipantRefinement().getConstraints().size()).isEqualTo(2);
        assertThat(updatedParticipantOnDB.getParticipantRefinement().getConstraints().stream().map(constraint1 -> constraint1.getConstraintIdsID().toString()).collect(Collectors.toList()))
            .isEqualTo(convertedParticipant.getParticipantRefinement().getConstraints().stream().map(constraint1 -> constraint1.getConstraintIdsID().toString()).collect(Collectors.toList()));
        System.out.println(updatedParticipantOnDB.getParticipantRefinement().getConstraints().stream().map(constraint1 -> constraint1.getLeftOperand().name()).toList());

        assertThat(updatedParticipantOnDB.getParticipantRefinement().getConstraints().stream().map(constraint1 -> constraint1.getLeftOperand().name()).toList().toString()).isEqualTo("[SECURITY_LEVEL, SECURITY_LEVEL]");
        assertThat(updatedParticipantOnDB.getParticipantRefinement().getConstraints().stream().map(inesctec.inestec.paris.model.Constraint::getOperator).toList())
            .isEqualTo(List.of("https://w3id.org/idsa/code/EQUALS", "https://w3id.org/idsa/code/EQUALS"));


        assertTrue(CompareUtil.compareDBPersons(updatedParticipantOnDB.getMemberPerson(), convertedParticipant.getMemberPerson()));
        assertThat(updatedParticipantOnDB.getParticipantCertification()).isEqualTo(convertedParticipant.getParticipantCertification());
        assertThat(updatedParticipantOnDB.getBusinessIdentifier().toString()).isEqualTo(convertedParticipant.getBusinessIdentifier().toString());
        assertTrue(CompareUtil.compareSet(updatedParticipantOnDB.getConnectorSet(), convertedParticipant.getConnectorSet()));

        assertThat(updatedParticipantOnDB.getParticipantCertification().getEvaluationFacility()).isEqualTo(convertedParticipant.getParticipantCertification().getEvaluationFacility());



    }

}