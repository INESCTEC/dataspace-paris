package inesctec.inestec.paris.model;

import inesctec.inestec.paris.dto.BusinessIdentifierDto;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BusinessIdentifierTest {


  @Test
  void convertToBusinessIdentifierDto() {
    // Creates a new Business Identifier
    BusinessIdentifier businessIdentifier=new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            .identifierSystem("businessIdentifierSystem")
            .identifierNumber("businessIdentifierNumber")
            .build();

    // Creates the Result Business Identifier -> Expected Result
    BusinessIdentifierDto businessIdentifierDto=new BusinessIdentifierDto();
    businessIdentifierDto.setBusinessIdentifierIdsID(URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"));
    businessIdentifierDto.setIdentifierNumber("businessIdentifierNumber");
    businessIdentifierDto.setIdentifierSystem("businessIdentifierSystem");

    assertEquals(businessIdentifier.convertToBusinessIdentifierDto().toString(), businessIdentifierDto.toString());
  }

  @Test
  void toJson() {
    String expectedResult="{\n" +
            "  \"businessIdentifierIdsID\" : \"https://texpact.pt/ids/businessIdentifier/id/producer-01\",\n" +
            "  \"identifierSystem\" : \"businessIdentifierSystem\",\n" +
            "  \"identifierNumber\" : \"businessIdentifierNumber\"\n" +
            "}";
    // Creates a new Business Identifier
    BusinessIdentifier businessIdentifier=new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            .identifierSystem("businessIdentifierSystem")
            .identifierNumber("businessIdentifierNumber")
            .build();

    System.out.println(expectedResult);
    System.out.println(businessIdentifier.toJson());

    assertEquals(businessIdentifier.toJson(), expectedResult.replaceAll("\n", System.lineSeparator()));
  }

  @Test
  void testEquals() {
    BusinessIdentifier businessIdentifier=new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            .identifierSystem("businessIdentifierSystem")
            .identifierNumber("businessIdentifierNumber")
            .build();
    BusinessIdentifier expectedBusinessIdentifier=new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            .identifierSystem("businessIdentifierSystem")
            .identifierNumber("businessIdentifierNumber")
            .build();

    // BusinessIdentifier should be equal
    assertTrue(businessIdentifier.equals(expectedBusinessIdentifier));
    assertEquals(businessIdentifier, expectedBusinessIdentifier);

    // Update the businessIdentifier. Now they should be different
    TestUtil testUtil=new TestUtil();
    Participant1 producer=testUtil.addProducer();
    businessIdentifier.setParticipant(producer);


    // Make the expectedBusinessIdentifier equal to the businessIdentifier
    producer.setBusinessIdentifier(Set.of(expectedBusinessIdentifier));
    expectedBusinessIdentifier.setParticipant(producer);
    // BusinessIdentifier should be equal
    assertTrue(businessIdentifier.equals(expectedBusinessIdentifier));
    assertEquals(businessIdentifier, expectedBusinessIdentifier);
  }
}