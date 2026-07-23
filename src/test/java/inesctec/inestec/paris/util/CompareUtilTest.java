package inesctec.inestec.paris.util;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.converter.XMLGregorianCalendarConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CompareUtilTest {

  private XMLGregorianCalendarConverter calendarConverter;
  private LocalDateTime certificationDate, certificationEndDate;
  private Participant part1;
  @BeforeEach
  void setUp() throws URISyntaxException {
    calendarConverter=new XMLGregorianCalendarConverter();
    certificationDate = LocalDateTime.of(
            LocalDate.of(2022, 03, 23),
            LocalTime.of(12, 23,22));

    certificationEndDate = LocalDateTime.of(
            LocalDate.of(2025, 03, 23),
            LocalTime.of(12, 23,22));

    part1 =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
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
                    new BusinessIdentifierBuilder()
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
  void compareFraunhParticipants_Equal() throws URISyntaxException {
    Participant part2 =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
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
                    new BusinessIdentifierBuilder()
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

    assertTrue(CompareUtil.compareFraunhParticipants(part1, part2));
  }

  @Test
  void compareFraunhParticipants_NotEqual() throws URISyntaxException {
    Participant part2 =new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"))
            ._title_(Util.asList(new TypedLiteral("Producer")))
            ._legalName_("Producer")
            ._description_(new TypedLiteral("Producer Description"))
            ._primarySite_(new SiteBuilder()._siteAddress_("Street X").build())
            ._corporateEmailAddress_(Util.asList("producer@inesctec.pt"))
            ._corporateHomepage_(new URI("http://www.stvgodigital.pt"))
            ._legalForm_("e.V.")
            ._vatID_("123456789")
            ._jurisdiction_("Portugal")
            .build();

    assertFalse(CompareUtil.compareFraunhParticipants(part1, part2));

    // Add Certification
    part2.setParticipantCertification(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"))
            ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
            ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
            ._title_(new TypedLiteral("Participant Certification 2"))
            ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
            ._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
            .build());
    assertFalse(CompareUtil.compareFraunhParticipants(part1, part2));
    assertTrue(Objects.equals(part1.getParticipantCertification(),part2.getParticipantCertification()));

    // Add Refinement
    part2.setParticipantRefinement(new ConstraintBuilder(URI.create("https://part/ref/01"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            .build());
    assertFalse(CompareUtil.compareFraunhParticipants(part1, part2));
    assertTrue(Objects.equals(part1.getParticipantRefinement(),part2.getParticipantRefinement()));

    // Add BusinessIdentifier
    part2.setBusinessIdentifier(List.of(
            new BusinessIdentifierBuilder()
                    ._identifierNumber_("123456789")
                    ._identifierSystem_("CAE")
                    .build()));
    assertFalse(CompareUtil.compareFraunhParticipants(part1, part2));
    assertTrue(Objects.equals(part1.getBusinessIdentifier(),part2.getBusinessIdentifier()));

    // Add Person
    part2.setMemberPerson(List.of(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build()));
    assertTrue(CompareUtil.compareFraunhParticipants(part1, part2));
    assertTrue(Objects.equals(part1.getMemberPerson(),part2.getMemberPerson()));
  }

  // TODO
  @Test
  void compareDBParticipants() {
  }

  @Test
  void compareSet() {
  }

  @Test
  void compare_SortByLastName() {
    de.fraunhofer.iais.eis.Person person1=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();
    de.fraunhofer.iais.eis.Person person2=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/bfgcfgeth6-f9f8-4cc1-b144-fgqertdf5"))
            ._familyName_("Doe2")
            ._givenName_("John2")
            ._phoneNumber_(List.of("924756829"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();

    // Add Person to List in a different order
    List< de.fraunhofer.iais.eis.Person> personsList1 = new ArrayList<>();
    personsList1.add(person1);
    personsList1.add(person2);

    List< de.fraunhofer.iais.eis.Person> personsList2 = new ArrayList<>();
    personsList2.add(person2);
    personsList2.add(person1);

    assertTrue(CompareUtil.comparePersons(personsList1, personsList2));

    // Clear Lists
    personsList1.clear();
    personsList2.clear();

    // Add Person to List in the same order
    personsList1.add(person1);
    personsList1.add(person2);

    personsList2.add(person1);
    personsList2.add(person2);

    assertTrue(CompareUtil.comparePersons(personsList1, personsList2));

    // Clear Lists
    personsList1.clear();
    personsList2.clear();


    // Add 2 Persons to one list AND one Person to another list
    personsList1.add(person1);
    personsList1.add(person2);

    personsList2.add(person2);

    assertFalse(CompareUtil.comparePersons(personsList1, personsList2));
  }

  @Test
  void compare_SortByLastName2() {
    de.fraunhofer.iais.eis.Person person1=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();
    de.fraunhofer.iais.eis.Person person2=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/bfgcfgeth6-f9f8-4cc1-b144-fgqertdf5"))
            ._familyName_("Doe2")
            ._givenName_("John2")
            ._phoneNumber_(List.of("924756829", "9856321475"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();
    de.fraunhofer.iais.eis.Person person3=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fgsfdrfsew-f9f8-4cc1-b144-fgqertdf5"))
            ._familyName_("Doe2")
            ._givenName_("John2")
            ._phoneNumber_(List.of("924756829", "9856321475"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();

    // Add Person to List in a different order
    List< de.fraunhofer.iais.eis.Person> personsList1 = new ArrayList<>();
    personsList1.add(person1);
    personsList1.add(person3);

    List< de.fraunhofer.iais.eis.Person> personsList2 = new ArrayList<>();
    //personsList2.add(person2);
    personsList2.add(person1);

    assertFalse(CompareUtil.comparePersons(personsList1, personsList2));
  }


}