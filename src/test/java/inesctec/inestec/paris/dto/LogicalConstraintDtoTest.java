package inesctec.inestec.paris.dto;

import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
/*@TestPropertySource(
        locations = "classpath:application-test.properties")*/
class LogicalConstraintDtoTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogicalConstraintDtoTest.class);

  /**
   * Tests all possible outcomes of this method:
   *    - Add a Constraint when the Operator is NONE and the Constraint List is empty
   *    - Throw an error when the Operator is NONE and the Constraint List is not empty (The NONE cannot be a list)
   *    - Add a Constraint when the Operator is OR
   *    - Add a Constraint when the Operator is AND
   *    - Add a Constraint when the Operator is XONE
   * */
  @Test
  void addConstraint() {
    LOGGER.info("Creating Constraint");
    ConstraintDto constraintDto=new ConstraintDto();
    List<ConstraintDto> constraintDtoList=new ArrayList<>();
    //constraintDtoList.add(constraintDto);

    // Creates the Logical Constraint
    LOGGER.info("Creating Logical Constraint Without Constraint");
    LogicalConstraintDto logicalConstraintDto=new LogicalConstraintDto();
    logicalConstraintDto.setIdsID(URI.create("https://test"));
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.NONE);

     /**
      * Tests when the Operator is NONE and the Constraint List is empty
      * Should return a list
      * */
    LOGGER.info("Assert that cannot add Constraint when the Operator is NONE and the list is empty...");
    logicalConstraintDto.addConstraint(constraintDto);
    logicalConstraintDto.getConstraints().forEach(System.out::println);
    LOGGER.info("... Assert that the List now has the new Constraint");
    // Add to the list to compare the lists
    constraintDtoList.add(constraintDto);
    assertEquals(logicalConstraintDto.getConstraints(), constraintDtoList);
    LOGGER.info("...Test passed");

    /**
     * Tests when the Operator is NONE and the Constraint List is not empty
     * Should throw an error, because this operator (NONE) can only have one Constraint
     * */
    LOGGER.info("Assert that cannot add Constraint when the Operator is NONE and the list is not empty...");
    Exception exception=assertThrows(RuntimeException.class, () ->
            logicalConstraintDto.addConstraint(constraintDto));
    assertEquals(exception.getMessage(), "Participant Refinement shouldn't be a list of Constraints");
    LOGGER.info("...Test passed");

    /**
     * Tests when the Operator is OR
     * Should increment the new Constraint to the existing Constraint List
     * */
    LOGGER.info("Updating Operator to OR ...");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.OR);
    LOGGER.info("... Add new Constraint to the Logical Constraint");
    logicalConstraintDto.addConstraint(constraintDto);
    //System.out.println();
    logicalConstraintDto.getConstraints().forEach(System.out::println);
    LOGGER.info("... Assert that the List now has the new Constraint");
    // Add to the list to compare the lists
    constraintDtoList.add(constraintDto);
    assertEquals(logicalConstraintDto.getConstraints(), constraintDtoList);
    LOGGER.info("... Test passed.");

    /**
     * Tests when the Operator is AND
     * Should increment the new Constraint to the existing Constraint List
     * */
    LOGGER.info("Updating Operator to AND ...");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.AND);
    LOGGER.info("... Add new Constraint to the Logical Constraint");
    logicalConstraintDto.addConstraint(constraintDto);
    //System.out.println();
    logicalConstraintDto.getConstraints().forEach(System.out::println);
    LOGGER.info("... Assert that the List now has the new Constraint");
    // Add to the list to compare the lists
    constraintDtoList.add(constraintDto);
    assertEquals(logicalConstraintDto.getConstraints(), constraintDtoList);
    LOGGER.info("... Test passed.");

    /**
     * Tests when the Operator is XONE
     * Should increment the new Constraint to the existing Constraint List
     * */
    LOGGER.info("Updating Operator to XONE ...");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.XONE);
    LOGGER.info("... Add new Constraint to the Logical Constraint");
    logicalConstraintDto.addConstraint(constraintDto);
    //System.out.println();
    logicalConstraintDto.getConstraints().forEach(System.out::println);
    LOGGER.info("... Assert that the List now has the new Constraint");
    // Add to the list to compare the lists
    constraintDtoList.add(constraintDto);
    assertEquals(logicalConstraintDto.getConstraints(), constraintDtoList);
    LOGGER.info("... Test passed.");
  }

  /**
   * Tests all possible outcomes of this method:
   *    - Throw an error when adding a List of Constraints when the Operator is NONE
   *    - Add a List of Constraints when the Operator is OR
   *    - Add a List of Constraints when the Operator is AND
   *    - Add a List of Constraints when the Operator is XONE
   * */
  @Test
  void addConstraintList() {
    ConstraintDto constraintDto=new ConstraintDto();
    List<ConstraintDto> constraintDtoList=new ArrayList<>();

    // Creates the Logical Constraint
    LOGGER.info("Creating Logical Constraint Without Constraint");
    LOGGER.info("");
    LogicalConstraintDto logicalConstraintDto=new LogicalConstraintDto();
    logicalConstraintDto.setIdsID(URI.create("https://test"));
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.NONE);

    /**
     * Tests when the Operator is NONE
     * Should throw an error because when it is NONE it can only have one value
     * */
    LOGGER.info("Assert that cannot add Constraint when the Operator is NONE...");
    Exception exception=assertThrows(RuntimeException.class, () ->
            logicalConstraintDto.addConstraint(constraintDtoList));
    assertEquals(exception.getMessage(), "Participant Refinement shouldn't be a list of Constraints");
    LOGGER.info("...Test passed.\n");


    /**
     * Tests when the Operator is OR
     * Should increment the new Constraint List to the existing Constraint List
     * */
    LOGGER.info("Updating Operator to OR ...");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.OR);
    LOGGER.info("... Add new Constraint to the Logical Constraint");
    // Add a constraint to the list
    constraintDtoList.add(constraintDto);
    logicalConstraintDto.addConstraint(constraintDtoList);
    //System.out.println();
    logicalConstraintDto.getConstraints().forEach(System.out::println);
    LOGGER.info("... Assert that the List now has the new Constraint");
    assertEquals(logicalConstraintDto.getConstraints(), constraintDtoList);
    LOGGER.info("... Test passed.\n");

    /**
     * Tests when the Operator is AND
     * Should increment the new Constraint List to the existing Constraint List
     * */
    LOGGER.info("Updating Operator to AND ...");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.AND);
    LOGGER.info("... Add new Constraint to the Logical Constraint");
    // Add a constraint to the list - To compare
    constraintDtoList.add(constraintDto);
    logicalConstraintDto.addConstraint(List.of(constraintDto));
    LOGGER.info("... Assert that the List now has the new Constraint");
    assertEquals(logicalConstraintDto.getConstraints(), constraintDtoList);
    LOGGER.info("... Test passed.\n");

    /**
     * Tests when the Operator is XONE
     * Should increment the new Constraint List to the existing Constraint List
     * */
    LOGGER.info("Updating Operator to XONE ...");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.XONE);
    LOGGER.info("... Add new Constraint to the Logical Constraint");
    // Add a constraint to the list - To compare
    constraintDtoList.add(constraintDto);
    logicalConstraintDto.addConstraint(List.of(constraintDto));
    LOGGER.info("... Assert that the List now has the new Constraint");
    assertEquals(logicalConstraintDto.getConstraints(), constraintDtoList);
    LOGGER.info("... Test passed.");
  }

  @Test
  void validate() {
    LOGGER.info("Creating Logical Constraint with NONE Operator");
    LogicalConstraintDto logicalConstraintDto=new LogicalConstraintDto();
    logicalConstraintDto.setIdsID(URI.create("https://test"));
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.NONE);

    LOGGER.info("Assert that method returns false");
    assertFalse(logicalConstraintDto.validate(LogicalConstraintOperator.NONE));

    LOGGER.info("Updating Operator to OR");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.OR);
    LOGGER.info("Assert that method returns true");
    assertTrue(logicalConstraintDto.validate(LogicalConstraintOperator.OR));

    LOGGER.info("Updating Operator to AND");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.AND);
    LOGGER.info("Assert that method returns true");
    assertTrue(logicalConstraintDto.validate(LogicalConstraintOperator.AND));

    LOGGER.info("Updating Operator to XONE");
    logicalConstraintDto.setLogicalOperator(LogicalConstraintOperator.XONE);
    LOGGER.info("Assert that method returns true");
    assertTrue(logicalConstraintDto.validate(LogicalConstraintOperator.XONE));
  }
}