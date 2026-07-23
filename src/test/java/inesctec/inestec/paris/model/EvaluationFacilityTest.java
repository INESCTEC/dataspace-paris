package inesctec.inestec.paris.model;

import de.fraunhofer.iais.eis.CertificationLevel;
import inesctec.inestec.paris.builder.ParticipantCertificationBuilder;
import inesctec.inestec.paris.dto.EvaluationFacilityDto;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationFacilityTest {

  @Test
  void convertToEvaluationFacilityDto_NotNull() {
    EvaluationFacility evaluationFacility=new EvaluationFacility(URI.create("https://ev/01"));
    EvaluationFacilityDto expectedEvaluationFacility=new EvaluationFacilityDto(URI.create("https://ev/01"));

    assertEquals(evaluationFacility.convertToEvaluationFacilityDto().toString(), expectedEvaluationFacility.toString());

  }

  @Test
  void equal() {
    EvaluationFacility evaluationFacility=new EvaluationFacility(URI.create("https://ev/01"));
    EvaluationFacility expectedEvaluationFacility=new EvaluationFacility(URI.create("https://ev/01"));

    // EvaluationFacility should be equal
    assertTrue(evaluationFacility.equals(expectedEvaluationFacility));
    assertEquals(evaluationFacility, expectedEvaluationFacility);

    // Update the evaluationFacility. Now they should be different
    ParticipantCertification certification=new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC")))
            .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
            .title("Participant Certification 2")
            //.description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .evaluationFacility(evaluationFacility)
            .build();

    // Make the expectedEvaluationFacility equal to the evaluationFacility
    certification.setEvaluationFacility(expectedEvaluationFacility);
    expectedEvaluationFacility.setCertifications(Set.of(certification));
    // EvaluationFacility should be equal
    assertTrue(evaluationFacility.equals(expectedEvaluationFacility));
    assertEquals(evaluationFacility, expectedEvaluationFacility);
  }

}