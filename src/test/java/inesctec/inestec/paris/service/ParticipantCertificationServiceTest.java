package inesctec.inestec.paris.service;

import de.fraunhofer.iais.eis.CertificationLevel;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.SiteBuilder;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.builder.ParticipantCertificationBuilder;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.model.BusinessIdentifier;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.TestUtil;
import inesctec.inestec.paris.converter.XMLGregorianCalendarConverter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ParticipantCertificationServiceTest {

  @Autowired
  private ParticipantCertificationService certificationService;
  private ZonedDateTime certificationEndDate;
  private ZonedDateTime certificationDate;
  private XMLGregorianCalendarConverter xmlGregorianCalendarConverter;

  @BeforeEach
  void setUp() {
    xmlGregorianCalendarConverter =new XMLGregorianCalendarConverter();
    certificationDate = ZonedDateTime.of(2021, 06, 30, 12, 23, 22, 0, ZoneId.of("UTC"));
    certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

  }

  @Test
  void convertToDatabaseModel() throws DatatypeConfigurationException, URISyntaxException {
    TestUtil testUtil=new TestUtil();

    Participant fraunhoferPart=testUtil.fraunhoferPartWithOnlyOneConstraint();
    System.out.println(fraunhoferPart.getParticipantCertification());
    ParticipantCertification certification=certificationService.convertToDatabaseModel(fraunhoferPart.getParticipantCertification());

    ParticipantCertification expectedCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .ids_id("https://cert/id/01")
            .build();

    System.out.println(expectedCertification.toJson());
    System.err.println(certification.toJson());

    assertEquals(expectedCertification.toJson(), certification.toJson());

  }

  @Test
  void convertToDatabaseModel_AllValues() throws DatatypeConfigurationException, URISyntaxException {

    Participant fraunhoferPart=new de.fraunhofer.iais.eis.ParticipantBuilder()
            ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
            ._description_(Util.asList(new TypedLiteral("Participant Description")))
            ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
            ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
            ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/01"))
                    ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    ._title_(new TypedLiteral("Title"))
                    ._description_(new TypedLiteral("Description"))
                    ._membershipEnd_( DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2027, 6, 30, 0))
                    ._lastValidDate_(
                            DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0)).build())
            ._version_("3.0")
            ._legalForm_("e.V.")
            .build();;
    ParticipantCertification certification=certificationService.convertToDatabaseModel(fraunhoferPart.getParticipantCertification());

    ParticipantCertification expectedCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2027, 6, 30, 0))
            .description("Description")
            .title("Title")
            .ids_id("https://cert/id/01")
            .build();

    System.out.println(expectedCertification.toJson());
    System.err.println(certification.toJson());

    assertEquals(expectedCertification.toJson(), certification.toJson());

  }

  @Test
  void convertToDatabaseModel_Test_EveryValue() throws Exception {

    de.fraunhofer.iais.eis.ParticipantCertification infomodel;
    XMLGregorianCalendar lastValidDate_asXmlGregorianCalendar=xmlGregorianCalendarConverter.convertToXMLGregorianCalendar(certificationDate);
    XMLGregorianCalendar membershipEnd_asXmlGregorianCalendar=xmlGregorianCalendarConverter.convertToXMLGregorianCalendar(certificationEndDate);

    // Test with a certification without membershipEnd
    infomodel=new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
            // ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build();
    assertThat(certificationService.convertToDatabaseModel(infomodel))
            .isEqualTo(new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    //.membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build());




    // Test with a certification without certificationLevel -> Throws Error message, since this attribute is mandatory
    infomodel=new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            //._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
            ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build();
    de.fraunhofer.iais.eis.ParticipantCertification finalInfomodel = infomodel;
    Exception exception=assertThrows(InvalidValueException.class, () -> certificationService.convertToDatabaseModel(finalInfomodel));
    assertEquals(exception.getMessage(), "Certification Level has invalid value: 'null'");

    // Test with a certification without lastValidDate
    infomodel=new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            //._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
            ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build();
    assertThat(certificationService.convertToDatabaseModel(infomodel))
            .isEqualTo(new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    //.lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build());

    // Test with a certification without title
    infomodel=new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
            ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
            //._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build();
    assertThat(certificationService.convertToDatabaseModel(infomodel).toJson2())
            .isEqualTo(new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    //.title("Participant Certification 2")
                    .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build().toJson2());


    // Test with a certification without description
    infomodel=new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
            ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
            ._title_(new TypedLiteral("Participant Certification 2"))
            //._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build();
    assertThat(certificationService.convertToDatabaseModel(infomodel).toJson2())
            .isEqualTo(new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    //.description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build().toJson2());

    // Test with a certification without evaluationFacility
    infomodel=new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
            ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            //._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build();
    assertThat(certificationService.convertToDatabaseModel(infomodel))
            .isEqualTo(new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    //.evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build());

    // Test with a certification without ids_id
    infomodel=new de.fraunhofer.iais.eis.ParticipantCertificationBuilder()
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
            ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build();
    ParticipantCertification convertedCert = certificationService.convertToDatabaseModel(infomodel);
    System.out.println(convertedCert.toJson2());
    assertTrue(Objects.equals(convertedCert.getMembershipEnd(), certificationEndDate));
    assertTrue(Objects.equals(convertedCert.getDescription(), "Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK));
    assertTrue(Objects.equals(convertedCert.getTitle(),"Participant Certification 2"));
    assertTrue(Objects.equals(convertedCert.getEvaluationFacility(), new EvaluationFacility(URI.create("https://ev/01"))));
    assertTrue(Objects.equals(convertedCert.getCertificationLevel(), CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK));
    System.out.println(convertedCert.getLastValidDate());
    System.out.println(certificationDate);
    assertTrue(Objects.equals(convertedCert.getLastValidDate(), certificationDate));

    //

  }

  @Test
  void convertToInfomodel(){
    ParticipantCertification bdCertification;
    XMLGregorianCalendar lastValidDate_asXmlGregorianCalendar=xmlGregorianCalendarConverter.convertToXMLGregorianCalendar(certificationDate);
    XMLGregorianCalendar membershipEnd_asXmlGregorianCalendar=xmlGregorianCalendarConverter.convertToXMLGregorianCalendar(certificationEndDate);

    // Test with a certification without membershipEnd
    bdCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            //.membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
    assertThat(Objects.compare(
            certificationService.convertToInfomodel(bdCertification),
            new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
                    ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
                    // ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
                    ._title_(new TypedLiteral("Participant Certification 2"))
                    ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                    ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                    .build(),
            new CompareUtil.SortByIdsId()))
            .isEqualTo(0);



    // Test with a certification without certificationLevel
    bdCertification=new ParticipantCertificationBuilder()
            //.certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
    assertThat(Objects.compare(
            certificationService.convertToInfomodel(bdCertification),
            new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
                    //._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
                    ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
                    ._title_(new TypedLiteral("Participant Certification 2"))
                    ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                    ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                    .build(),
            new CompareUtil.SortByIdsId()))
            .isEqualTo(0);

    // Test with a certification without lastValidDate
    bdCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            //.lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
    assertThat(certificationService.convertToInfomodel(bdCertification).toRdf())
            .isEqualTo(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
                    ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    //._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
                    ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
                    ._title_(new TypedLiteral("Participant Certification 2"))
                    ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                    ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                    .build().toRdf());

    // Test with a certification without title
    bdCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            //.title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
    assertThat(certificationService.convertToInfomodel(bdCertification).toRdf())
            .isEqualTo(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
                    ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
                    ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
                    //._title_(new TypedLiteral("Participant Certification 2"))
                    ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                    ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                    .build().toRdf());

    // Test with a certification without description
    bdCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            //.description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
    assertThat(certificationService.convertToInfomodel(bdCertification).toRdf())
            .isEqualTo(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
                    ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
                    ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
                    ._title_(new TypedLiteral("Participant Certification 2"))
                    //._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                    ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                    .build().toRdf());

    // Test with a certification without evaluationFacility
    bdCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            //.evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
    assertThat(certificationService.convertToInfomodel(bdCertification).toRdf())
            .isEqualTo(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
                    ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    ._lastValidDate_(lastValidDate_asXmlGregorianCalendar)
                    ._membershipEnd_(membershipEnd_asXmlGregorianCalendar)
                    ._title_(new TypedLiteral("Participant Certification 2"))
                    ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                    //._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                    .build().toRdf());

    // Test with a certification without ids_id
    bdCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
    de.fraunhofer.iais.eis.ParticipantCertification convertedCert = certificationService.convertToInfomodel(bdCertification);
    assertTrue(Objects.equals(convertedCert.getMembershipEnd(), membershipEnd_asXmlGregorianCalendar));
    assertTrue(Objects.equals(convertedCert.getDescription().stream().map(v->v.getValue()).collect(Collectors.joining()), "Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK));
    assertTrue(Objects.equals(convertedCert.getTitle().stream().map(v->v.getValue()).collect(Collectors.joining()),"Participant Certification 2"));
    assertTrue(Objects.equals(convertedCert.getEvaluationFacility(), new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/01")).build()));
    assertTrue(Objects.equals(convertedCert.getCertificationLevel(), CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK));
    assertTrue(Objects.equals(convertedCert.getLastValidDate(), lastValidDate_asXmlGregorianCalendar));


  }

}