package inesctec.inestec.paris.service;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.builder.ParticipantCertificationBuilder;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.ParticipantUtil;
import inesctec.inestec.paris.util.TestUtil;
import inesctec.inestec.paris.converter.XMLGregorianCalendarConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ParticipantServiceTest {

  @Autowired
  private ParticipantUtil participantUtil;

  @Autowired
  private ParticipantService participantService;

  @Autowired private ParticipantCertificationService certificationService;
  private ZonedDateTime certificationEndDate;
  private ZonedDateTime certificationDate;
  private XMLGregorianCalendarConverter calendarConverter;

  @BeforeEach
  void setUp() {
    certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
    certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));


    calendarConverter=new XMLGregorianCalendarConverter();
  }

  @Test
  void convertToDatabaseModel() throws URISyntaxException {
    TestUtil testUtil=new TestUtil();
    Participant1 expectedParticipant=testUtil.addProducer();

    de.fraunhofer.iais.eis.Participant participant=new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
        ._title_(Util.asList(new TypedLiteral("Producer")))
        ._legalName_("Producer")
        ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
        ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
        ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
        ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
                ._title_(new TypedLiteral("Participant Certification 2"))
                ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                .build())
        ._legalForm_("e.V.")
        ._vatID_("123456789")
        ._businessIdentifier_(
                new BusinessIdentifierBuilder(URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
                        ._identifierNumber_("123456789")
                        ._identifierSystem_("CAE")
                        .build())
        ._jurisdiction_("Portugal")
        ._version_("1")
        .build();

    ParticipantCertification certification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .build();

    Participant1 convertedParticipant=participantService.convertToDatabaseModel(participant, certification);
    certification.setParticipant(convertedParticipant);
    /*System.out.println(expectedParticipant.toJson());
    System.err.println(convertedParticipant.toJson());*/

    assertTrue(CompareUtil.compareDBParticipants(convertedParticipant, expectedParticipant));
    /*assertEquals(convertedParticipant.getLegalName(), expectedParticipant.getLegalName());
    assertEquals(convertedParticipant.getPrimarySite(), expectedParticipant.getPrimarySite());
    assertEquals(convertedParticipant.getCorporateEmailAddress(), expectedParticipant.getCorporateEmailAddress());
    assertEquals(convertedParticipant.getCorporateHomepage(), expectedParticipant.getCorporateHomepage());
    assertEquals(convertedParticipant.getVatID(), expectedParticipant.getVatID());
    assertEquals(convertedParticipant.getRegistryCourt(), expectedParticipant.getRegistryCourt());
    assertEquals(convertedParticipant.getLegalForm(), expectedParticipant.getLegalForm());
    assertEquals(convertedParticipant.getBusinessIdentifier(), expectedParticipant.getBusinessIdentifier());
    assertEquals(convertedParticipant.getName(), expectedParticipant.getName());

    assertEquals(convertedParticipant.getParticipantCertification().getTitle(),expectedParticipant.getParticipantCertification().getTitle());
    assertEquals(convertedParticipant.getParticipantCertification().getCertificationLevel(),expectedParticipant.getParticipantCertification().getCertificationLevel());
    assertEquals(convertedParticipant.getParticipantCertification().getEvaluationFacility(),expectedParticipant.getParticipantCertification().getEvaluationFacility());
    assertEquals(convertedParticipant.getParticipantCertification().getDescription(),expectedParticipant.getParticipantCertification().getDescription());
    assertEquals(convertedParticipant.getParticipantCertification().getLastValidDate(),expectedParticipant.getParticipantCertification().getLastValidDate());
    assertEquals(convertedParticipant.getParticipantCertification().getMembershipEnd(),expectedParticipant.getParticipantCertification().getMembershipEnd());*/
  }

  @Test
  void convertToDatabaseModel_TestEveryValue() throws URISyntaxException {
    Participant fraun_Participant;
    Participant1 bd_Participant, convertedPart;
    ParticipantCertification dbCert=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
    de.fraunhofer.iais.eis.ParticipantCertification infModelCert=new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
            ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            ._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build();

    // Convert a null Participant
    convertedPart=participantService.convertToDatabaseModel(null, dbCert);
    assertNull(convertedPart);

    // Participant without legalName and Certification (Fails because in this system the Legal Name is mandatory)
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            //._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            //._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder(URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            .build();
    Participant finalFraun_Participant = fraun_Participant;
    Exception exception=assertThrows(InvalidValueException.class, () ->
            participantService.convertToDatabaseModel(finalFraun_Participant, dbCert));
    assertEquals(exception.getMessage(), "Legal Name has invalid value: '[]'");


    // Participant without primarySite
    bd_Participant=new inesctec.inestec.paris.builder.ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            //.primarySite("Street X")
            .vatID("123456789")
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .build();
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            //._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=participantService.convertToDatabaseModel(fraun_Participant, dbCert);
    System.out.println(convertedPart.getBusinessIdentifier());
    System.out.println(bd_Participant.getBusinessIdentifier());
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));


    // Participant without corporateEmailAddress
    bd_Participant=new inesctec.inestec.paris.builder.ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            //.corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .build();
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            //._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=participantService.convertToDatabaseModel(fraun_Participant, dbCert);
    System.out.println(convertedPart.getCorporateEmailAddress());
    System.out.println(bd_Participant.getCorporateEmailAddress());
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));


    // Participant without corporateHomepage
    bd_Participant=new inesctec.inestec.paris.builder.ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            //.corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .build();
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            //._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=participantService.convertToDatabaseModel(fraun_Participant, dbCert);
    System.out.println(convertedPart.getCorporateHomepage());
    System.out.println(bd_Participant.getCorporateHomepage());
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));

    // Participant without vatID
    bd_Participant=new inesctec.inestec.paris.builder.ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            //.vatID("123456789")
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .build();
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            //._vatID_("123456789")
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=participantService.convertToDatabaseModel(fraun_Participant, dbCert);
    System.out.println(convertedPart.getVatID());
    System.out.println(bd_Participant.getVatID());
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));

    // Participant without registryCourt/jurisdiction
    bd_Participant=new inesctec.inestec.paris.builder.ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            //.registryCourt("Portugal")
            .legalForm("e.V.")
            .build();
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            //._jurisdiction_("Portugal")
            .build();
    convertedPart=participantService.convertToDatabaseModel(fraun_Participant, dbCert);
    System.out.println(convertedPart.getRegistryCourt());
    System.out.println(bd_Participant.getRegistryCourt());
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));

    // legalForm cannot be null. So the test does not exist


    // Participant without name/title
    bd_Participant=new inesctec.inestec.paris.builder.ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            //.name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .build();
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            //._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=participantService.convertToDatabaseModel(fraun_Participant, dbCert);
    System.out.println(convertedPart.getName());
    System.out.println(bd_Participant.getName());
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));


    // Participant with all Basic arguments (including Description)
    bd_Participant=new inesctec.inestec.paris.builder.ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            //.description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .build();
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            //._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=participantService.convertToDatabaseModel(fraun_Participant, dbCert);
    System.out.println(convertedPart.getDescription());
    System.out.println(bd_Participant.getDescription());
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));
  }

  @Test
  void findParticipant1ByParticipantIdsID() throws Exception {
    TestUtil testUtil=new TestUtil();
    List<Participant1> participantsToRemove=new ArrayList<>();

    de.fraunhofer.iais.eis.Participant participant=new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
        ._title_(Util.asList(new TypedLiteral("Producer")))
        ._legalName_("Producer")
        ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
        ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
        ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
        ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
            ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            .build())
        ._legalForm_("e.V.")
        ._vatID_("123456789")
        ._businessIdentifier_(
            new BusinessIdentifierBuilder(URI.create("https://stvgodigital.pt/ids/businessIdentifier/id/producer-01"))
                ._identifierNumber_("123456789")
                ._identifierSystem_("CAE")
                .build())
        ._jurisdiction_("Portugal")
        ._version_("1.0")
        .build();


    // Add first version of Participant
    participantsToRemove.add(participantUtil.addParticipant(participant));

    participant.setMemberPerson(List.of(new PersonBuilder()._familyName_("FamilyName")
        ._givenName_("Given Name").build()));
    participant.setVersion("2.0");

    // Add Second version of Participant
    participantsToRemove.add(participantUtil.addParticipant(participant));

    participantService.findAll().forEach(System.err::println);

    assertThat(participantService.findParticipantByParticipantIDSID(participant.getId())).isPresent();
    assertThat(participantService.findParticipantByParticipantIDSID(participant.getId()).get().getVersion()).isEqualTo("2.0");

    // Add an value impossible to order by
    participant.setVatID("VAT");
    participant.setVersion("Test");

    // Add Third version of Participant
    participantsToRemove.add(participantUtil.addParticipant(participant));
    assertThat(participantService.findParticipantByParticipantIDSID(participant.getId()).get().getVersion()).isEqualTo("Test");

    System.err.println(participantService.findParticipantByParticipantIDSID(participant.getId()));

    // Remove inserted Participant
    participantsToRemove.forEach(p->participantUtil.removeParticipant(p));
  }
}