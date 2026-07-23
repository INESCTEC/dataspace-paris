package inesctec.inestec.paris.withJsonFiles;

import de.fraunhofer.iais.eis.Participant;
import inesctec.inestec.paris.DataLoader;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.converter.ParticipantConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.DatatypeConfigurationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@Sql({"/remove_data.sql"})
class JsonTestOfParticipantConverter {

  @Autowired
  EvaluationFacilityService evaluationFacilityService;
  @Autowired
  PersonService personService;
  @Autowired
  ParticipantService participantService;
  @Autowired
  ParticipantCertificationService certificationService;
  @Autowired
  LogicalConstraintService logicalConstraintService;
  @Autowired
  BusinessIdentifierService businessIdentifierService;
  @Autowired
  DataLoader dataLoader;

  private List<Participant1> participants;
  private Optional<Participant1> producer;
  private Optional<Participant1> subcontracted;

  @BeforeEach
  void getParticipant(){
    participants = (List<Participant1>) participantService.findAll();


    participants.forEach(System.err::println);
    producer = participantService.findParticipantByParticipantIDSID(
        URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"));

    subcontracted = participantService.findParticipantByParticipantIDSID(
        URI.create("https://stvgodigital.pt/ids/participant/id/subcontracted-01"));
  }


  @Test
  @Transactional
  void convertParticipant_DummyParticipant() throws DatatypeConfigurationException, URISyntaxException {
    Participant participant=new ParticipantConverter(participantService, certificationService, logicalConstraintService, personService, businessIdentifierService).buildDummyParticipant();
    System.out.println(participant.toRdf());
  }

  @Test
  void testInvalidParticipant(){
    Optional<Participant1> invalidParticipant = participantService.findParticipantByParticipantIDSID(
        URI.create("https://stvgodigital.pt/ids/participant/id/supplier-02"));

    System.out.println("Participant is empty: "+invalidParticipant.isEmpty());
    if(invalidParticipant.isEmpty()){
      System.err.println("Success!!");
    }
  }
}