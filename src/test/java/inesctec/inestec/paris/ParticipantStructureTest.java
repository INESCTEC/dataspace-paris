package inesctec.inestec.paris;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import de.fraunhofer.iais.eis.Participant;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@SpringBootTest
class ParticipantStructureTest {

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

  private JsonConverter jsonConverter;

  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantStructureTest.class);



  @BeforeEach
  void startVariables(){
    jsonConverter = new JsonConverter();
  }


    /*
    * Participant is complete, except for memberParticipant
    * Should Pass
    * */
  @Test
  void convertToDatabaseParticipant_Pass1() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest1.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }

  /*
  * Test with Participant children
  * Should Pass
  * */
  @Test
  void convertToDatabaseParticipant_Pass2(){
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest2.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }


  /*
   * Participant does not have MemberPerson, EvaluationFacility, MemberParticipant, vat,
   * participantRefinement, legalName, businessIdentifier, jurisdiction
   *
   * Should Pass
   * */
  @Test
  void convertToDatabaseParticipant_Pass3() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest3.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }


  /*
   * Participant:
   *    - has MemberPerson with all a null, except id
   *    - Does not have:
   *              - EvaluationFacility, MemberParticipant, vat, legalName,
   *              - businessIdentifier, jurisdiction
   *    - Has participantRefinement: Constraint
   * */
  @Test
  void convertToDatabaseParticipant_Pass4() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest4.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }

  /*
   * Participant:
   *    - has MemberPerson with all a null, except id
   *    - Does not have:
   *              - EvaluationFacility, MemberParticipant, vat, legalName,
   *              - businessIdentifier, jurisdiction
   *    - Has participantRefinement: Constraint  and Logical Constraint at AND
   * */
  @Test
  void convertToDatabaseParticipant_Pass5() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest5.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }

  /*
   * Participant:
   *    - has MemberPerson with all a null, except id
   *    - Does not have:
   *              - EvaluationFacility, MemberParticipant, vat, legalName,
   *              - businessIdentifier, jurisdiction
   *    - Has participantRefinement: Constraint  and Logical Constraint at OR
   * */
  @Test
  void convertToDatabaseParticipant_Pass6() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest6.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }

  /*
   * Participant:
   *    - has MemberPerson with all a null, except id
   *    - Does not have:
   *              - EvaluationFacility, MemberParticipant, vat, legalName,
   *              - businessIdentifier, jurisdiction
   *    - Has participantRefinement: Constraint  and Logical Constraint at XONE
   * */
  @Test
  void convertToDatabaseParticipant_Pass7() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest7.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }


  /*
   * Should fail on ids:corporateHomepage
   * */
  @Test
  void convertToDatabaseParticipant_Fail1(){
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest8.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }


  }

  private void executeConverter(File file){
    try {
      Participant participant = jsonConverter.json2Object(Participant.class,
              jsonConverter.file2Json(file));

      new ParticipantConverter(participantService, certificationService, logicalConstraintService, personService, businessIdentifierService).convertToDatabaseParticipant(participant);
    }
    catch (MismatchedInputException ex){
      LOGGER.info("Error Deserializing JSON");
      System.err.println(ex);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}