package inesctec.inestec.paris.model;

import de.fraunhofer.iais.eis.CertificationLevel;
import inesctec.inestec.paris.builder.ParticipantCertificationBuilder;
import inesctec.inestec.paris.dto.ParticipantCertificationDto;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantCertificationTest {
  TestUtil testUtil;
  ZonedDateTime certificationDate, certificationEndDate;
  ParticipantCertification bdCertification, bdCertification2;

  @BeforeEach
  void setUp() {
    testUtil=new TestUtil();
    certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
    certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

    bdCertification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();

    bdCertification2=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();
  }

  @Test
  void convertToCertificationDto() {

    ParticipantCertificationDto expectedCertificationDto=new ParticipantCertificationDto();
    expectedCertificationDto.setCertificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK);
    expectedCertificationDto.setLastValidDate(certificationDate);
    expectedCertificationDto.setMembershipEnd(certificationEndDate);
    expectedCertificationDto.setCertificationIdsID(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"));
    expectedCertificationDto.setTitle("Participant Certification 2");
    expectedCertificationDto.setDescription("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK);


    ParticipantCertification certification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .build();

    assertEquals(certification.convertToCertificationDto().toJson(), expectedCertificationDto.toJson());
  }

  @Test
  void toJson() {
    String expectedResult="{\n" +
            "  \"membershipEnd\": \"2025-03-23\",\n" +
            "  \"description\": \"Participant with Certification Level https://w3id.org/idsa/code/PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK\",\n" +
            "  \"title\": \"Participant Certification 2\",\n" +
            "  \"evaluationFacility\": {\n" +
            "    \"evaluationFacilityID\": \"https://ev/01\"\n" +
            "  },\n" +
            "  \"certificationIdsID\": \"https://stvgodigital.pt/ids/participantCertification/id/02\",\n" +
            "  \"certificationLevel\": \"PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK\",\n" +
            "  \"lastValidDate\": \"2022-03-23\"\n" +
            "}";

    ParticipantCertification certification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .membershipEnd(certificationEndDate)
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(new EvaluationFacility(URI.create("https://ev/01")))
            .build();

    System.out.println(certification.toJson());
    assertEquals(certification.toJson(), expectedResult);
  }

  @Test
  void equals() throws Exception {

    System.out.println(bdCertification.toJson2());
    System.err.println(bdCertification2.toJson2());

    // Title is the same and Description does not exist
    assertEquals(bdCertification.toJson2(), bdCertification2.toJson2());
    assertTrue(bdCertification.equals(bdCertification2));


    // Update the Description on one. Equals should be false
    bdCertification.setDescription("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK);
    assertFalse(bdCertification.equals(bdCertification2));

    // Update the Description on the other. Equals should now be true
    bdCertification2.setDescription("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK);
    assertEquals(bdCertification,bdCertification2);
    assertTrue(bdCertification.equals(bdCertification2));
}
}