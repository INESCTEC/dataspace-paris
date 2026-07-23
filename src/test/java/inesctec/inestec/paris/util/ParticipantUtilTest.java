package inesctec.inestec.paris.util;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.converter.XMLGregorianCalendarConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import inesctec.inestec.paris.exceptions.ResourceAlreadyExistsException;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.LogicalConstraint;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.service.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.xml.datatype.DatatypeFactory;
import java.net.URI;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
class ParticipantUtilTest {

    @Autowired
    EvaluationFacilityService evaluationFacilityService;
    @Autowired
    PersonService personService;
    @Autowired
    ParticipantService participantService;
    @Autowired
    ParticipantCertificationService certificationService;
    @Autowired
    ConstraintService constraintService;
    @Autowired
    LogicalConstraintService logicalConstraintService;
    @Autowired
    ConnectorService connectorService;
    @Autowired
    BusinessIdentifierService businessIdentifierService;

    private JsonConverter jsonConverter;
    private TestUtil testUtil;

    @Autowired
    private ParticipantUtil participantUtil;
    private XMLGregorianCalendarConverter calendarConverter;
    private ZonedDateTime certificationEndDate;
    private ZonedDateTime certificationDate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantUtil.class);

    /*@BeforeClass
    public static void init() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }*/

    void clearDatabase() {
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

    @Sql(scripts = "/remove_data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @BeforeEach
    void startVariables() {
        //clearDatabase();
        jsonConverter = new JsonConverter();
        testUtil = new TestUtil();
        calendarConverter = new XMLGregorianCalendarConverter();
        certificationDate = ZonedDateTime.of(2021, 06, 30, 12, 23, 22, 0, ZoneId.of("UTC"));
        certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

        //participantUtil.removeAllDataFromDB();
    }

    private void delete() {
        // remove connector values -> missing since I'm not using this table
        constraintService.remove();
        logicalConstraintService.remove();
        certificationService.remove();
        evaluationFacilityService.remove();
        personService.remove();
        participantService.remove();
        businessIdentifierService.remove();

    }

    /**
     * Tests:
     * - Add a Participant that is already on the DB
     * - Add a Certification without an EV when it is not on the DB
     * - Add a Certification with an EV when it is not on the DB
     * - Add a Certification with an EV when it is already on the DB
     * - Add a Certification when the EV is already on the DB
     */
    @Test
    @Sql(scripts = "/remove_data.sql")
    void participantCertificationAndEvaluationFacility() throws Exception {
        // Prepare Variables
        Participant participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer/01"))
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
            ._version_("3.0")
            ._legalForm_("e.V.")
            .build();
        ;
        ParticipantCertification certificationToExpect = certificationService.convertToDatabaseModel(participant.getParticipantCertification());


        System.out.println("\n ********************* Test method participantCertificationAndEvaluationFacility() - Test1  ********************* ");
        LOGGER.info("Tests inserting a Certification without an Evaluation Facility ... ");
        // Participant, Certification and EV are not on the DB AND the EV is not defined
        Participant1 insertedPart = participantUtil.addParticipant(participant);
        ParticipantCertification insertedCertification = certificationService.findCertificationByIDSID(insertedPart.getParticipantCertification().getCertificationIdsID());

        LOGGER.info("... Assert that the inserted Certification has a Database ID ... ");
        // If the certification was not inserted in the DB, the ID would be null
        assertNotNull(insertedCertification.getParticipantCertificationID());
        LOGGER.info("... Test passed ... ");

        LOGGER.info("... Assert that the inserted Certification is equal to the expected certification ... ");
        System.out.println("insertedCertification " + insertedCertification.toString());
        System.out.println("certificationToExpect " + certificationToExpect.toString());
        assertTrue(insertedCertification.convertToCertificationDto().equals(certificationToExpect.convertToCertificationDto()));
        LOGGER.info("... Test passed ... ");

        LOGGER.info("... Assert that the inserted Certification does not have an Evaluation Facility ... ");
        assertNull(insertedCertification.getEvaluationFacility());
        LOGGER.info("... Test passed. ");


        System.out.println("\n ********************* Test method addParticipant() - Test2  ********************* ");
        LOGGER.info("Tests inserting a Certification that is already on the Database (without an Evaluation Facility)... ");
        // Participant AND Certification are already on the DB
        LOGGER.info("... Assert that it throws an error because the Participnat already exists ... ");
        Participant finalParticipant = participant;
        Exception exception = assertThrows(ResourceAlreadyExistsException.class, () ->
            participantUtil.addParticipant(finalParticipant));
        LOGGER.info("... Test passed ... ");

        LOGGER.info("... Assert that the Participant is already on the DB... ");
        // If the certification was not inserted in the DB, the ID would be null
        assertEquals(exception.getMessage(), "Participant with ID 'https://participant/id/test/connector/fraunhofer/01' already exists");
        LOGGER.info("... Test passed. ");


        System.out.println("\n ********************* Test method participantCertificationAndEvaluationFacility() - Test3  ********************* ");
        LOGGER.info("Tests inserting a Certification with an Evaluation Facility when they are not on the DB... ");
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer/02"))
            ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
            ._legalName_("Fraunhofer-Institut 2")
            ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
            ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
            ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
            ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/02"))
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
                ._title_(new TypedLiteral("Participant Certification 3"))
                ._description_(new TypedLiteral("Participant with Certification Level " + CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/02")).build())
                .build())
            ._legalForm_("e.V.")
            .build();
        certificationToExpect = new inesctec.inestec.paris.builder.ParticipantCertificationBuilder()
            .ids_id("https://cert/id/02")
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .title("Participant Certification 3")
            .description("Participant with Certification Level " + CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/02")))
            .build();

        insertedPart = participantUtil.addParticipant(participant);
        insertedCertification = certificationService.findCertificationByIDSID(insertedPart.getParticipantCertification().getCertificationIdsID());

        LOGGER.info("... Assert that the inserted Certification and Evaluation Facility have a Database ID ... ");
        // If the certification was not inserted in the DB, the ID would be null
        assertNotNull(insertedCertification.getParticipantCertificationID());
        assertNotNull(insertedCertification.getEvaluationFacility().getEvaluationFacilityID());
        LOGGER.info("... Test passed ... ");

        LOGGER.info("... Assert that the inserted Certification and EV is equal to the expected certification and EV ... ");
        assertTrue(insertedCertification.getEvaluationFacility().getEvaluationFacilityID().equals(certificationToExpect.getEvaluationFacility().getEvaluationFacilityID()));
        assertTrue(Objects.equals(insertedCertification.getCertificationIdsID(), certificationToExpect.getCertificationIdsID()));
        assertEquals(insertedCertification.getMembershipEnd(), certificationToExpect.getMembershipEnd());
        assertTrue(Objects.equals(insertedCertification.getMembershipEnd(), certificationToExpect.getMembershipEnd()));
        assertTrue(Objects.equals(insertedCertification.getLastValidDate(), certificationToExpect.getLastValidDate()));
        assertTrue(Objects.equals(insertedCertification.getDescription(), certificationToExpect.getDescription()));
        assertTrue(Objects.equals(insertedCertification.getTitle(), certificationToExpect.getTitle()));
        LOGGER.info("... Test passed ... ");

        LOGGER.info("... Assert that the inserted Certification has an Evaluation Facility ... ");
        assertNotNull(insertedCertification.getEvaluationFacility());
        LOGGER.info("... Test passed. ");


        //  Create a Certification when the EV is already on the DB. Check if the Certification was inserted on the EV table

        System.out.println("\n ********************* Test method participantCertificationAndEvaluationFacility() - Test4  ********************* ");
        LOGGER.info("Tests inserting a Certification with an Evaluation Facility when they are not on the DB... ");
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer/03"))
            ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
            ._legalName_("Fraunhofer-Institut 3")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/03"))
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
                ._title_(new TypedLiteral("Participant Certification 3"))
                ._description_(new TypedLiteral("Participant with Certification Level " + CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/02")).build())
                .build())
            ._legalForm_("e.V.")
            .build();

        int evaluationFacilitySize_BeforeInsert = evaluationFacilityService.findByEvaluationFacilityID(URI.create("https://ev/02")).get().getCertifications().size();

        insertedPart = participantUtil.addParticipant(participant);
        insertedCertification = certificationService.findCertificationByIDSID(insertedPart.getParticipantCertification().getCertificationIdsID());

        LOGGER.info("... Assert that the inserted Certification and Evaluation Facility have a Database ID ... ");
        // If the certification was not inserted in the DB, the ID would be null
        assertNotNull(insertedCertification.getParticipantCertificationID());
        assertNotNull(insertedCertification.getEvaluationFacility().getEvaluationFacilityID());
        LOGGER.info("... Test passed ... ");

        LOGGER.info("... Assert that the Evaluation Facility now has the inserted Certification  ... ");
        Optional<EvaluationFacility> evaluationFacility = evaluationFacilityService.findByEvaluationFacilityID(insertedPart.getParticipantCertification().getEvaluationFacility().getEvaluationFacilityID());
        assertTrue(evaluationFacility.isPresent());
        assertTrue(evaluationFacility.get().getCertifications().contains(insertedCertification));
        // Checks if the Size of the Set of certifications was updated
        assertEquals(evaluationFacilitySize_BeforeInsert + 1, evaluationFacility.get().getCertifications().size());
        LOGGER.info("... Test passed ... ");


        // Test when the Certification is null
        System.out.println("\n ********************* Test method participantCertificationAndEvaluationFacility() - Test5  ********************* ");
        LOGGER.info("Tests inserting a Participant when the Certification is null... ");
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer/04"))
            ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
            ._legalName_("Fraunhofer-Institut 4")
            ._participantCertification_(null)
            ._legalForm_("e.V.")
            .build();

        Participant finalParticipant1 = participant;
        exception = assertThrows(Exception.class, () ->
            participantUtil.addParticipant(finalParticipant1));
        assertEquals(exception.getMessage().toString(), "Participant has invalid Certification value: 'null'");
        exception = assertThrows(ResourceNotFoundException.class, () ->
            certificationService.findCertificationByIDSID((URI.create("https://test"))));
        assertEquals(exception.getMessage(), "Certification not found");

    }

    /**
     * Tests inserting a Participant:
     * - without Refinement
     * - with a NONE Refinement
     * - with an AND Refinement
     * - with an XONE Refinement
     * -  with an OR Refinement
     */
    @Test
    @Sql(scripts = "/remove_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void participantRefinement() throws Exception {
        // Prepare Variables
        de.fraunhofer.iais.eis.Constraint infomodel_constraint1 = new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        de.fraunhofer.iais.eis.Constraint infomodel_constraint2 = new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint2"))
            ._leftOperand_(LeftOperand.ENDPOINT)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("http://endpoint/01")).build();

        List<Constraint> infomodel_constraints = new ArrayList<>();
        infomodel_constraints.add(infomodel_constraint1);
        infomodel_constraints.add(infomodel_constraint2);

        de.fraunhofer.iais.eis.LogicalConstraint logicalConstraint = new LogicalConstraintBuilder(URI.create("https://logical/contraint/id/01"))
            ._and_(infomodel_constraints).build();

        Participant participant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 1")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            ._participantRefinement_(infomodel_constraint1)
            .build();
        Participant1 producer = testUtil.addProducer();
        LogicalConstraint constraintToExpect = logicalConstraintService.convertToDatabaseModel(participant.getParticipantRefinement(), producer);


        System.out.println("\n ********************* Test1  ********************* ");
        LOGGER.info("Tests inserting a Refinement with an NONE: ");
        Participant1 insertedPart = participantUtil.addParticipant(participant);

        // Get the inserted Refinement
        LogicalConstraint inserted_LogicalConst = logicalConstraintService.findLogicalConstraintByIds_ID(insertedPart.getParticipantRefinement().getIdsID());
        List<inesctec.inestec.paris.model.Constraint> inserted_Constraint = insertedPart.getParticipantRefinement().getConstraints().stream()
            .map(v -> constraintService.findConstraintByConstraintIdsID(v.getConstraintIdsID()))
            .toList();


        LOGGER.info("... Assert that the inserted Refinement has a Database ID ... ");
        // If the certification was not inserted in the DB, the ID would be null
        assertNotNull(inserted_LogicalConst.getId_logical_constraint());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Refinement is equal to the expected Refinement ... ");
        assertEquals(inserted_LogicalConst.toJson(), constraintToExpect.toJson());
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Logical Constraint contains the inserted Constraint ... ");
        assertTrue(inserted_LogicalConst.getConstraints().containsAll(inserted_Constraint));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Constraint contains the inserted Logical Constraint ... ");
        LogicalConstraint finalInserted_LogicalConst = inserted_LogicalConst;
        inserted_Constraint.forEach(v -> {
            /*System.out.println("LogicalConstraint -> Ids_ID " +  Objects.equals(v.getLogicalConstraint().getIdsID(), inserted_LogicalConst.getIdsID())
                            + "\nLogicalConstraint -> Constraints " +   Objects.equals(v.getLogicalConstraint().getConstraints(), inserted_LogicalConst.getConstraints())
                            + "\nLogicalConstraint -> Operator " +   (v.getLogicalConstraint().getLogicalOperator() == inserted_LogicalConst.getLogicalOperator())
           );*/
            assertTrue(Objects.equals(v.getLogicalConstraint().getIdsID(), finalInserted_LogicalConst.getIdsID())
                && (v.getLogicalConstraint().getLogicalOperator() == finalInserted_LogicalConst.getLogicalOperator()));
        });
        LOGGER.info("... Test passed . ");

        clearDatabase();

        System.out.println("\n ********************* Test2  ********************* ");
        LOGGER.info("Tests inserting a Refinement with an AND: ");
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 2")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            ._participantRefinement_(logicalConstraint)
            .build();
        insertedPart = participantUtil.addParticipant(participant);
        // Get the inserted Refinement
        inserted_LogicalConst = logicalConstraintService.findLogicalConstraintByIds_ID(insertedPart.getParticipantRefinement().getIdsID());
        inserted_Constraint = insertedPart.getParticipantRefinement().getConstraints().stream()
            .map(v -> constraintService.findConstraintByConstraintIdsID(v.getConstraintIdsID()))
            .toList();
        constraintToExpect = logicalConstraintService.convertToDatabaseModel(participant.getParticipantRefinement(), producer);

        LOGGER.info("... Assert that the inserted Refinement has a Database ID ... ");
        // If the certification was not inserted in the DB, the ID would be null
        assertNotNull(inserted_LogicalConst.getId_logical_constraint());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Refinement is equal to the expected Refinement ... ");
        assertEquals(inserted_LogicalConst.toJson(), constraintToExpect.toJson());
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Logical Constraint contains the inserted Constraint ... ");
        System.err.println(inserted_LogicalConst.getConstraints().toString());
        System.out.println(infomodel_constraints.toString());
        assertTrue(inserted_LogicalConst.getConstraints().containsAll(inserted_Constraint));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Constraint contains the inserted Logical Constraint ... ");
        LogicalConstraint finalInserted_LogicalConst1 = inserted_LogicalConst;
        inserted_Constraint.forEach(v -> {
            /*System.out.println("LogicalConstraint -> Ids_ID " +  Objects.equals(v.getLogicalConstraint().getIdsID(), inserted_LogicalConst.getIdsID())
                            + "\nLogicalConstraint -> Constraints " +   Objects.equals(v.getLogicalConstraint().getConstraints(), inserted_LogicalConst.getConstraints())
                            + "\nLogicalConstraint -> Operator " +   (v.getLogicalConstraint().getLogicalOperator() == inserted_LogicalConst.getLogicalOperator())
           );*/
            assertTrue(Objects.equals(v.getLogicalConstraint().getIdsID(), finalInserted_LogicalConst1.getIdsID())
                && (v.getLogicalConstraint().getLogicalOperator() == finalInserted_LogicalConst1.getLogicalOperator()));
        });
        LOGGER.info("... Test passed . ");

        // Clear DB
        clearDatabase();

        System.out.println("\n ********************* Test3  ********************* ");
        LOGGER.info("Tests inserting a Refinement with an OR: ");
        logicalConstraint = new LogicalConstraintBuilder(URI.create("https://logical/contraint/id/02"))
            ._or_(infomodel_constraints).build();
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 3")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            ._participantRefinement_(logicalConstraint)
            .build();
        insertedPart = participantUtil.addParticipant(participant);
        // Get the inserted Refinement
        inserted_LogicalConst = logicalConstraintService.findLogicalConstraintByIds_ID(insertedPart.getParticipantRefinement().getIdsID());
        inserted_Constraint = insertedPart.getParticipantRefinement().getConstraints().stream()
            .map(v -> constraintService.findConstraintByConstraintIdsID(v.getConstraintIdsID()))
            .toList();
        constraintToExpect = logicalConstraintService.convertToDatabaseModel(participant.getParticipantRefinement(), producer);

        LOGGER.info("... Assert that the inserted Refinement has a Database ID ... ");
        // If the certification was not inserted in the DB, the ID would be null
        assertNotNull(inserted_LogicalConst.getId_logical_constraint());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Refinement is equal to the expected Refinement ... ");
        assertEquals(inserted_LogicalConst.toJson(), constraintToExpect.toJson());
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Logical Constraint contains the inserted Constraint ... ");
        assertTrue(inserted_LogicalConst.getConstraints().containsAll(inserted_Constraint));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Constraint contains the inserted Logical Constraint ... ");
        LogicalConstraint finalInserted_LogicalConst2 = inserted_LogicalConst;
        inserted_Constraint.forEach(v -> {
            /*System.out.println("LogicalConstraint -> Ids_ID " +  Objects.equals(v.getLogicalConstraint().getIdsID(), inserted_LogicalConst.getIdsID())
                            + "\nLogicalConstraint -> Constraints " +   Objects.equals(v.getLogicalConstraint().getConstraints(), inserted_LogicalConst.getConstraints())
                            + "\nLogicalConstraint -> Operator " +   (v.getLogicalConstraint().getLogicalOperator() == inserted_LogicalConst.getLogicalOperator())
           );*/
            assertTrue(Objects.equals(v.getLogicalConstraint().getIdsID(), finalInserted_LogicalConst2.getIdsID())
                && (v.getLogicalConstraint().getLogicalOperator() == finalInserted_LogicalConst2.getLogicalOperator()));
        });
        LOGGER.info("... Test passed . ");


        // Clear DB
        clearDatabase();

        System.out.println("\n ********************* Test4  ********************* ");
        LOGGER.info("Tests inserting a Refinement with a XONE: ");
        logicalConstraint = new LogicalConstraintBuilder(URI.create("https://logical/contraint/id/03"))
            ._xone_(infomodel_constraints).build();
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 4")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            ._participantRefinement_(logicalConstraint)
            .build();
        insertedPart = participantUtil.addParticipant(participant);
        // Get the inserted Refinement
        inserted_LogicalConst = logicalConstraintService.findLogicalConstraintByIds_ID(insertedPart.getParticipantRefinement().getIdsID());
        inserted_Constraint = insertedPart.getParticipantRefinement().getConstraints().stream()
            .map(v -> constraintService.findConstraintByConstraintIdsID(v.getConstraintIdsID()))
            .toList();
        constraintToExpect = logicalConstraintService.convertToDatabaseModel(participant.getParticipantRefinement(), producer);

        LOGGER.info("... Assert that the inserted Refinement has a Database ID ... ");
        // If the certification was not inserted in the DB, the ID would be null
        assertNotNull(inserted_LogicalConst.getId_logical_constraint());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Refinement is equal to the expected Refinement ... ");
        assertEquals(inserted_LogicalConst.toJson(), constraintToExpect.toJson());
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Logical Constraint contains the inserted Constraint ... ");
        assertTrue(inserted_LogicalConst.getConstraints().containsAll(inserted_Constraint));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Constraint contains the inserted Logical Constraint ... ");
        LogicalConstraint finalInserted_LogicalConst3 = inserted_LogicalConst;
        inserted_Constraint.forEach(v -> {
            assertTrue(Objects.equals(v.getLogicalConstraint().getIdsID(), finalInserted_LogicalConst3.getIdsID())
                && (v.getLogicalConstraint().getLogicalOperator() == finalInserted_LogicalConst3.getLogicalOperator()));
        });
        LOGGER.info("... Test passed . ");


        // Clear DB
        clearDatabase();

        System.out.println("\n ********************* Test5  ********************* ");
        LOGGER.info("Tests inserting a Refinement without Refinement: ");
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 5")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            ._participantRefinement_(null)
            .build();
        insertedPart = participantUtil.addParticipant(participant);

        LOGGER.info("... Assert that the inserted Refinement does not hava a Database ID ... ");
        assertNull(insertedPart.getParticipantRefinement());
        LOGGER.info("... Test passed.");
    }


    /**
     * Tests inserting a Participant:
     * - without a Person
     * - with a Person
     * - with a List of Person's
     * - with a Person that is already on the DB
     * - with a list of Persons that are already on the DB
     */
    @Test
    //@Sql(scripts = "/remove_data.sql",  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) //@Transactional
    void person() throws Exception {
        clearDatabase();
        // Prepare Variables
        List<String> phoneNumberList_Person = new ArrayList<>();
        phoneNumberList_Person.add("923452876");
        List<String> emailList_Person = new ArrayList<>();
        emailList_Person.add("john.doe@gmail.com");
        emailList_Person.add("john.doe@hotmail.com");

        // Prepare Person to be inserted (Infomodel)
        Set<Person> persons = new HashSet<>();
        Person person1 = new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(phoneNumberList_Person)
            ._emailAddress_(emailList_Person)
            ._homepage_("www.john-doe.com")
            .build();
        persons.add(person1);

        persons.add(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6642"))
            ._familyName_("Doe2")
            ._givenName_("John2")
            ._phoneNumber_(phoneNumberList_Person)
            ._emailAddress_(emailList_Person)
            ._homepage_("www.test.com").build());

        Participant participant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 1")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            .build();

        // Prepare Expected Person (DB)
        inesctec.inestec.paris.model.Person expectedPerson = testUtil.person();
        Set<inesctec.inestec.paris.model.Person> expectedPersons = testUtil.persons();

        System.out.println("\n ********************* Test1  ********************* ");
        LOGGER.info("Tests inserting a Participant without a Person: ");
        Participant1 insertedPart = participantUtil.addParticipant(participant);


        LOGGER.info("... Assert that the Participant was still inserted ... ");
        assertNotNull(insertedPart);
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that no Person was inserted ... ");
        assertNull(insertedPart.getMemberPerson());
        LOGGER.info("... Test passed.");

        System.err.println("Printing persons on DB:");
        personService.findAll().forEach(System.err::println);
        clearDatabase();

        System.out.println("\n ********************* Test2  ********************* ");
        LOGGER.info("Tests inserting a Person: ");
        participant.setMemberPerson(List.of(person1));

        System.out.println(participant.getParticipantCertification());
        System.out.println(certificationService.findAll().toString());
        insertedPart = participantUtil.addParticipant(participant);

        // Get the inserted Person
        Set<inesctec.inestec.paris.model.Person> inserted_Person = new HashSet<>();
        insertedPart.getMemberPerson().stream().forEach(p -> {
            personService.findPersonByPersonIdsID(p.getPersonIdsID().toString())
                .ifPresent(
                    (v) -> inserted_Person.add(v)
                );
        });

        LOGGER.info("... Assert that the inserted Person has a Database ID ... ");
        // If the Person was not inserted in the DB, the ID would be null
        inserted_Person.stream().forEach(p -> {
            assertNotNull(p.getPersonIdsID());
        });
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Person is equal to the expected Person ... ");
        assertTrue(CompareUtil.compareDBPersons(inserted_Person, Set.of(expectedPerson)));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Person contains the inserted Participant ... ");
        Participant1 finalInsertedPart = insertedPart;
        inserted_Person.stream().forEach(p -> {
            assertTrue(CompareUtil.compareDBParticipants(p.getParticipant().stream().toList(), List.of(finalInsertedPart)));
        });
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that there is 1 Person associated with this Participant ... ");
        assertEquals(personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().size(), 1);
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that there is 1 Person associated with this Participant ... ");
        assertEquals(personService.findPersonsByParticipant(insertedPart).size(), 1);
        LOGGER.info("... Test passed.");

        System.err.println("Printing persons on DB:");
        personService.findAll().forEach(System.err::println);

        // Clear DB and List
        clearDatabase();
        inserted_Person.clear();

        System.err.println("****************************");
        System.err.println(personService.size());
        System.err.println("****************************");

        System.out.println("\n ********************* Test3  ********************* ");
        LOGGER.info("Tests inserting a List of Persons: ");

        //participant.setMemberPerson(null);
        participant.setMemberPerson(persons.stream().toList());
        insertedPart = participantUtil.addParticipant(participant);

        // Get the inserted Person
        insertedPart.getMemberPerson().stream().forEach(p -> {
            personService.findPersonByPersonIdsID(p.getPersonIdsID().toString())
                .ifPresent(
                    (v) -> inserted_Person.add(v)
                );
        });

        LOGGER.info("... Assert that the inserted Person has a Database ID ... ");
        // If the Person was not inserted in the DB, the ID would be null
        inserted_Person.stream().forEach(p -> {
            assertNotNull(p.getPersonIdsID());
        });
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Person is equal to the expected Person ... ");
        inserted_Person.forEach(p -> System.err.println(p.toJson2()));
        /*System.out.println(expectedPerson.toJson2());*/
        assertTrue(CompareUtil.compareDBPersons(inserted_Person, expectedPersons));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Person contains the inserted Participant ... ");
        Participant1 finalInsertedPart1 = insertedPart;
        inserted_Person.stream().forEach(p -> {
            assertTrue(CompareUtil.compareDBParticipants(p.getParticipant().stream().toList(), List.of(finalInsertedPart1)));
        });
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Person only belongs to one Participant ... ");
        inserted_Person.stream().forEach(p -> {
            assertEquals(p.getParticipant().size(), 1, "Person should only be connected with one Participant");
        });
        LOGGER.info("... Test passed . ");

        System.err.println("Printing persons on DB:");
        personService.findAll().forEach(System.err::println);

        //inserted_Person.clear();


        System.out.println("\n ********************* Test4  ********************* ");
        LOGGER.info("Tests inserting a Participant with a Person that is already on the DB: ");


        Participant finalParticipant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 2")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            //._memberPerson_(person1)
            .build();
        /*Exception exception=assertThrows(ResourceAlreadyExistsException.class, () -> participantUtil.addParticipant(finalParticipant));
        assertEquals(exception.getMessage(), "Person with ID '"+participant.getMemberPerson().stream().findFirst().get().getId()+"' already exists");
*/
        // After creating a ManyToMany relation between Person and Participant, this should pass
        System.out.println("Before inserting, Person1 belongs to " + personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().size() + " Participants");
        assertEquals(personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().size(), 1);

        // Add Participant
        finalParticipant.setMemberPerson(List.of(person1));
        insertedPart = participantUtil.addParticipant(finalParticipant);
        System.out.println("After inserting, Person1 belongs to " + personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().size() + " Participants");

        System.out.println("After inserting, Person1 belongs to Participants " + personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant()
            .stream().map(part -> part.getParticipantIdsID()).toList());
        assertEquals(personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().size(), 2);

        insertedPart.getMemberPerson().stream().forEach(p -> {
            personService.findPersonByPersonIdsID(p.getPersonIdsID().toString())
                .ifPresent(
                    (v) -> inserted_Person.add(v)
                );
        });

        System.err.println("Printing persons on DB:");
        personService.findAll().forEach(System.err::println);

        System.out.println(personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().toString());
        // Check that the Participant was added into the Person entity
        assertEquals(personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().size(),
            2,
            "After inserting the last participant, person1 should belong to two participants");

        // Check that the Participant was added into the Person entity
        LOGGER.info("... Assert that the updated Person contains the inserted Participant ... ");
        assertThat(personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant()).contains(insertedPart);
        LOGGER.info("... Test passed. ");

        // Check that the Person was added into the Participant entity
        LOGGER.info("... Assert that the Participant contains the new Person ... ");
        assertTrue(CompareUtil.compareDBPersons(insertedPart.getMemberPerson(), Set.of(personService.findPersonByPersonIdsID(person1.getId().toString()).get())));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Person belongs to two Participant ... ");
        assertThat(personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().size()).isEqualTo(2);
        LOGGER.info("... Test passed . ");

        // Prepare database for the next test
        clearDatabase();
        // Participant also has 'persons' set
        participantUtil.addParticipant(participant);


        System.out.println("\n ********************* Test5  ********************* ");
        LOGGER.info("Tests inserting a Participant with a list of Persons that are already on the DB: ");


        Participant finalParticipant2 = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 3")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            ._memberPerson_(persons.stream().toList())
            .build();
        /*Exception exception=assertThrows(ResourceAlreadyExistsException.class, () -> participantUtil.addParticipant(finalParticipant2));
        assertEquals(exception.getMessage(), "Person with ID '"+participant.getMemberPerson().stream().findFirst().get().getId()+"' already exists");*/

        // After creating a ManyToMany relation between Person and Participant, this should pass
        for (Person person : persons) {
            System.out.println(personService.findPersonByPersonIdsID(person.getId().toString()));
            System.out.println("Before inserting, person (" + person.getId() + ") belongs to " + personService.findPersonByPersonIdsID(person.getId().toString()).get().getParticipant().size() + " Participants");
            assertEquals(personService.findPersonByPersonIdsID(person1.getId().toString()).get().getParticipant().size(), 1);
        }
        ;


        insertedPart = participantUtil.addParticipant(finalParticipant2);

        persons.forEach(person -> {
            System.out.println("After inserting, person (" + person.getId() + ") belongs to " + personService.findPersonByPersonIdsID(person.getId().toString()).get().getParticipant().size() + " Participants");
            assertEquals(personService.findPersonByPersonIdsID(person.getId().toString()).get().getParticipant().size(), 2);
        });

        System.err.println("Printing persons on DB:");
        personService.findAll().forEach(System.err::println);

        // Check that the Participant was added into the Person entity
        persons.forEach(person -> {
            assertEquals(personService.findPersonByPersonIdsID(person.getId().toString()).get().getParticipant().size(),
                2,
                "After inserting the last participant, the person should belong to two participants");
        });


        // Check that the Participant was added into the Person entity
        LOGGER.info("... Assert that the updated Person contains the inserted Participant ... ");
        for (Person person : persons) {
            assertThat(personService.findPersonByPersonIdsID(person.getId().toString()).get().getParticipant()).contains(insertedPart);
        }
        ;

        LOGGER.info("... Test passed. ");

        System.out.println(insertedPart.getMemberPerson().toString());
        //System.out.println(personService.findPersonByPersonIdsID(person1.getId().toString()).get().toString());
        // Check that the Person was added into the Participant entity
        LOGGER.info("... Assert that the Participant contains the new Person Set... ");
        Set<inesctec.inestec.paris.model.Person> personsOnDB = new HashSet<>();
        persons.forEach(person -> {
            personService.findPersonByPersonIdsID(person.getId().toString()).ifPresent(personsOnDB::add);
        });
        assertTrue(CompareUtil.compareDBPersons(insertedPart.getMemberPerson(), personsOnDB));

        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Person belongs to two Participant ... ");
        for (Person person : persons) {
            assertThat(personService.findPersonByPersonIdsID(person.getId().toString()).get().getParticipant().size()).isEqualTo(2);
        }

        LOGGER.info("... Test passed . ");
    }

    /**
     * Tests inserting a Participant:
     * - without a Business Identifier
     * - with a Business Identifier
     * - with a List of Business Identifier's
     * - with a Business Identifier that is already on the DB
     * - with a list of Business Identifiers that are already on the DB
     */
    @Test
    @Sql(scripts = "/remove_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void businessIdentifier() throws Exception {
        // Prepare Variables
        // Prepare Business Identifier to be inserted (Infomodel)
        List<BusinessIdentifier> businessIdentifiersToInsert = new ArrayList<>();
        de.fraunhofer.iais.eis.BusinessIdentifier businessIdentifierProducerToInsert = new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();
        de.fraunhofer.iais.eis.BusinessIdentifier businessIdentifierSubcontractedToInsert = new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/subcontracted-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();
        businessIdentifiersToInsert.add(businessIdentifierProducerToInsert);
        businessIdentifiersToInsert.add(businessIdentifierSubcontractedToInsert);

        Participant participant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 1")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            .build();

        // Prepare Expected Business Identifier (DB)
        inesctec.inestec.paris.model.BusinessIdentifier expectedBusinessIdentifier_producer = testUtil.businessIdentifier_Producer();
        Set<inesctec.inestec.paris.model.BusinessIdentifier> expectedBusinessIdentifier = new HashSet<>();
        expectedBusinessIdentifier.add(expectedBusinessIdentifier_producer);
        expectedBusinessIdentifier.add(testUtil.businessIdentifier_Subcontracted());


        System.out.println("\n ********************* Test1  ********************* ");
        LOGGER.info("Tests inserting a Participant without a Business Identifier: ");
        Participant1 insertedPart = participantUtil.addParticipant(participant);


        LOGGER.info("... Assert that the Participant was still inserted ... ");
        assertNotNull(insertedPart);
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that no Business Identifier was inserted ... ");
        assertTrue(insertedPart.getBusinessIdentifier() == null ||
            insertedPart.getBusinessIdentifier().isEmpty());
        LOGGER.info("... Test passed.");

        clearDatabase();

        System.out.println("\n ********************* Test2  ********************* ");
        LOGGER.info("Tests inserting a Business Identifier: ");
        participant.setBusinessIdentifier(List.of(businessIdentifierProducerToInsert));
        insertedPart = participantUtil.addParticipant(participant);

        // Get the inserted Business Identifier
        Set<inesctec.inestec.paris.model.BusinessIdentifier> inserted_BusinessIdentifier = new HashSet<>();
        insertedPart.getBusinessIdentifier().stream().forEach(p -> {
            businessIdentifierService.findBusinessIdentifierByBusinessIdentifierIdsID(p.getBusinessIdentifierIdsID().toString())
                .ifPresent(
                    (v) -> inserted_BusinessIdentifier.add(v)
                );
        });

        LOGGER.info("... Assert that the inserted Business Identifier has a Database ID ... ");
        // If the Business Identifier was not inserted in the DB, the ID would be null
        inserted_BusinessIdentifier.stream().forEach(p -> {
            assertNotNull(p.getBusinessIdentifierIdsID());
        });
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Business Identifier is equal to the expected Business Identifier ... ");
        /*inserted_BusinessIdentifier.forEach(p-> System.out.println(p.toJson()));
        expectedBusinessIdentifier.forEach(p-> System.err.println(p.toJson()));*/
        assertTrue(CompareUtil.compareSet(inserted_BusinessIdentifier, Set.of(expectedBusinessIdentifier_producer)));
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Business Identifier contains the inserted Participant ... ");
        Participant1 finalInsertedPart = insertedPart;
        inserted_BusinessIdentifier.stream().forEach(p -> {
            assertTrue(p.getParticipant().equals(finalInsertedPart));
        });
        LOGGER.info("... Test passed . ");

        // Clear DB and List
        clearDatabase();
        inserted_BusinessIdentifier.clear();

        System.err.println("****************************");
        System.err.println(businessIdentifierService.size());
        System.err.println("****************************");

        System.out.println("\n ********************* Test3  ********************* ");
        LOGGER.info("Tests inserting a List of Business Identifiers: ");

        System.out.println(businessIdentifiersToInsert.toString());

        participant.setBusinessIdentifier(businessIdentifiersToInsert.stream().toList());
        insertedPart = participantUtil.addParticipant(participant);

        // Get the inserted Business Identifier
        insertedPart.getBusinessIdentifier().stream().forEach(p -> {
            System.err.println("Sieze: " + p);
            businessIdentifierService.findBusinessIdentifierByBusinessIdentifierIdsID(p.getBusinessIdentifierIdsID().toString())
                .ifPresent(
                    (v) -> inserted_BusinessIdentifier.add(v)
                );
        });

        LOGGER.info("... Assert that the inserted Business Identifier has a Database ID ... ");
        // If the Business Identifier was not inserted in the DB, the ID would be null
        inserted_BusinessIdentifier.stream().forEach(p -> {
            assertNotNull(p.getBusinessIdentifierIdsID());
        });
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Business Identifier is equal to the expected Business Identifier ... ");
        inserted_BusinessIdentifier.forEach(p -> System.err.println(p.toJson()));
        expectedBusinessIdentifier.forEach(p -> System.out.println(p.toJson()));
        assertTrue(CompareUtil.compareDBBusinessIdentifiers(inserted_BusinessIdentifier, expectedBusinessIdentifier));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Business Identifier contains the inserted Business Identifier ... ");
        Participant1 finalInsertedPart1 = insertedPart;
        inserted_BusinessIdentifier.stream().forEach(p -> {
            assertTrue(p.getParticipant().equals(finalInsertedPart1));
        });
        LOGGER.info("... Test passed . ");


        System.out.println("\n ********************* Test4  ********************* ");
        LOGGER.info("Tests inserting a Participant with a Business Identifier that is already on the DB: ");

        Participant finalParticipant = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 2")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            ._businessIdentifier_(businessIdentifierProducerToInsert)
            .build();
        //participantUtil.addParticipant(finalParticipant);
        Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> participantUtil.addParticipant(finalParticipant));
        assertEquals(exception.getMessage(), "Business Identifier with ID '" + participant.getBusinessIdentifier().stream().findFirst().get().getId() + "' already exists");

        clearDatabase();
        participantUtil.addParticipant(participant);


        System.out.println("\n ********************* Test5  ********************* ");
        LOGGER.info("Tests inserting a Participant with a list of Business Identifiers that are already on the DB: ");


        Participant finalParticipant2 = new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._legalName_("paris-test Producer 3")
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                .build())
            ._legalForm_("e.V.")
            ._businessIdentifier_(businessIdentifierProducerToInsert)
            .build();
        exception = assertThrows(ResourceAlreadyExistsException.class, () -> participantUtil.addParticipant(finalParticipant2));
        assertEquals(exception.getMessage(), "Business Identifier with ID '" + participant.getBusinessIdentifier().stream().findFirst().get().getId() + "' already exists");

    }

    /*@Test  @Sql(scripts = "/remove_data.sql",  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void participantWithChildren() throws Exception {

        //clearDatabase();

        ParticipantConverter converter=new ParticipantConverter(participantService, certificationService,
                logicalConstraintService, personService);
        // Prepare Test
        Participant1 insertedParticipant;
        TestUtil testUtil=new TestUtil();

        Participant1 producer=testUtil.addProducer();
        Participant infomodel_producer=converter.convert(producer);

        LocalDateTime certificationDate = LocalDateTime.of(
                LocalDate.of(2022, 03, 23),
                LocalTime.of(12, 23,22));

        LocalDateTime certificationEndDate = LocalDateTime.of(
                LocalDate.of(2025, 03, 23),
                LocalTime.of(12, 23,22));

        Participant1 subcontracted= new inesctec.inestec.paris.builder.ParticipantBuilder(
                URI.create("https://stvgodigital.pt/ids/participant/id/subcontracted-01"),
                List.of("Subcontracted"),
                new inesctec.inestec.paris.builder.ParticipantCertificationBuilder()
                        .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        .lastValidDate(certificationDate)
                        .membershipEnd(certificationEndDate)
                        .ids_id("https://stvgodigital.pt/ids/participantCertification/id/01")
                        .title("Participant Certification 2")
                        .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        .build())
                .name( List.of("Subcontracted"))
                .corporateEmailAddress(List.of("subcontracted@inesctec.pt"))
                .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
                .registryCourt("Portugal")
                .legalForm("e.V.")
                .build();
        Participant infomodel_subcontracted=converter.convert(subcontracted);

        Participant1 expectedParticipant=new inesctec.inestec.paris.builder.ParticipantBuilder(
                URI.create("https://paris-test/ids/participant/id/producer-01"),
                List.of("paris test Producer"),
                new inesctec.inestec.paris.builder.ParticipantCertificationBuilder()
                        .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        .lastValidDate(certificationDate)
                        .membershipEnd(certificationEndDate)
                        .ids_id("https://paris-test/ids/participant/certification/id/02")
                        .evaluationFacility(new EvaluationFacility(URI.create("https://paris-test/ev/01")))
                        .build())
                .name( List.of("Producer"))
                .corporateEmailAddress(List.of("producer@inesctec.pt"))
                .corporateHomepage(URI.create("http://paris-test.pt"))
                //.memberParticipant(producer)
                .primarySite("Street X")
                .registryCourt("Portugal")
                .legalForm("e.V.")
                .build();

        Participant participantToInsert =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://paris-test/ids/participant/id/producer-01"))
                ._title_(Util.asList(new TypedLiteral("Producer")))
                ._legalName_("paris test Producer")
                ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
                ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
                ._corporateHomepage_(new URI("http://paris-test.pt"))
                ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://paris-test/ids/participant/certification/id/02"))
                        ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                        ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
                        ._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://paris-test/ev/01")).build())
                        .build())
                ._legalForm_("e.V.")
                ._memberParticipant_(infomodel_producer)
                ._jurisdiction_("Portugal")
                .build();
        // Initializes the DB
        participantUtil.addParticipant(infomodel_producer);

        System.out.println("\n ********************* Test1  ********************* ");
        LOGGER.info("Tests inserting a Participant that has a member Participant: ");
        // Test when MemberParticipant (Producer) does not have children
        participantUtil.addParticipant(participantToInsert);
        insertedParticipant=participantService.findParticipantByParticipantIDSID(participantToInsert.getId()).get();

        LOGGER.info("... Assert that the inserted Participant has a Database ID ... ");
        assertNotNull(insertedParticipant.getId_participant());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Participant is equal to the expected Participant ... ");
        assertTrue(CompareUtil.compareDBParticipants(insertedParticipant, expectedParticipant));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Participant has a memberParticipant ... ");
        assertTrue(insertedParticipant.getMemberParticipant().contains(producer));
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the Member Participant of the inserted Participant has the inserted Participant in its children ... ");
        Optional<Participant1> parent=participantService.findParticipantByParticipantIDSID(insertedParticipant.getMemberParticipant().get(0));
        assertTrue(parent.isPresent());
        assertTrue(parent.get().getMemberParticipant().contains(insertedParticipant.getParticipantIdsID()));
        LOGGER.info("... Test passed . ");

        assertTrue(CompareUtil.compareDBParticipants(insertedParticipant,expectedParticipant));

        System.out.println("\n ********************* Test1.2 Preparing the System  ********************* ");
        // Add children to the Producer
        subcontracted.setMemberParticipant(producer);
        infomodel_subcontracted.setMemberParticipant(List.of(infomodel_producer));
        participantUtil.addParticipant(infomodel_subcontracted);

        // Test when MemberParticipant Has children
        expectedParticipant=new inesctec.inestec.paris.builder.ParticipantBuilder(
                URI.create("https://paris-test/ids/participant/id/producer-02"),
                List.of("paris test Producer"),
                new inesctec.inestec.paris.builder.ParticipantCertificationBuilder()
                        .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        .lastValidDate(certificationDate)
                        .membershipEnd(certificationEndDate)
                        .ids_id("https://paris-test/ids/participant/certification/id/03")
                        .evaluationFacility(new EvaluationFacility(URI.create("https://paris-test/ev/01")))
                        .build())
                .name( List.of("Producer"))
                .corporateEmailAddress(List.of("producer@inesctec.pt"))
                .corporateHomepage(URI.create("http://paris-test.pt"))
                .memberParticipant(producer)
                .primarySite("Street X")
                .registryCourt("Portugal")
                .legalForm("e.V.")
                .build();
        participantToInsert=new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://paris-test/ids/participant/id/producer-02"))
                ._title_(Util.asList(new TypedLiteral("Producer")))
                ._legalName_("paris test Producer")
                ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
                ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
                ._corporateHomepage_(new URI("http://paris-test.pt"))
                ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://paris-test/ids/participant/certification/id/03"))
                        ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                        ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
                        ._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://paris-test/ev/01")).build())
                        .build())
                ._legalForm_("e.V.")
                ._memberParticipant_(infomodel_producer)
                ._jurisdiction_("Portugal")
                .build();

        System.out.println("\n ********************* Test2 ********************* ");
        participantUtil.addParticipant(participantToInsert);
        insertedParticipant=participantService.findParticipantByParticipantIDSID(participantToInsert.getId()).get();
        assertTrue(CompareUtil.compareDBParticipants(insertedParticipant,expectedParticipant));

        LOGGER.info("... Assert that the inserted Participant has a Database ID ... ");
        assertNotNull(insertedParticipant.getId_participant());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Participant is equal to the expected Participant ... ");
        assertTrue(CompareUtil.compareDBParticipants(insertedParticipant, expectedParticipant));
        LOGGER.info("... Test passed . ");


        LOGGER.info("... Assert that the inserted Participant has a memberParticipant ... ");
        assertTrue(insertedParticipant.getMemberParticipant().contains(producer));
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the Member Participant of the inserted Participant has the inserted Participant in its children ... ");
        parent=participantService.findParticipantByParticipantIDSID(insertedParticipant.getMemberParticipant().get(0));
        assertTrue(parent.isPresent());
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the Subcontracted still exists ... ");
        assertTrue(participantService.findParticipantByParticipantIDSID(subcontracted.getParticipantIdsID()).isPresent());
        LOGGER.info("... Test passed . ");

        // Test child with and without a Person
    }*/

    // I'm not removing Connectors
    @Test
    @Sql(scripts = "/remove_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void removeParticipant() throws Exception {

        Participant participantToRemove = testUtil.fraunhoferPartComplete();
        Optional<Participant1> participantToRemove_dbFormat;

        // Add a complete participant
        LOGGER.info("Adding a Participant to later on remove it.");
        participantUtil.addParticipant(participantToRemove);

        // Save the size of the Tables
        int participantSize, personSize, certificationSize, evSize, logicalConsSize, consSize, businessIdentifierSize;
        participantSize = participantService.size();
        personSize = personService.size();
        certificationSize = certificationService.size();
        evSize = evaluationFacilityService.size();
        logicalConsSize = logicalConstraintService.size();
        consSize = constraintService.size();
        businessIdentifierSize = businessIdentifierService.size();

        LOGGER.info("Checking if Participant was added.");
        participantToRemove_dbFormat = participantService.findParticipantByParticipantIDSID(participantToRemove.getId());
        assertThat(participantToRemove_dbFormat)
            .isPresent();

        //Remove that participant
        LOGGER.info("Removing the Participant.");
        participantUtil.removeParticipant(participantToRemove_dbFormat.get());

        // Assert that the current size is has minus one that the previous size (i.e., checks that, from each table, only one line was remove)
        assertThat(participantService.size()).isEqualTo(participantSize - 1);
        assertThat(certificationService.size()).isEqualTo(certificationSize - 1);
        assertThat(logicalConstraintService.size()).isEqualTo(logicalConsSize - 1);
        assertThat(constraintService.size()).isEqualTo(consSize - 2); // There were 2 constraints
        assertThat(businessIdentifierService.size()).isEqualTo(businessIdentifierSize - 1);

        //Assert That the Person Table is the same AND That there are no Persons associated with the Removed Participant
        assertThat(personService.size()).isEqualTo(personSize);
        assertThat(personService.findPersonsByParticipant(participantToRemove_dbFormat.get()).size()).isEqualTo(0);

        // Assert That No evaluation Facility was removed
        assertThat(evaluationFacilityService.size()).isEqualTo(evSize);


        // Assert that everything was removed
        assertThat(participantService.findParticipantByParticipantIDSID(participantToRemove.getId()))
            .isEmpty();
        Exception exception = assertThrows(ResourceNotFoundException.class,
            () -> certificationService.findCertificationByIDSID(participantToRemove.getParticipantCertification().getId()));
        assertThat(exception.getMessage()).isEqualTo("Certification not found");

        assertThat(evaluationFacilityService.findByEvaluationFacilityID(participantToRemove.getParticipantCertification().getEvaluationFacility().getId()))
            .isPresent();
        exception = assertThrows(ResourceNotFoundException.class,
            () -> logicalConstraintService.findLogicalConstraintByIds_ID(participantToRemove.getParticipantRefinement().getId()));
        assertThat(exception.getMessage()).isEqualTo("Logical Constraint not found");

        participantToRemove.getBusinessIdentifier().forEach(businessIdentifier -> {
            assertThat(businessIdentifierService
                .findBusinessIdentifierByBusinessIdentifierIdsID(businessIdentifier.getId().toString()))
                .isEmpty();
        });


    }

    @Test
    @Sql(scripts = "/remove_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void removeParticipantWithConnector() throws Exception {

        Participant participantToRemove = testUtil.fraunhoferPartComplete();
        inesctec.inestec.paris.model.Connector conn_producer = testUtil.connector_producer();
        Optional<Participant1> participantToRemove_dbFormat;

        // Create 2 participants that share the connector
        ParticipantFraunDto participantWithConnector = new ParticipantFraunDto(participantToRemove, Set.of(conn_producer));
        ParticipantFraunDto participant2WithConnector = new ParticipantFraunDto(testUtil.fraunhoferPartComplete_2(), Set.of(conn_producer));

        // Add a complete participant
        LOGGER.info("Adding a Participant to later on remove it.");
        participantUtil.addParticipant(participantWithConnector);
        participantUtil.addParticipant(participant2WithConnector);

        // Save the size of the Tables
        int participantSize, personSize, certificationSize, evSize, logicalConsSize, consSize, businessIdentifierSize, connectorSize;
        participantSize = participantService.size();
        personSize = personService.size();
        certificationSize = certificationService.size();
        logicalConsSize = logicalConstraintService.size();
        consSize = constraintService.size();
        businessIdentifierSize = businessIdentifierService.size();
        connectorSize = connectorService.size();

        LOGGER.info("Checking if Participant was added.");
        participantToRemove_dbFormat = participantService.findParticipantByParticipantIDSID(participantToRemove.getId());
        assertThat(participantToRemove_dbFormat)
            .isPresent();

        LOGGER.info("Checking if the Connector added has two Participants associated.");
        assertThat(connectorService.findConnectorByConnectorIdsID(conn_producer.getConnectorID().toString()).isPresent());
        assertThat(connectorService.findConnectorByConnectorIdsID(conn_producer.getConnectorID().toString()).get().getParticipantSet().size()).isEqualTo(2);


        //Remove that participant
        LOGGER.info("Removing the Participant.");
        participantUtil.removeParticipant(participantToRemove_dbFormat.get());

        // Assert that the current size is has minus one that the previous size (i.e., checks that, from each table, only one line was remove)
        assertThat(participantService.size()).isEqualTo(participantSize - 1);
        assertThat(certificationService.size()).isEqualTo(certificationSize - 1);
        assertThat(logicalConstraintService.size()).isEqualTo(logicalConsSize - 1);
        assertThat(constraintService.size()).isEqualTo(consSize - 2); // There were 2 constraints
        assertThat(businessIdentifierService.size()).isEqualTo(businessIdentifierSize - 1);

        //Assert That the Person Table is the same AND That there are no Persons associated with the Removed Participant
        assertThat(personService.size()).isEqualTo(personSize);
        assertThat(personService.findPersonsByParticipant(participantToRemove_dbFormat.get()).size()).isEqualTo(0);

        //Assert That the Connector Table is the same AND That there are no Connectors associated with the Removed Participant
        assertThat(connectorService.size()).isEqualTo(connectorSize);
        assertThat(connectorService.findConnectorsFromParticipant(participantToRemove_dbFormat.get()).size()).isEqualTo(0);

        LOGGER.info("Checking if the Connector added has, now, one Participant associated.");
        assertThat(connectorService.findConnectorByConnectorIdsID(conn_producer.getConnectorID().toString()).isPresent());
        assertThat(connectorService.findConnectorByConnectorIdsID(conn_producer.getConnectorID().toString()).get().getParticipantSet().size()).isEqualTo(1);

        // Assert that everything was removed
        assertThat(participantService.findParticipantByParticipantIDSID(participantToRemove.getId()))
            .isEmpty();
        Exception exception = assertThrows(ResourceNotFoundException.class,
            () -> certificationService.findCertificationByIDSID(participantToRemove.getParticipantCertification().getId()));
        assertThat(exception.getMessage()).isEqualTo("Certification not found");

        assertThat(evaluationFacilityService.findByEvaluationFacilityID(participantToRemove.getParticipantCertification().getEvaluationFacility().getId()))
            .isPresent();
        exception = assertThrows(ResourceNotFoundException.class,
            () -> logicalConstraintService.findLogicalConstraintByIds_ID(participantToRemove.getParticipantRefinement().getId()));
        assertThat(exception.getMessage()).isEqualTo("Logical Constraint not found");

        participantToRemove.getBusinessIdentifier().forEach(businessIdentifier -> {
            assertThat(businessIdentifierService
                .findBusinessIdentifierByBusinessIdentifierIdsID(businessIdentifier.getId().toString()))
                .isEmpty();
        });


    }

    /**
     * Tests inserting a Participant:
     * - first with a version 1 and IDS_ID
     * - then add another participant with the same IDS_ID and version 2
     */
    @Test
    @Sql(scripts = "/remove_data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void version() throws Exception {
        // Prepare Variables
        de.fraunhofer.iais.eis.Constraint constraint = new ConstraintBuilder()
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        de.fraunhofer.iais.eis.EvaluationFacility ev = new EvaluationFacilityBuilder().build();
        Participant participant;
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer"))
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
                ._evaluationFacility_(ev)
                ._lastValidDate_(
                    DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0)).build())
            ._memberPerson_(Util.asList(new de.fraunhofer.iais.eis.PersonBuilder(URI.create("https://person/01"))
                ._familyName_("Mueller")
                ._givenName_("Peter")
                ._emailAddress_(Util.asList("mueller.peter@emailcontact.de"))
                ._homepage_("https://MemberPerson.de/mueller_peter")
                ._phoneNumber_(Util.asList("017894561237"))
                .build()))
            ._businessIdentifier_(new BusinessIdentifierBuilder()
                ._identifierSystem_("CAE")
                ._identifierNumber_("number")
                .build())
            ._version_("1.0")
            ._legalForm_("e.V.")
            ._participantRefinement_(constraint)
            .build();


        System.out.println("\n ********************* Test1  ********************* ");
        LOGGER.info("Tests inserting a Participant: ");
        Participant1 insertedPart = participantUtil.addParticipant(participant);

        // Get the inserted Participant
        Optional<Participant1> partOnDB = participantService.findParticipantByParticipantIDSID(participant.getId());

        LOGGER.info("... Assert that the inserted Participant is present ... ");
        assertThat(partOnDB).isPresent();
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Participant has a Database ID ... ");
        // If the participant was not inserted in the DB, the ID would be null
        assertNotNull(partOnDB.get().getId_participant());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Participant has the correct version ... ");
        assertThat(partOnDB.get().getVersion()).isEqualTo("1.0");
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Person has the correct participant associated ... ");
        assertThat(partOnDB.get().getMemberPerson().size()).isEqualTo(1);
        assertThat(partOnDB.get().getMemberPerson().stream().findFirst().get().getFamilyName()).isEqualTo("Mueller");
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Refinement (LogicalConstraint) has the correct version ... ");
        Optional<LogicalConstraint> updatedLogicalCons_Op = logicalConstraintService.findLogicalConstraintBy_IDS_ID(partOnDB.get().getParticipantRefinement().getIdsID());
        assertThat(updatedLogicalCons_Op).isPresent();
        assertThat(partOnDB.get().getParticipantRefinement().getVersion()).isEqualTo("1.0");
        assertThat(constraintService.findConstraintByConstraintIdsID(updatedLogicalCons_Op.get().getConstraints().get(0).getConstraintIdsID()).getVersion()).isEqualTo("1.0");
        LOGGER.info("... Test passed . ");


        System.out.println("\n ********************* Test2  ********************* ");
        LOGGER.info("Tests inserting an updated Participant by changing the version: ");
        Participant participant2 = participant;
        participant2.setVatID("Updated VAT");
        participant2.setVersion("2.0");
        insertedPart = participantUtil.addParticipant(participant2);

        // Get the inserted Participant
        partOnDB = participantService.findParticipantByParticipantIDSID(participant2.getId());

        LOGGER.info("... Assert that the inserted Participant is present ... ");
        assertThat(partOnDB).isPresent();
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Participant has a Database ID ... ");
        // If the participant was not inserted in the DB, the ID would be null
        assertNotNull(partOnDB.get().getId_participant());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Participant has the correct version ... ");
        assertThat(partOnDB.get().getVersion()).isEqualTo("2.0");
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Refinement (LogicalConstraint) has the correct version ... ");
        updatedLogicalCons_Op = logicalConstraintService.findLogicalConstraintBy_IDS_ID(partOnDB.get().getParticipantRefinement().getIdsID());
        assertThat(updatedLogicalCons_Op).isPresent();
        assertThat(partOnDB.get().getParticipantRefinement().getVersion()).isEqualTo("2.0");
        assertThat(constraintService.findConstraintByConstraintIdsID(updatedLogicalCons_Op.get().getConstraints().get(0).getConstraintIdsID()).getVersion()).isEqualTo("2.0");
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Person has the correct participant associated ... ");
        assertThat(partOnDB.get().getMemberPerson().size()).isEqualTo(1);
        assertThat(partOnDB.get().getMemberPerson().stream().findFirst().get().getFamilyName()).isEqualTo("Mueller");
        LOGGER.info("... Test passed . ");

        System.out.println("\n ********************* Test3  ********************* ");
        LOGGER.info("Tests inserting an updated Participant by changing the version and all the values from the other tables: ");
        Participant participant3 = participant2;
        participant3.setMemberPerson(List.of(new de.fraunhofer.iais.eis.PersonBuilder()
            ._familyName_("Test")
            ._givenName_("Update")
            ._emailAddress_(Util.asList("test@emailcontact.de"))
            ._homepage_("https://MemberPerson.de/test")
            ._phoneNumber_(Util.asList("09876554"))
            .build()));
        participant3.setVersion("3.0");
        participant3.setParticipantCertification(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._evaluationFacility_(new EvaluationFacilityBuilder().build())
            ._lastValidDate_(
                DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0)).build());
        participant3.setBusinessIdentifier(List.of(new BusinessIdentifierBuilder()
            ._identifierSystem_("CAE 2")
            ._identifierNumber_("number")
            .build()));
        participant3.setParticipantRefinement(testUtil.fraunhoferPartWithOnlyOneConstraint().getParticipantRefinement());
        insertedPart = participantUtil.addParticipant(participant3);

        // Get the inserted Participant
        partOnDB = participantService.findParticipantByParticipantIDSID(participant3.getId());

        LOGGER.info("... Assert that the inserted Participant is present ... ");
        assertThat(partOnDB).isPresent();
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Participant has a Database ID ... ");
        // If the participant was not inserted in the DB, the ID would be null
        assertNotNull(partOnDB.get().getId_participant());
        LOGGER.info("... Test passed.");

        LOGGER.info("... Assert that the inserted Participant has the correct version ... ");
        assertThat(partOnDB.get().getVersion()).isEqualTo("3.0");
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Refinement (LogicalConstraint) has the correct version ... ");
        updatedLogicalCons_Op = logicalConstraintService.findLogicalConstraintBy_IDS_ID(partOnDB.get().getParticipantRefinement().getIdsID());
        assertThat(updatedLogicalCons_Op).isPresent();
        assertThat(partOnDB.get().getParticipantRefinement().getVersion()).isEqualTo("3.0"); // It is a new LogicalConstraint
        assertThat(constraintService.findConstraintByConstraintIdsID(updatedLogicalCons_Op.get().getConstraints().get(0).getConstraintIdsID()).getVersion()).isEqualTo("3.0");
        LOGGER.info("... Test passed . ");

        LOGGER.info("... Assert that the inserted Person has the correct participant associated ... ");
        assertThat(partOnDB.get().getMemberPerson().size()).isEqualTo(1);
        assertThat(partOnDB.get().getMemberPerson().stream().findFirst().get().getFamilyName()).isEqualTo("Test");
        assertThat(personService.findPersonByPersonIdsID("https://person/01").get().getParticipant().contains(participant3)).isFalse();
        LOGGER.info("... Test passed . ");

    }
}