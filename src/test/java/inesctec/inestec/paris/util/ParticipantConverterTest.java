package inesctec.inestec.paris.util;

import com.google.common.collect.Sets;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.builder.LogicalConstraintBuilder;
import inesctec.inestec.paris.builder.ParticipantBuilder;
import inesctec.inestec.paris.builder.ParticipantCertificationBuilder;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.converter.XMLGregorianCalendarConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ParticipantConverterTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantConverterTest.class);

  @Autowired private ParticipantService participantService;
  @Autowired private ParticipantCertificationService certificationService;
  @Autowired private LogicalConstraintService constraintService;
  @Autowired private PersonService personService;
  @Autowired private BusinessIdentifierService businessIdentifierService;

  private ParticipantConverter converter;
  private Participant1 bd_Participant;
  private Participant fraun_Participant;
  private ZonedDateTime certificationEndDate;
  private ZonedDateTime certificationDate;
  private XMLGregorianCalendarConverter calendarConverter;

  @BeforeEach
  void setUp() throws URISyntaxException {
    calendarConverter=new XMLGregorianCalendarConverter();
    certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
    certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));


    converter=new ParticipantConverter(participantService, certificationService, constraintService, personService, businessIdentifierService);

    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"),
            new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build())
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .participantRefinement(
                    new LogicalConstraintBuilder(URI.create("https://part/ref/01"))
                            .constraints(new inesctec.inestec.paris.builder.ConstraintBuilder()
                                    .leftOperand(LeftOperand.SECURITY_LEVEL)
                                    .operator(String.valueOf(BinaryOperator.EQUALS))
                                    .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
                                    .unit("http://dbpedia.org/resource/Euro")
                                    .constraint_ids_ID("https://part/ref/01")
                                    .build())
                            .build()
            )
            .memberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
                            .familyName("Doe")
                            .givenName("John")
                            .phoneNumber(List.of("923452876"))
                            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                            .homepage("www.john-doe.com")
                            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                            .build()))
            .primarySite("Street X")
            .vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
            .registryCourt("Portugal")
            .legalForm("e.V.")
            //.hasRole(URI.create("http://www.stvgodigital.pt"))
            .build();

    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
                    ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                    ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
                    ._title_(new TypedLiteral("Participant Certification 2"))
                    ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                    ._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                    .build())
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder(URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            ._participantRefinement_(new ConstraintBuilder(URI.create("https://part/ref/01"))
                    ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                    ._operator_(BinaryOperator.EQUALS)
                    ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
                    ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
                    .build())
            ._memberPerson_(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
                    ._familyName_("Doe")
                    ._givenName_("John")
                    ._phoneNumber_(List.of("923452876"))
                    ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    ._homepage_("www.john-doe.com")
                    .build())
            .build();
  }

  @Test
  void convert() {
    Participant convertedPart;

    // Compare two complete Participants
    convertedPart=converter.convert(bd_Participant);
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart, fraun_Participant));

    // Convert a Participant without certification
    bd_Participant.setParticipantCertification(null);
    fraun_Participant.setParticipantCertification(null);
    assertTrue(CompareUtil.compareFraunhParticipants(converter.convert(bd_Participant),fraun_Participant));

    // Convert a Participant without Refinement
    bd_Participant.setParticipantRefinement(null);
    fraun_Participant.setParticipantRefinement(null);
    assertTrue(CompareUtil.compareFraunhParticipants(converter.convert(bd_Participant),fraun_Participant));

    // Convert a Participant with a Refinement with no Constraints. In this case, the converted Participant does not have Refinement
    bd_Participant.setParticipantRefinement(new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
            .build());
    assertTrue(CompareUtil.compareFraunhParticipants(converter.convert(bd_Participant),fraun_Participant));
  }

  @Test
  void convert_Person() {
    Participant convertedPart;

    // Convert a Participant without Person
    bd_Participant.setMemberPerson((Set<inesctec.inestec.paris.model.Person>) null);
    fraun_Participant.setMemberPerson(null);
    convertedPart=converter.convert(bd_Participant);
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));
    assertEquals(convertedPart.getParticipantRefinement(), fraun_Participant.getParticipantRefinement());

    // Person without familyName
    bd_Participant.setMemberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
            //.familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build()));
    fraun_Participant.setMemberPerson(List.of(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            //._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build()));
    assertTrue(CompareUtil.compareFraunhParticipants(converter.convert(bd_Participant),fraun_Participant));

    // Person without givenName
    bd_Participant.setMemberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            //.givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build()));
    fraun_Participant.setMemberPerson(List.of(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            //._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build()));
    assertTrue(CompareUtil.compareFraunhParticipants(converter.convert(bd_Participant),fraun_Participant));

    // Person without phoneNumber
    bd_Participant.setMemberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            //.phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build()));
    fraun_Participant.setMemberPerson(List.of(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            //._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build()));
    assertTrue(CompareUtil.compareFraunhParticipants(converter.convert(bd_Participant),fraun_Participant));

    // Person without email
    bd_Participant.setMemberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            //.email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build()));
    fraun_Participant.setMemberPerson(List.of(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            //._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build()));
    assertTrue(CompareUtil.compareFraunhParticipants(converter.convert(bd_Participant),fraun_Participant));

    // Person without homepage
    bd_Participant.setMemberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            //.homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build()));
    fraun_Participant.setMemberPerson(List.of(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            //._homepage_("www.john-doe.com")
            .build()));
    assertTrue(CompareUtil.compareFraunhParticipants(converter.convert(bd_Participant),fraun_Participant));

    // Person without ids_id
    bd_Participant.setMemberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            //.ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build()));
    fraun_Participant.setMemberPerson(List.of(new PersonBuilder()
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build()));
    convertedPart=converter.convert(bd_Participant);
    assertTrue(Objects.equals(convertedPart.getMemberPerson().get(0).getFamilyName(), "Doe"));
    assertTrue(Objects.equals(convertedPart.getMemberPerson().get(0).getGivenName(), "John"));
    assertTrue(Objects.equals(convertedPart.getMemberPerson().get(0).getPhoneNumber(), List.of("923452876")));
    assertTrue(Objects.equals(convertedPart.getMemberPerson().get(0).getEmailAddress(), List.of("john.doe@gmail.com", "john.doe@hotmail.com")));
    assertTrue(Objects.equals(convertedPart.getMemberPerson().get(0).getHomepage(), "www.john-doe.com"));
  }

  @Test
  void convert_Participant() throws URISyntaxException {
    Participant convertedPart;
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
    convertedPart=converter.convert(null);
    assertNull(convertedPart);

    // Participant without legalName and Certification
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01")
            /*,
            List.of("Producer"), dbCert*/
            )
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description", "ICT Service Provider"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .build();
    fraun_Participant =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            //._legalName_("Producer")
            ._description_(List.of(new TypedLiteral("Producer Description"), new TypedLiteral("ICT Service Provider")))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            //._participantCertification_(infModelCert)
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder()
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    System.err.println(fraun_Participant.toRdf());
    System.out.println(convertedPart.toRdf());
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));


    // Participant without primarySite
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            //.primarySite("Street X")
            .vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
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
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder()
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));


    // Participant without corporateEmailAddress
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            //.corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
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
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder(URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    System.out.println(convertedPart.getCorporateEmailAddress());
    System.out.println(fraun_Participant.getCorporateEmailAddress());
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));


    // Participant without corporateHomepage
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            //.corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
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
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder()
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    System.out.println(convertedPart.getCorporateHomepage());
    System.out.println(fraun_Participant.getCorporateHomepage());
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));

    // Participant without vatID
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            //.vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
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
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder(URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    System.out.println(convertedPart.getVatID());
    System.out.println(fraun_Participant.getVatID());
    System.out.println(convertedPart.getBusinessIdentifier());
    System.out.println(fraun_Participant.getBusinessIdentifier());
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));

    // Participant without registryCourt/jurisdiction
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
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
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder()
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            //._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    System.out.println(convertedPart.getJurisdiction());
    System.out.println(fraun_Participant.getJurisdiction());
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));

    // legalForm cannot be null. So the test does not exist

    // Participant without taxID/businessIdentifier
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            //.businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            //                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            //                    .identifierSystem("businessIdentifierSystem")
            //                    .identifierNumber("businessIdentifierNumber")
            //                    .build())
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
            ._vatID_("123456789")
            /*._businessIdentifier_(
                    new BusinessIdentifierBuilder()
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())*/
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    System.out.println(convertedPart.getBusinessIdentifier());
    System.out.println(fraun_Participant.getBusinessIdentifier());
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));


    // Participant without name/title
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            //.name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
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
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder()
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    System.out.println(convertedPart.getTitle());
    System.out.println(fraun_Participant.getTitle());
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));


    // Participant with all Basic arguments (including Description)
    bd_Participant=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), dbCert)
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            //.description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456789")
            .businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("CAE")
                    .identifierNumber("123456789")
                    .build())
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
            ._businessIdentifier_(
                    new BusinessIdentifierBuilder(URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
                            ._identifierNumber_("123456789")
                            ._identifierSystem_("CAE")
                            .build())
            ._jurisdiction_("Portugal")
            .build();
    convertedPart=converter.convert(bd_Participant);
    System.out.println(convertedPart.getDescription());
    System.out.println(fraun_Participant.getDescription());
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));
  }

  @Test
  void convert_ParticipantWithChildren() throws URISyntaxException {

    // Prepare Test
    Participant convertedPart;
    TestUtil testUtil=new TestUtil();

    Participant1 producer=testUtil.addProducer();
    Participant infomodel_producer=converter.convert(producer);

    Participant1 subcontracted=testUtil.addSubcontracted();
    Participant infomodel_subcontracted=converter.convert(subcontracted);


    // TODO
    // Test when MemberParticipant does not have children
    /*fraun_Participant.setMemberParticipant(List.of(infomodel_producer));
    convertedPart=converter.convert(bd_Participant);
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));

    // Add children to the Producer
    
    infomodel_producer.setMemberParticipant(List.of(infomodel_subcontracted));

    // Test when MemberParticipant Has children
    
    //bd_Participant.addChild(producer);
    fraun_Participant.setMemberParticipant(List.of(infomodel_producer));
    convertedPart=converter.convert(bd_Participant);
    assertTrue(CompareUtil.compareFraunhParticipants(convertedPart,fraun_Participant));*/

  }

  @Test
  void convertToDatabaseParticipant(){

    LOGGER.info("Start convertToDatabaseParticipant() test");
    Participant1 convertedPart=converter.convertToDatabaseParticipant(fraun_Participant);

    LOGGER.info("Participant converted");

    /*System.out.println(convertedPart.toJson());
    System.err.println(bd_Participant.toJson());*/

    System.out.println(convertedPart.getBusinessIdentifier().toString());
    System.err.println(bd_Participant.getBusinessIdentifier().toString());

    assertTrue(CompareUtil.compareDBParticipants(convertedPart, bd_Participant));
    assertEquals(convertedPart.getParticipantCertification(), bd_Participant.getParticipantCertification());
    assertEquals(convertedPart.getParticipantCertification().getEvaluationFacility(), bd_Participant.getParticipantCertification().getEvaluationFacility());
    assertEquals(convertedPart.getConnectorSet(), bd_Participant.getConnectorSet());
    assertEquals(convertedPart.getParticipantRefinement(), bd_Participant.getParticipantRefinement());
    assertTrue(Sets.difference(convertedPart.getMemberPerson(), bd_Participant.getMemberPerson()).isEmpty());

    assertEquals(convertedPart, bd_Participant);

    // Add Connectors to the Participants
    Connector connector=new Connector(URI.create("http://test"), "test-connector",URI.create("http://test"));
    convertedPart.setConnectorSet(Set.of(connector));
    assertNotEquals(convertedPart.getConnectorSet(), bd_Participant.getConnectorSet());
    bd_Participant.setConnectorSet(Set.of(connector));

    assertEquals(convertedPart.getConnectorSet(), bd_Participant.getConnectorSet());
    assertTrue(Sets.difference(convertedPart.getMemberPerson(), bd_Participant.getMemberPerson()).isEmpty());
  }

  @Test
  void convertToDatabaseParticipant_TestAllValues(){

    // Convert a Participant without certification
    LOGGER.info("Convert a Participant without certification ...");
    bd_Participant.setParticipantCertification(null);
    fraun_Participant.setParticipantCertification(null);

    Participant1 convertedPart=converter.convertToDatabaseParticipant(fraun_Participant);

    LOGGER.info(" ... Participant converted...");
    assertTrue(CompareUtil.compareDBParticipants(convertedPart, bd_Participant));
    assertEquals(convertedPart.getParticipantCertification(), bd_Participant.getParticipantCertification());
    assertEquals(convertedPart.getConnectorSet(), bd_Participant.getConnectorSet());
    assertEquals(convertedPart.getParticipantRefinement(), bd_Participant.getParticipantRefinement());
    assertTrue(Sets.difference(convertedPart.getMemberPerson(), bd_Participant.getMemberPerson()).isEmpty());

    assertEquals(convertedPart, bd_Participant);
    LOGGER.info(" ... Test passed.");


    // Convert a Participant with a certification that does not have a Evaluation Facility
    LOGGER.info("Convert a Participant with a certification that does not have a Evaluation Facility ...");
    bd_Participant.setParticipantCertification(new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            //.evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build());
    fraun_Participant.setParticipantCertification(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
            ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            //._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build());
    convertedPart=converter.convertToDatabaseParticipant(fraun_Participant);

    LOGGER.info(" ... Participant converted...");
    assertTrue(CompareUtil.compareDBParticipants(convertedPart, bd_Participant));
    assertEquals(convertedPart.getParticipantCertification(), bd_Participant.getParticipantCertification());
    assertEquals(convertedPart.getConnectorSet(), bd_Participant.getConnectorSet());
    assertEquals(convertedPart.getParticipantRefinement(), bd_Participant.getParticipantRefinement());
    assertTrue(Sets.difference(convertedPart.getMemberPerson(), bd_Participant.getMemberPerson()).isEmpty());

    assertEquals(convertedPart, bd_Participant);
    LOGGER.info(" ... Test passed.");
    /*System.out.println(convertedPart.toJson());
    System.err.println(bd_Participant.toJson());*/


    // Convert a Participant without a Person
    LOGGER.info("Convert a Participant without a Person ...");
    bd_Participant.setMemberPerson((Set<inesctec.inestec.paris.model.Person>) null);
    fraun_Participant.setMemberPerson(null);
    convertedPart=converter.convertToDatabaseParticipant(fraun_Participant);

    LOGGER.info(" ... Participant converted...");
    assertTrue(CompareUtil.compareDBParticipants(convertedPart, bd_Participant));
    LOGGER.info(" ... Test passed.");

    // Convert a Participant without Refinement
    LOGGER.info("Convert a Participant without Refinement ...");
    bd_Participant.setParticipantRefinement(null);
    fraun_Participant.setParticipantRefinement(null);
    convertedPart=converter.convertToDatabaseParticipant(fraun_Participant);

    LOGGER.info(" ... Participant converted...");
    assertTrue(CompareUtil.compareDBParticipants(convertedPart, bd_Participant));
    LOGGER.info(" ... Test passed.");


    // Add Connectors to the Participants
    Connector connector=new Connector(URI.create("http://test"), "test-connector",URI.create("http://test"));
    convertedPart.setConnectorSet(Set.of(connector));
    assertNotEquals(convertedPart.getConnectorSet(), bd_Participant.getConnectorSet());
    bd_Participant.setConnectorSet(Set.of(connector));

    assertEquals(convertedPart.getConnectorSet(), bd_Participant.getConnectorSet());
    assertTrue(CompareUtil.compareSet(convertedPart.getMemberPerson(), bd_Participant.getMemberPerson()));
  }


  @Test
  void convertToDatabaseParticipant_ParticipantWithChildren() throws URISyntaxException {

    // Prepare Test
    Participant1 convertedPart;
    TestUtil testUtil=new TestUtil();

    Participant1 producer=testUtil.addProducer();
    Participant infomodel_producer=converter.convert(producer);

    Participant1 subcontracted=testUtil.addSubcontracted();
    Participant infomodel_subcontracted=converter.convert(subcontracted);


    // Test when MemberParticipant does not have children
    fraun_Participant.setMemberParticipant(List.of(infomodel_producer));
    convertedPart=converter.convertToDatabaseParticipant(fraun_Participant);
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));

    // Add children to the Producer
    
    infomodel_producer.setMemberParticipant(List.of(infomodel_subcontracted));

    // Test when MemberParticipant Has children
    
    //bd_Participant.addChild(producer);
    fraun_Participant.setMemberParticipant(List.of(infomodel_producer));
    convertedPart=converter.convertToDatabaseParticipant(fraun_Participant);
    assertTrue(CompareUtil.compareDBParticipants(convertedPart,bd_Participant));

  }

  @Test
  void convertToDatabaseParticipant_WithFraunDTO(){

    TestUtil testUtil=new TestUtil();
    LOGGER.info("Start convertToDatabaseParticipant() test");

    bd_Participant.setConnectorSet(Set.of(testUtil.connector_producer()));
    ParticipantFraunDto dto_part=new ParticipantFraunDto(fraun_Participant, Set.of(testUtil.connector_producer()));
    Participant1 convertedPart=converter.convertToDatabaseParticipant(dto_part);

    LOGGER.info("Participant converted");

    /*System.out.println(convertedPart.toJson());
    System.err.println(bd_Participant.toJson());*/

    assertTrue(CompareUtil.compareDBParticipants(convertedPart, bd_Participant));
    assertEquals(convertedPart.getParticipantCertification(), bd_Participant.getParticipantCertification());
    assertEquals(convertedPart.getParticipantCertification().getEvaluationFacility(), bd_Participant.getParticipantCertification().getEvaluationFacility());
    assertEquals(convertedPart.getConnectorSet(), bd_Participant.getConnectorSet());
    assertEquals(convertedPart.getParticipantRefinement(), bd_Participant.getParticipantRefinement());
    assertTrue(Sets.difference(convertedPart.getMemberPerson(), bd_Participant.getMemberPerson()).isEmpty());

    assertEquals(convertedPart, bd_Participant);
  }

  @Test
  void convertToParticipantFraunDto() {
    TestUtil testUtil=new TestUtil();
    // Add the Connectors Set to the Database Participant and into the Infomodel Participant
    ParticipantFraunDto expectedParticipant = new ParticipantFraunDto(fraun_Participant, Set.of(testUtil.connector_producer()));
    bd_Participant.setConnectorSet(Set.of(testUtil.connector_producer()));

    ParticipantFraunDto convertedPart;

    // Compare two complete Participants
    convertedPart=converter.convertToParticipantFraunDto(bd_Participant);
    assertTrue(CompareUtil.compareFraunhDTOParticipants(convertedPart, expectedParticipant));

    // Convert a Participant without certification
    bd_Participant.setParticipantCertification(null);
    fraun_Participant.setParticipantCertification(null);
    assertTrue(CompareUtil.compareFraunhDTOParticipants(converter.convertToParticipantFraunDto(bd_Participant),expectedParticipant));

    // Convert a Participant without Refinement
    bd_Participant.setParticipantRefinement(null);
    fraun_Participant.setParticipantRefinement(null);
    assertTrue(CompareUtil.compareFraunhDTOParticipants(converter.convertToParticipantFraunDto(bd_Participant),expectedParticipant));

    // Convert a Participant with a Refinement with no Constraints. In this case, the converted Participant does not have Refinement
    bd_Participant.setParticipantRefinement(new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
            .build());
    assertTrue(CompareUtil.compareFraunhDTOParticipants(converter.convertToParticipantFraunDto(bd_Participant),expectedParticipant));
  }
}