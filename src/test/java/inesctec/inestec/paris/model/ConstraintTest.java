package inesctec.inestec.paris.model;

import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.LeftOperand;
import inesctec.inestec.paris.builder.ConstraintBuilder;
import inesctec.inestec.paris.dto.ConstraintDto;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintTest {


  @Test
  void convertToConstraintDto_WithList() {
    // Creates a list of Constraints
    List<Constraint> constraintList=new ArrayList<>();
    Constraint constraint=new ConstraintBuilder()
            .constraint_ids_ID("http://inesctec/constraint/id/01")
            .unit("http://dbpedia.org/resource/Euro")
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK").build();
    Constraint constraint2=new ConstraintBuilder()
            .constraint_ids_ID("http://inesctec/constraint/id/02")
            .unit("http://dbpedia.org/resource/Euro")
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK").build();
    constraintList.add(constraint);
    constraintList.add(constraint2);

    // Expected Result
    List<ConstraintDto> expectedResult=new ArrayList<>();
    expectedResult.add(new ConstraintDto(URI.create("http://inesctec/constraint/id/01"), LeftOperand.SECURITY_LEVEL, String.valueOf(BinaryOperator.EQUALS),
            "PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK", null, URI.create("http://dbpedia.org/resource/Euro"), null));
    expectedResult.add(new ConstraintDto(URI.create("http://inesctec/constraint/id/02"), LeftOperand.SECURITY_LEVEL, String.valueOf(BinaryOperator.EQUALS),
            "PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK", null, URI.create("http://dbpedia.org/resource/Euro"), null));

    List<ConstraintDto> constraintResult=constraintList.stream().map(Constraint::convertToConstraintDto).toList();

    assertEquals(constraintResult.toString(), expectedResult.toString());
  }

  @Test
  void convertToConstraintDto_WithoutList() {
    // Expected Result
    ConstraintDto constraintResult=new ConstraintDto(URI.create("http://inesctec/constraint/id/01"), LeftOperand.SECURITY_LEVEL, String.valueOf(BinaryOperator.EQUALS),
            "PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK", null, URI.create("http://dbpedia.org/resource/Euro"), null);

    Constraint constraint=new ConstraintBuilder()
            .constraint_ids_ID("http://inesctec/constraint/id/01")
            .unit("http://dbpedia.org/resource/Euro")
            .leftOperand(LeftOperand.SECURITY_LEVEL)
            .operator(String.valueOf(BinaryOperator.EQUALS))
            .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK").build();

    assertEquals(constraint.convertToConstraintDto().toString(), constraintResult.toString());
  }
}