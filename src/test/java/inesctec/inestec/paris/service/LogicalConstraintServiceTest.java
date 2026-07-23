package inesctec.inestec.paris.service;

import de.fraunhofer.iais.eis.AbstractConstraint;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.util.RdfResource;
import inesctec.inestec.paris.builder.ConstraintBuilder;
import inesctec.inestec.paris.builder.LogicalConstraintBuilder;
import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import inesctec.inestec.paris.model.Constraint;
import inesctec.inestec.paris.model.LogicalConstraint;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.datatype.DatatypeConfigurationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LogicalConstraintServiceTest {

  @Autowired
  private ParticipantService participantService;
  @Autowired private ParticipantCertificationService certificationService;
  @Autowired private LogicalConstraintService logicalConstraintService;
  @Autowired private PersonService personService;
  @Autowired private BusinessIdentifierService businessIdentifierService;

  private TestUtil testUtil;
  @BeforeEach
  void setUp() {
    testUtil=new TestUtil();
  }

  @Test
  void checkParticipantRefinement_LogicalConstraint() throws DatatypeConfigurationException, URISyntaxException {

    Participant fraunhoferPart=new ParticipantConverter(participantService, certificationService, logicalConstraintService, personService, businessIdentifierService).buildDummyParticipant();
    Participant1 producer=testUtil.addProducer();

    // Usually the participants sent on the method are the same, but in different formats
    LogicalConstraint refinement=logicalConstraintService.convertToDatabaseModel(fraunhoferPart.getParticipantRefinement(), producer);

    LogicalConstraint expectedRefinement=new LogicalConstraint();
    expectedRefinement.setLogicalOperator(LogicalConstraintOperator.AND);
    expectedRefinement.setParticipant(producer);

    expectedRefinement.addConstraint(new ConstraintBuilder()
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
            .unit("http://dbpedia.org/resource/Euro")
            .constraint_ids_ID("https://w3id.org/idsa/autogen/logicalConstraint/4aea7738-b340-401c-b0f2-1fc78053cbb2")
            .build());
    expectedRefinement.addConstraint(new ConstraintBuilder()
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
            .unit("http://dbpedia.org/resource/Euro")
            .constraint_ids_ID("https://w3id.org/idsa/autogen/logicalConstraint/4aea7738-b340-401c-b0f2-1fc78053cbb2")
            .build());
    expectedRefinement.addConstraint(new ConstraintBuilder()
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperandReference("http://right/operand/reference/test")
            .constraint_ids_ID("https://w3id.org/idsa/autogen/logicalConstraint/4aea7738-b340-401c-b0f2-1fc78053cbb2")
            .build());


    List<Constraint> expectedRefinementConstraints=expectedRefinement.getConstraints();
    List<Constraint> refinementConstraints=refinement.getConstraints();

    assertEquals(expectedRefinement.getLogicalOperator(), refinement.getLogicalOperator());
    assertEquals(expectedRefinement.getParticipant(), refinement.getParticipant());
    assertEquals(expectedRefinementConstraints.size(), refinementConstraints.size());

    // Asserts that all Constraints are the Same (ignoring Constraint ID)
    for (int i=0; i<expectedRefinementConstraints.size(); i++) {
      assertEquals(expectedRefinementConstraints.get(i).getUnit(), refinementConstraints.get(i).getUnit());
      assertEquals(expectedRefinementConstraints.get(i).getOperator(), refinementConstraints.get(i).getOperator());
      assertEquals(expectedRefinementConstraints.get(i).getPipEndpoint(), refinementConstraints.get(i).getPipEndpoint());
      assertEquals(expectedRefinementConstraints.get(i).getRightOperand(), refinementConstraints.get(i).getRightOperand());
      assertEquals(expectedRefinementConstraints.get(i).getRightOperandReference(), refinementConstraints.get(i).getRightOperandReference());
    }

  }

  @Test
  void checkParticipantRefinement_Constraint() throws DatatypeConfigurationException, URISyntaxException {

    Participant fraunhoferPart=testUtil.fraunhoferPartWithOnlyOneConstraint();
    Participant1 producer=testUtil.addProducer();

    // Usually the participants sent on the method are the same, but in different formats
    LogicalConstraint refinement=logicalConstraintService.convertToDatabaseModel(fraunhoferPart.getParticipantRefinement(), producer);

    LogicalConstraint expectedRefinement=new LogicalConstraint();
    expectedRefinement.setLogicalOperator(LogicalConstraintOperator.NONE);
    expectedRefinement.setParticipant(producer);

    expectedRefinement.addConstraint(new ConstraintBuilder()
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")
            .unit("http://dbpedia.org/resource/Euro")
            .constraint_ids_ID("https://w3id.org/idsa/autogen/logicalConstraint/4aea7738-b340-401c-b0f2-1fc78053cbb2")
            .build());


    List<Constraint> expectedRefinementConstraints=expectedRefinement.getConstraints();
    List<Constraint> refinementConstraints=refinement.getConstraints();

    assertEquals(expectedRefinement.getLogicalOperator(), refinement.getLogicalOperator());
    assertEquals(expectedRefinement.getParticipant(), refinement.getParticipant());
    assertEquals(expectedRefinementConstraints.size(), refinementConstraints.size());

    // Asserts that all Constraints are the Same (ignoring Constraint ID)
    for (int i=0; i<expectedRefinementConstraints.size(); i++) {
      assertEquals(expectedRefinementConstraints.get(i).getUnit(), refinementConstraints.get(i).getUnit());
      assertEquals(expectedRefinementConstraints.get(i).getOperator(), refinementConstraints.get(i).getOperator());
      assertEquals(expectedRefinementConstraints.get(i).getPipEndpoint(), refinementConstraints.get(i).getPipEndpoint());
      assertEquals(expectedRefinementConstraints.get(i).getRightOperand(), refinementConstraints.get(i).getRightOperand());
      assertEquals(expectedRefinementConstraints.get(i).getRightOperandReference(), refinementConstraints.get(i).getRightOperandReference());
    }

  }

  @Test
  void convertToInfomodel() {

    de.fraunhofer.iais.eis.Constraint infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

    de.fraunhofer.iais.eis.Constraint infomodel_constraint2= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint2"))
            ._leftOperand_(LeftOperand.ENDPOINT)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("http://endpoint/01")).build();

    List<de.fraunhofer.iais.eis.Constraint> infomodel_constraints=new ArrayList<>();
    infomodel_constraints.add(infomodel_constraint1);
    infomodel_constraints.add(infomodel_constraint2);

    // Creates a List
    Constraint db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

    Constraint db_constraint2= new ConstraintBuilder()
            .leftOperand(LeftOperand.ENDPOINT)
            .constraint_ids_ID("http://constraint2")
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("http://endpoint/01")).build();

    List<Constraint> db_constraints=new ArrayList<>();
    db_constraints.add(db_constraint1);
    db_constraints.add(db_constraint2);


    // Tests with NONE -> Uses the db_constraint1 and its equivalent (infomodel_constraint1)
    LogicalConstraint refinementToConvert=new LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"),
            LogicalConstraintOperator.NONE,
            db_constraint1).build();
    AbstractConstraint convertedRefinement=logicalConstraintService.convertToInfomodel(refinementToConvert);
    System.out.println(convertedRefinement.toRdf());
    System.out.println(infomodel_constraint1.toRdf());
    assertThat(convertedRefinement).isEqualTo(infomodel_constraint1);


    // Tests with AND
    refinementToConvert=new LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"),
            LogicalConstraintOperator.AND,
            db_constraints).build();
    de.fraunhofer.iais.eis.LogicalConstraint expectedRefinement=new de.fraunhofer.iais.eis.LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"))
            ._and_(infomodel_constraints)
            .build();
    convertedRefinement=logicalConstraintService.convertToInfomodel(refinementToConvert);
    assertThat(convertedRefinement).isEqualTo(expectedRefinement);

    // Tests with OR
    refinementToConvert=new LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"),
            LogicalConstraintOperator.OR,
            db_constraints).build();
    expectedRefinement=new de.fraunhofer.iais.eis.LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"))
            ._or_(infomodel_constraints)
            .build();
    convertedRefinement=logicalConstraintService.convertToInfomodel(refinementToConvert);
    assertThat(convertedRefinement).isEqualTo(expectedRefinement);


    // Tests with XONE
    refinementToConvert=new LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"),
            LogicalConstraintOperator.XONE,
            db_constraints).build();
    expectedRefinement=new de.fraunhofer.iais.eis.LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"))
            ._xone_(infomodel_constraints)
            .build();
    convertedRefinement=logicalConstraintService.convertToInfomodel(refinementToConvert);
    assertThat(convertedRefinement).isEqualTo(expectedRefinement);
  }

  @Test
  void convertToDatabaseModel() {
    Participant1 producer=testUtil.addProducer();

    de.fraunhofer.iais.eis.Constraint infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

    de.fraunhofer.iais.eis.Constraint infomodel_constraint2= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint2"))
            ._leftOperand_(LeftOperand.ENDPOINT)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("http://endpoint/01")).build();

    List<de.fraunhofer.iais.eis.Constraint> infomodel_constraints=new ArrayList<>();
    infomodel_constraints.add(infomodel_constraint1);
    infomodel_constraints.add(infomodel_constraint2);

    // Creates a List
    Constraint db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

    Constraint db_constraint2= new ConstraintBuilder()
            .leftOperand(LeftOperand.ENDPOINT)
            .constraint_ids_ID("http://constraint2")
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("http://endpoint/01")).build();

    List<Constraint> db_constraints=new ArrayList<>();
    db_constraints.add(db_constraint1);
    db_constraints.add(db_constraint2);

    AbstractConstraint refinementToConvert;
    LogicalConstraint expectedRefinement, convertedRefinement;

    // Tests with NONE -> Uses the db_constraint1 and its equivalent (infomodel_constraint1)
    expectedRefinement=new LogicalConstraintBuilder(
            URI.create("http://constraint1"),
            LogicalConstraintOperator.NONE).constraints(db_constraint1).build();
    convertedRefinement=logicalConstraintService.convertToDatabaseModel(infomodel_constraint1, producer);
    System.out.println(convertedRefinement.toJson());
    System.out.println(db_constraint1.toJson());
    assertThat(convertedRefinement).isEqualTo(expectedRefinement);

    // Tests with AND
    refinementToConvert=new de.fraunhofer.iais.eis.LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"))
            ._and_(infomodel_constraints)
            .build();
    expectedRefinement=new LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"),
            LogicalConstraintOperator.AND,
            db_constraints).build();
    convertedRefinement=logicalConstraintService.convertToDatabaseModel(refinementToConvert, producer);
    assertThat(convertedRefinement).isEqualTo(expectedRefinement);

    // Tests with OR
    refinementToConvert=new de.fraunhofer.iais.eis.LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"))
            ._or_(infomodel_constraints)
            .build();
    expectedRefinement=new LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"),
            LogicalConstraintOperator.OR,
            db_constraints).build();
    convertedRefinement=logicalConstraintService.convertToDatabaseModel(refinementToConvert, producer);
    assertThat(convertedRefinement).isEqualTo(expectedRefinement);


    // Tests with XONE
    refinementToConvert=new de.fraunhofer.iais.eis.LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"))
            ._xone_(infomodel_constraints)
            .build();
    expectedRefinement=new LogicalConstraintBuilder(
            URI.create("http://constraint1/logicalConstraint"),
            LogicalConstraintOperator.XONE,
            db_constraints).build();
    convertedRefinement=logicalConstraintService.convertToDatabaseModel(refinementToConvert, producer);
    assertThat(convertedRefinement).isEqualTo(expectedRefinement);

  }
}