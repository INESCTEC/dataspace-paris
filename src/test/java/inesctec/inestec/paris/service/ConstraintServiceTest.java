package inesctec.inestec.paris.service;

import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.util.RdfResource;
import inesctec.inestec.paris.builder.ConstraintBuilder;
import inesctec.inestec.paris.converter.BinaryOperatorConverter;
import inesctec.inestec.paris.model.Constraint;
import inesctec.inestec.paris.util.CompareUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConstraintServiceTest {

  @Autowired
  private ConstraintService constraintService;

  @Test
  void convertDBIntoInfoModel() {
    de.fraunhofer.iais.eis.Constraint infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

    de.fraunhofer.iais.eis.Constraint infomodel_constraint2= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint2"))
            ._leftOperand_(LeftOperand.ENDPOINT)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("http://endpoint/01")).build();

    List<de.fraunhofer.iais.eis.Constraint> expected_infomodel_constraints=new ArrayList<>();
    expected_infomodel_constraints.add(infomodel_constraint1);
    expected_infomodel_constraints.add(infomodel_constraint2);

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

    List<de.fraunhofer.iais.eis.Constraint> resultList=constraintService.convertToInfomodel(db_constraints);
    assertTrue(CompareUtil.compareConstraints(resultList, expected_infomodel_constraints));
  }

  /**
   * Tests the convertDBIntoInfoModel(Constraint) method with all possible null values:
   *  leftOperand and operator cannot be null
   * */
  @Test
  void convertDBIntoInfoModel_CheckAllValues() {
    BinaryOperatorConverter binaryOperatorConverter=new BinaryOperatorConverter();

    de.fraunhofer.iais.eis.Constraint infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();

    Constraint db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();


    // Test Complete Constraint
    de.fraunhofer.iais.eis.Constraint convertedConstraint=constraintService.convertToInfomodel(db_constraint1);
    assertThat(convertedConstraint).isEqualTo(infomodel_constraint1);


    // Test without unit
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            //._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            //.unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToInfomodel(db_constraint1);
    assertThat(convertedConstraint).isEqualTo(infomodel_constraint1);


    // Test without rightOperand
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            //._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            //.rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToInfomodel(db_constraint1);
    assertThat(convertedConstraint).isEqualTo(infomodel_constraint1);


    // Test without rightOperandReference
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            //._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            //.rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToInfomodel(db_constraint1);
    assertThat(convertedConstraint).isEqualTo(infomodel_constraint1);

    // Test without pipEndpoint
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            //._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            //.pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToInfomodel(db_constraint1);
    assertThat(convertedConstraint).isEqualTo(infomodel_constraint1);

    // Test without ID
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder()
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            //.constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToInfomodel(db_constraint1);
    assertThat(convertedConstraint.getUnit()).isEqualTo(infomodel_constraint1.getUnit());
    assertThat(convertedConstraint.getLeftOperand()).isEqualTo(infomodel_constraint1.getLeftOperand());
    assertThat(convertedConstraint.getOperator()).isEqualTo(infomodel_constraint1.getOperator());
    assertThat(convertedConstraint.getRightOperand()).isEqualTo(infomodel_constraint1.getRightOperand());
    assertThat(convertedConstraint.getRightOperandReference()).isEqualTo(infomodel_constraint1.getRightOperandReference());
    assertThat(convertedConstraint.getPipEndpoint()).isEqualTo(infomodel_constraint1.getPipEndpoint());

  }

  /**
   * Tests the convertToDatabaseModel(Constraint) method with all possible null values:
   *  leftOperand and operator cannot be null
   * */
  @Test
  void convertToDatabaseModel_CheckAllValues() {
    BinaryOperatorConverter binaryOperatorConverter=new BinaryOperatorConverter();

    de.fraunhofer.iais.eis.Constraint infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();

    Constraint db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();


    // Test Complete Constraint
    Constraint convertedConstraint=constraintService.convertToDatabaseModel(infomodel_constraint1);
    assertThat(convertedConstraint).isEqualTo(db_constraint1);


    // Test without unit
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            //._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            //.unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToDatabaseModel(infomodel_constraint1);
    assertThat(convertedConstraint).isEqualTo(db_constraint1);


    // Test without rightOperand
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            //._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            //.rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToDatabaseModel(infomodel_constraint1);
    assertThat(convertedConstraint).isEqualTo(db_constraint1);

    // Test without rightOperandReference
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            //._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            //.rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToDatabaseModel(infomodel_constraint1);
    assertThat(convertedConstraint).isEqualTo(db_constraint1);

    // Test without pipEndpoint
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder(URI.create("http://constraint1"))
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            //._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            .constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            //.pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToDatabaseModel(infomodel_constraint1);
    assertThat(convertedConstraint).isEqualTo(db_constraint1);

    // Test without ID
    infomodel_constraint1= new de.fraunhofer.iais.eis.ConstraintBuilder()
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            ._rightOperandReference_(URI.create("https://right/operand/reference"))
            ._pipEndpoint_(URI.create("https://pip/endpoint"))
            .build();
    db_constraint1= new ConstraintBuilder()
            //.constraint_ids_ID("http://constraint1")
            .unit(URI.create("http://dbpedia.org/resource/Euro"))
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK"))
            .rightOperandReference(URI.create("https://right/operand/reference"))
            .pipEndpoint(URI.create("https://pip/endpoint"))
            .build();
    convertedConstraint=constraintService.convertToDatabaseModel(infomodel_constraint1);
    //assertThat(convertedConstraint).isEqualTo(db_constraint1);
    assertThat(convertedConstraint.getUnit()).isEqualTo(db_constraint1.getUnit());
    assertThat(convertedConstraint.getLeftOperand()).isEqualTo(db_constraint1.getLeftOperand());
    assertThat(convertedConstraint.getOperator()).isEqualTo(db_constraint1.getOperator());
    assertThat(convertedConstraint.getRightOperand()).isEqualTo(db_constraint1.getRightOperand());
    assertThat(convertedConstraint.getRightOperandReference()).isEqualTo(db_constraint1.getRightOperandReference());
    assertThat(convertedConstraint.getPipEndpoint()).isEqualTo(db_constraint1.getPipEndpoint());

  }

  @Test
  void convertToDatabaseModel() {
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

    List<Constraint> expected_db_constraints=new ArrayList<>();
    expected_db_constraints.add(db_constraint1);
    expected_db_constraints.add(db_constraint2);

    List<Constraint> resultList=constraintService.convertToDatabaseModel(infomodel_constraints);
    assertTrue(ObjectUtils.equals(resultList, expected_db_constraints));
  }

}