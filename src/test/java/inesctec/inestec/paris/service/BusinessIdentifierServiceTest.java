package inesctec.inestec.paris.service;

import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.model.BusinessIdentifier;
import inesctec.inestec.paris.model.Person;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BusinessIdentifierServiceTest {

  @Autowired
  private BusinessIdentifierService businessIdentifierService;

  @Test
  void convertToDatabaseModel_List() {
    TestUtil testUtil=new TestUtil();
    Set<BusinessIdentifier> expectedBusinessIdentifierSet=new HashSet<>();
    expectedBusinessIdentifierSet.add(testUtil.businessIdentifier_Producer());
    expectedBusinessIdentifierSet.add(testUtil.businessIdentifier_Subcontracted());

    List<de.fraunhofer.iais.eis.BusinessIdentifier> businessIdentifiersToConvert=new ArrayList<>();
    de.fraunhofer.iais.eis.BusinessIdentifier businessIdentifierProducer=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();
    de.fraunhofer.iais.eis.BusinessIdentifier businessIdentifierSubcontracted=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/subcontracted-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();
    businessIdentifiersToConvert.add(businessIdentifierProducer);
    businessIdentifiersToConvert.add(businessIdentifierSubcontracted);

    Set<BusinessIdentifier> convertedBusinessIdentifiers=businessIdentifierService.convertToDatabaseModel(businessIdentifiersToConvert);

    assertTrue(CompareUtil.compareSet(convertedBusinessIdentifiers, expectedBusinessIdentifierSet));
  }

  @Test
  void convertToDatabaseModel() {
    TestUtil testUtil=new TestUtil();
    BusinessIdentifier expectedBusinessIdentifier=testUtil.businessIdentifier_Producer();

    de.fraunhofer.iais.eis.BusinessIdentifier businessIdentifierProducer=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();


    BusinessIdentifier convertedBusinessIdentifiers=businessIdentifierService.convertToDatabaseModel(businessIdentifierProducer);

    assertTrue(Objects.equals(convertedBusinessIdentifiers, expectedBusinessIdentifier));
  }

  void convertToDatabaseModel_TestEveryValue() {
    // Complete Business Identifier
    BusinessIdentifier expectedBusinessIdentifier=new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            .identifierSystem("businessIdentifierSystem")
            .identifierNumber("businessIdentifierNumber")
            .build();;

    de.fraunhofer.iais.eis.BusinessIdentifier businessIdentifierProducer=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();

    BusinessIdentifier convertedBusinessIdentifiers=businessIdentifierService.convertToDatabaseModel(businessIdentifierProducer);

    assertTrue(Objects.equals(convertedBusinessIdentifiers, expectedBusinessIdentifier));

    // Business Identifier without ids_ID
    expectedBusinessIdentifier=new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            //.ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            .identifierSystem("businessIdentifierSystem")
            .identifierNumber("businessIdentifierNumber")
            .build();;

    businessIdentifierProducer=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            /*URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01")*/)
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();

    convertedBusinessIdentifiers=businessIdentifierService.convertToDatabaseModel(businessIdentifierProducer);

    assertTrue(Objects.equals(convertedBusinessIdentifiers, expectedBusinessIdentifier));

    // Business Identifier without identifierSystem
    expectedBusinessIdentifier=new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            //.identifierSystem("businessIdentifierSystem")
            .identifierNumber("businessIdentifierNumber")
            .build();;

    businessIdentifierProducer=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
            //._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();

    convertedBusinessIdentifiers=businessIdentifierService.convertToDatabaseModel(businessIdentifierProducer);

    assertTrue(Objects.equals(convertedBusinessIdentifiers, expectedBusinessIdentifier));

    // Business Identifier without identifierNumber
    expectedBusinessIdentifier=new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
            .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
            .identifierSystem("businessIdentifierSystem")
            //.identifierNumber("businessIdentifierNumber")
            .build();;

    businessIdentifierProducer=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
            ._identifierSystem_("businessIdentifierSystem")
            //._identifierNumber_("businessIdentifierNumber")
            .build();

    convertedBusinessIdentifiers=businessIdentifierService.convertToDatabaseModel(businessIdentifierProducer);

    assertTrue(Objects.equals(convertedBusinessIdentifiers, expectedBusinessIdentifier));
  }


  @Test
  void convertToInfomodel() {
    TestUtil testUtil=new TestUtil();
    BusinessIdentifier businessIdentifierToConvert=testUtil.businessIdentifier_Producer();

    de.fraunhofer.iais.eis.BusinessIdentifier expectedBusinessIdentifier=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();


    de.fraunhofer.iais.eis.BusinessIdentifier convertedBusinessIdentifiers=businessIdentifierService.convertToInfomodel(businessIdentifierToConvert);

    assertTrue(Objects.equals(convertedBusinessIdentifiers, expectedBusinessIdentifier));
  }

  @Test
  void testConvertDBIntoInfoModel() {
    TestUtil testUtil=new TestUtil();
    Set<BusinessIdentifier> businessIdentifierToConvert=new HashSet<>();
    businessIdentifierToConvert.add(testUtil.businessIdentifier_Producer());
    businessIdentifierToConvert.add(testUtil.businessIdentifier_Subcontracted());

    List<de.fraunhofer.iais.eis.BusinessIdentifier> expectedBusinessIdentifier=new ArrayList<>();
    de.fraunhofer.iais.eis.BusinessIdentifier expectedBusinessIdentifierProducer=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/producer-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();
    de.fraunhofer.iais.eis.BusinessIdentifier expectedBusinessIdentifierSubcontracted=new de.fraunhofer.iais.eis.BusinessIdentifierBuilder(
            URI.create("https://texpact.pt/ids/businessIdentifier/id/subcontracted-01"))
            ._identifierSystem_("businessIdentifierSystem")
            ._identifierNumber_("businessIdentifierNumber")
            .build();
    expectedBusinessIdentifier.add(expectedBusinessIdentifierProducer);
    expectedBusinessIdentifier.add(expectedBusinessIdentifierSubcontracted);



    List<de.fraunhofer.iais.eis.BusinessIdentifier> convertedBusinessIdentifiers=businessIdentifierService.convertToInfomodel(businessIdentifierToConvert);

    assertTrue(CompareUtil.compareListsEmptyAndNull(convertedBusinessIdentifiers, expectedBusinessIdentifier));
  }
}