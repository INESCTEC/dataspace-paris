package inesctec.inestec.paris;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import de.fraunhofer.iais.eis.Participant;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.util.ParticipantUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@SpringBootTest
@Transactional
@Sql({"/remove_data.sql"})
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
class ParticipantInsertationTest {

  @Autowired
  EvaluationFacilityService evaluationFacilityService;
  @Autowired
  PersonService personService;
  @Autowired
  ParticipantService participantService;
  @Autowired
  ParticipantCertificationService certificationService;
  @Autowired
  BusinessIdentifierService businessIdentifierService;

  @Autowired
  ConstraintService constraintService;
  @Autowired
  LogicalConstraintService logicalConstraintService;
  @Autowired
  ConnectorService connectorService;


  @Autowired
  ParticipantUtil participantUtil;

  private JsonConverter jsonConverter;

  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantInsertationTest.class);



  @BeforeEach
  void startVariables(){
    jsonConverter = new JsonConverter();
    /*participantUtil =new ParticipantUtil(evaluationFacilityService, personService,participantService,
            certificationService, constraintService, logicalConstraintService, businessIdentifierService, connectorService);*/
  }

    /*
    * Participant is complete, except for memberParticipant
    *
    * Not Defined: Connector, Constraint, Logical Constraint
    * Inserted on DB: Participant, Certification, Person
    * Used values already on DB: Evaluation Facility
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
  *
  * Not Defined: Connector, Constraint, Logical Constraint
   * Inserted on DB: Participant, Certification, Person, Evaluation Facility
   * Used values already on DB: ---
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
   * Not Defined: Connector, Constraint, Logical Constraint, Person, Evaluation Facility
   * Inserted on DB: Participant, Certification
   * Used values already on DB: ---
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
   *
   * Not Defined: Connector
   * Inserted on DB: Participant, Certification, Person, Constraint, Logical Constraint
   * Used values already on DB: Evaluation Facility
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
   *
   * Not Defined: Connector
   * Inserted on DB: Participant, Certification, Person, Constraint, Logical Constraint
   * Used values already on DB: Evaluation Facility
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
   *
   * Not Defined: Connector
   * Inserted on DB: Participant, Certification, Person, Constraint, Logical Constraint
   * Used values already on DB: Evaluation Facility
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
   *
   * Not Defined: Connector
   * Inserted on DB: Participant, Certification, Person, Constraint, Logical Constraint
   * Used values already on DB: Evaluation Facility
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

  // TODO: Tests 9 and 11. Right now the addParticipant does not accept the property Connector
  /*
   * Not Defined: Constraint, Logical Constraint
   * Inserted on DB: Participant, Certification, Person
   * Used values already on DB: Connector, Evaluation Facility
   * */
  @Test
  void convertToDatabaseParticipant_Pass9() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest9.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }

  /*
   * Not Defined: Constraint, Logical Constraint
   * Inserted on DB: Participant, Certification, Person, Connector
   * Used values already on DB: Evaluation Facility
   * */
  @Test
  void convertToDatabaseParticipant_Pass11() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/ParticipantTest11.jsonc");
      executeConverter(file);
    }
    catch (FileNotFoundException exception){
      System.err.println("File not found!!");
    }
  }

  /*
   * Not Defined: Constraint, Logical Constraint
   * Inserted on DB: Participant, Certification, Person, Connector
   * Used values already on DB: Evaluation Facility
   * */
  @Test
  void testing() {
    try {
      File file = ResourceUtils.getFile("classpath:jsonFiles/testing.jsonc");
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

      participantUtil.addParticipant(participant);
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