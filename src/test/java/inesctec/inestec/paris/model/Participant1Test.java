package inesctec.inestec.paris.model;

import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.CertificationLevel;
import de.fraunhofer.iais.eis.LeftOperand;
import inesctec.inestec.paris.builder.LogicalConstraintBuilder;
import inesctec.inestec.paris.builder.ParticipantBuilder;
import inesctec.inestec.paris.builder.ParticipantCertificationBuilder;
import inesctec.inestec.paris.dto.Participant1Dto;
import inesctec.inestec.paris.dto.PersonDto;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class Participant1Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(Participant1Test.class);
  Participant1 producer;
  TestUtil testUtil;
  
  @BeforeEach
  void setUp() {
    testUtil=new TestUtil();
    producer=testUtil.addProducer();
  }

  
  /*@Test
  void update() {
  }

  @Test
  void testUpdate() {
  }*/

  @Test
  void convertToParticipantDto_CompleteParticipant() {
    producer.setMemberPerson(Set.of(testUtil.person()));
    producer.setParticipantRefinement(testUtil.refinement_none());
    producer.setConnectorSet(Set.of(testUtil.connector_producer()));
    producer.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer()));


    Person person=testUtil.person();

    Participant1Dto expectedParticipantDto=testUtil.addProducerDto();
    expectedParticipantDto.setMemberPerson(Set.of(person.convertToPersonDto()));
    //expectedParticipantDto.setChildren(testUtil.addSubcontracted().convertToParticipantDto(testUtil.addSubcontracted()));
    expectedParticipantDto.setParticipantRefinement(testUtil.refinement_none().convertToLogicalConstDto());
    expectedParticipantDto.setConnectorSet(Set.of(testUtil.connector_producer().convertToConnectorDto()));
    expectedParticipantDto.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer().convertToBusinessIdentifierDto()));
  }

  @Test
  void convertToParticipantDto_NoPerson_NoChild_NoRefinement_NoConnector_NoBusinessIdentifier() {

    // Create the Expected Participant DTO. I need to remove the children manually
    Participant1Dto expectedParticipantDto=testUtil.addProducerDto();


    System.out.println(expectedParticipantDto.toString());
    // The producer.toJson() automatically calls the convertToParticipantDto(), which is the one that I'm testing
    assertEquals(expectedParticipantDto.toJson(), producer.toJson());
    
  }

  @Test
  void convertToParticipantDto_NoRefinement() {
    producer.setMemberPerson(Set.of(testUtil.person()));
    producer.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer()));
    producer.setConnectorSet(Set.of(testUtil.connector_producer()));

    Person person=testUtil.person();

    // Create the expected participant DTO
    Participant1Dto expectedParticipantDto=testUtil.addProducerDto();
    expectedParticipantDto.setConnectorSet(Set.of(testUtil.connector_producer().convertToConnectorDto()));
    expectedParticipantDto.setMemberPerson(Set.of(person.convertToPersonDto()));
    //expectedParticipantDto.setChildren(testUtil.addSubcontracted().convertToParticipantDto(testUtil.addSubcontracted()));
    expectedParticipantDto.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer().convertToBusinessIdentifierDto()));

    // The producer.toJson() automatically calls the convertToParticipantDto(), which is the one that I'm testing
    assertEquals(expectedParticipantDto.toJson(), producer.toJson());
  }

  @Test
  void convertToParticipantDtoNoConnector() {
    producer.setMemberPerson(Set.of(testUtil.person()));
    producer.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer()));
    producer.setParticipantRefinement(testUtil.refinement_none());

    Person person=testUtil.person();

    // Create the expected participant DTO
    Participant1Dto expectedParticipantDto=testUtil.addProducerDto();
    expectedParticipantDto.setParticipantRefinement(testUtil.refinement_none().convertToLogicalConstDto());
    expectedParticipantDto.setMemberPerson(Set.of(person.convertToPersonDto()));
    //expectedParticipantDto.setChildren(testUtil.addSubcontracted().convertToParticipantDto(testUtil.addSubcontracted()));
    expectedParticipantDto.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer().convertToBusinessIdentifierDto()));

    // The producer.toJson() automatically calls the convertToParticipantDto(), which is the one that I'm testing
    assertEquals(expectedParticipantDto.toJson(), producer.toJson());
  }

  @Test
  void convertToParticipantDto_NoChild() {
    producer.setMemberPerson(Set.of(testUtil.person()));
    producer.setParticipantRefinement(testUtil.refinement_none());
    producer.setConnectorSet(Set.of(testUtil.connector_producer()));
    producer.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer()));

    Person person=testUtil.person();

    // Create the expected participant DTO
    Participant1Dto expectedParticipantDto=testUtil.addProducerDto();
    expectedParticipantDto.setParticipantRefinement(testUtil.refinement_none().convertToLogicalConstDto());
    expectedParticipantDto.setConnectorSet(Set.of(testUtil.connector_producer().convertToConnectorDto()));
    expectedParticipantDto.setMemberPerson(Set.of(person.convertToPersonDto()));
    expectedParticipantDto.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer().convertToBusinessIdentifierDto()));

    assertEquals(expectedParticipantDto.toJson(), producer.convertToParticipantDto().toJson());

    // The producer.toJson() automatically calls the convertToParticipantDto(), which is the one that I'm testin
    //assertEquals(expectedParticipantDto.toJson(), producer.toJson());
  }

  @Test
  void convertToParticipantDto_NoPerson() {
    // Update the Participant to convert
    producer.setParticipantRefinement(testUtil.refinement_none());
    producer.setConnectorSet(Set.of(testUtil.connector_producer()));
    producer.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer()));

    // Create the expected participant DTO
    Participant1Dto expectedParticipantDto=testUtil.addProducerDto();
    expectedParticipantDto.setParticipantRefinement(testUtil.refinement_none().convertToLogicalConstDto());
    expectedParticipantDto.setConnectorSet(Set.of(testUtil.connector_producer().convertToConnectorDto()));
    //expectedParticipantDto.setChildren(testUtil.addSubcontracted().convertToParticipantDto(testUtil.addSubcontracted()));
    expectedParticipantDto.setBusinessIdentifier(Set.of(testUtil.businessIdentifier_Producer().convertToBusinessIdentifierDto()));

    // The producer.toJson() automatically calls the convertToParticipantDto(), which is the one that I'm testing
    assertEquals(expectedParticipantDto.toJson(), producer.toJson());
  }

  @Test
  void convertToParticipantDto_NoBusinessIdentifier() {
    // Update the Participant to convert

    producer.setParticipantRefinement(testUtil.refinement_none());
    producer.setConnectorSet(Set.of(testUtil.connector_producer()));
    producer.setMemberPerson(Set.of(testUtil.person()));

    Person person=testUtil.person();

    // Create the expected participant DTO
    Participant1Dto expectedParticipantDto=testUtil.addProducerDto();
    expectedParticipantDto.setParticipantRefinement(testUtil.refinement_none().convertToLogicalConstDto());
    expectedParticipantDto.setConnectorSet(Set.of(testUtil.connector_producer().convertToConnectorDto()));
    expectedParticipantDto.setMemberPerson(Set.of(person.convertToPersonDto()));
    //expectedParticipantDto.setChildren(testUtil.addSubcontracted().convertToParticipantDto(testUtil.addSubcontracted()));

    // The producer.toJson() automatically calls the convertToParticipantDto(), which is the one that I'm testing
    assertEquals(expectedParticipantDto.toJson(), producer.toJson());
  }

  @Test
  void equals_CompareIncompletePartWithComplete() {
    ZonedDateTime certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
    ZonedDateTime certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

    Participant1 part1=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"),
            new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    /*.evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))*/
                    .build())
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            /*.participantRefinement(
                    new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
                            .constraints(new inesctec.inestec.paris.builder.ConstraintBuilder()
                                    .leftOperand(LeftOperand.SECURITY_LEVEL)
                                    .operator(String.valueOf(BinaryOperator.EQUALS))
                                    .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
                                    .unit("http://dbpedia.org/resource/Euro")
                                    .constraint_ids_ID("https://part/ref/01")
                                    .build())
                            .build()
            )*/
            /*.memberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe")
                    .givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build()))*/
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

    Participant1 part2=new ParticipantBuilder(
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
                    new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
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

    LOGGER.info("Asserting that the Participants are different");
    assertNotEquals(part1, part2);
    LOGGER.info("... test succeded.");

    LOGGER.info("Asserting that the Participants are different after updating Person");
    // Add two Person
    part1.setMemberPerson(Set.of(
            new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe1")
                    .givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build(),
            new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe")
                    .givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build()));
    assertNotEquals(part1, part2);
    LOGGER.info("... test succeded.");

    LOGGER.info("Asserting that the Participants are different after updating Person");
    // Update Person
    part1.setMemberPerson((Set<inesctec.inestec.paris.model.Person>) null);
    assertNotEquals(part1, part2);
    LOGGER.info("... test succeded.");

    // Update Person
    LOGGER.info("Asserting that the Participants are different after updating Person");
    part1.setMemberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build()));
    assertNotEquals(part1, part2);
    assertEquals(part1.getMemberPerson(), part2.getMemberPerson());
    LOGGER.info("... test succeded.");

    LOGGER.info("Asserting that the Participants are different after updating Certification");
    // Update Certification
    part1.setParticipantCertification(new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build());
    assertNotEquals(part1, part2);
    System.out.println(part1.getParticipantCertification().toJson());
    System.out.println(part2.getParticipantCertification().toJson());
    assertEquals(part1.getParticipantCertification(), part2.getParticipantCertification());
    LOGGER.info("... test succeded.");

    LOGGER.info("Asserting that the Participants are EQUAL after updating Refinement");
    // Update Refinement
    part1.setParticipantRefinement(new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
            .constraints(List.of(new inesctec.inestec.paris.builder.ConstraintBuilder()
                    .leftOperand(LeftOperand.SECURITY_LEVEL)
                    .operator(String.valueOf(BinaryOperator.EQUALS))
                    .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
                    .unit("http://dbpedia.org/resource/Euro")
                    .constraint_ids_ID("https://part/ref/01")
                    .build()))
            .build());
    assertEquals(part1, part2);
    assertEquals(part1.getParticipantRefinement(), part2.getParticipantRefinement());
    LOGGER.info("... test succeded.");

  }

  @Test
  void equals_UpdatePart() {
    ZonedDateTime certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
    ZonedDateTime certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

    Participant1 part1=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"),
            new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("")
                    //.description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build())
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .participantRefinement(
                    new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
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

    Participant1 part2=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"),
            new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("")
                    //.description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build())
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .participantRefinement(
                    new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
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

    assertEquals(part1, part2);

    // Add two Person
    part1.setMemberPerson(Set.of(
            new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe1")
                    .givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build(),
            new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe")
                    .givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build()));
    assertNotEquals(part1, part2);

    // Update Person
    part1.setMemberPerson((Set<inesctec.inestec.paris.model.Person>) null);
    assertNotEquals(part1, part2);

    // Update Person
    part1.setMemberPerson(Set.of(new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build()));
    assertEquals(part1, part2);

    // Update Certification
    part1.setParticipantCertification(new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build());
    assertNotEquals(part1, part2);

    // Make them Equal Again
    part2.setParticipantCertification(new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build());
    assertEquals(part1, part2);

    // Update Refinement
    part1.setParticipantRefinement(new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
            .constraints(List.of(new inesctec.inestec.paris.builder.ConstraintBuilder()
                            .leftOperand(LeftOperand.SECURITY_LEVEL)
                            .operator(String.valueOf(BinaryOperator.EQUALS))
                            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
                            .unit("http://dbpedia.org/resource/Euro")
                            .constraint_ids_ID("https://part/ref/01")
                            .build(),
                    new inesctec.inestec.paris.builder.ConstraintBuilder()
                            .leftOperand(LeftOperand.SECURITY_LEVEL)
                            .operator(String.valueOf(BinaryOperator.EQUALS))
                            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
                            .unit("http://dbpedia.org/resource/Euro")
                            .constraint_ids_ID("https://part/ref/02")
                            .build())
            )
            .build());
    assertNotEquals(part1, part2);

    // Make them Equal Again
    part2.setParticipantRefinement(new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
            .constraints(List.of(new inesctec.inestec.paris.builder.ConstraintBuilder()
                            .leftOperand(LeftOperand.SECURITY_LEVEL)
                            .operator(String.valueOf(BinaryOperator.EQUALS))
                            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
                            .unit("http://dbpedia.org/resource/Euro")
                            .constraint_ids_ID("https://part/ref/01")
                            .build(),
                    new inesctec.inestec.paris.builder.ConstraintBuilder()
                            .leftOperand(LeftOperand.SECURITY_LEVEL)
                            .operator(String.valueOf(BinaryOperator.EQUALS))
                            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
                            .unit("http://dbpedia.org/resource/Euro")
                            .constraint_ids_ID("https://part/ref/02")
                            .build())
            )
            .build());
    assertEquals(part1, part2);

    // Update Title
    part1.setName("Participant Name");
    assertNotEquals(part1, part2);

    // Make them Equal Again
    part2.setName("Participant Name");
    assertEquals(part1, part2);
  }

  /*@Test
  void equals_testChildren() {
    LocalDateTime certificationDate = LocalDateTime.of(
            LocalDate.of(2022, 03, 23),
            LocalTime.of(12, 23,22));

    LocalDateTime certificationEndDate = LocalDateTime.of(
            LocalDate.of(2025, 03, 23),
            LocalTime.of(12, 23,22));
    Participant1 part1=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"),
            new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("")
                    //.description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build())
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .participantRefinement(
                    new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
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
            .build();

    Participant1 part2=new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"),
            new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("")
                    //.description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
                    .build())
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .description(List.of("Producer Description"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .participantRefinement(
                    new LogicalConstraintBuilder(URI.create("https://part/ref/01/logicalConstraint"))
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
            .build();

    // part1 has a child
    assertNotEquals(part1, part2);

    // Add Child to Part2
    assertEquals(part1, part2);


    // add Subcontracted as a child of Producer. This tests with a more complex hierarchy


    System.out.println(part1.toJson());

  }*/

}