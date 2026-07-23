package inesctec.inestec.paris.util;

import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.util.RdfResource;
import inesctec.inestec.paris.converter.BinaryOperatorConverter;
import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import inesctec.inestec.paris.model.LogicalConstraint;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BinaryOperatorConverterTest {

  BinaryOperatorConverter binaryOperatorConverter;

  @BeforeEach
  void setUp() {
    binaryOperatorConverter=new BinaryOperatorConverter();
  }

  @Test
  void convert() {

    assertEquals(binaryOperatorConverter.convert("EQUALS"), BinaryOperator.valueOf("EQUALS"));
    assertEquals(binaryOperatorConverter.convert("EQ"), BinaryOperator.valueOf("EQ"));
    assertEquals(binaryOperatorConverter.convert("AFTER"), BinaryOperator.valueOf("AFTER"));
    assertEquals(binaryOperatorConverter.convert("BEFORE"), BinaryOperator.valueOf("BEFORE"));
    assertEquals(binaryOperatorConverter.convert("CONTAINS"), BinaryOperator.valueOf("CONTAINS"));
    assertEquals(binaryOperatorConverter.convert("COVERED_BY"), BinaryOperator.valueOf("COVERED_BY"));
    assertEquals(binaryOperatorConverter.convert("STARTS"), BinaryOperator.valueOf("STARTS"));
    assertNull(binaryOperatorConverter.convert("test"));
  }

  @Test
  void convert2() {

    //System.out.println(binaryOperatorConverter.convert2(String.valueOf(BinaryOperator.EQUALS)));
    assertEquals(binaryOperatorConverter.convert2(String.valueOf(BinaryOperator.EQUALS)), "EQUALS");
    assertEquals(binaryOperatorConverter.convert2(String.valueOf(BinaryOperator.EQ)), "EQ");
    assertEquals(binaryOperatorConverter.convert2(String.valueOf(BinaryOperator.AFTER)), "AFTER");
    assertEquals(binaryOperatorConverter.convert2(String.valueOf(BinaryOperator.BEFORE)), "BEFORE");
    assertEquals(binaryOperatorConverter.convert2(String.valueOf(BinaryOperator.CONTAINS)), "CONTAINS");
    assertEquals(binaryOperatorConverter.convert2(String.valueOf(BinaryOperator.COVERED_BY)), "COVERED_BY");
    assertEquals(binaryOperatorConverter.convert2(String.valueOf(BinaryOperator.STARTS)), "STARTS");
  }
}