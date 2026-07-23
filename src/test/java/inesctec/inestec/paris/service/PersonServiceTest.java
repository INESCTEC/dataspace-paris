package inesctec.inestec.paris.service;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import de.fraunhofer.iais.eis.CertificationLevel;
import de.fraunhofer.iais.eis.PersonBuilder;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.model.Person;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.TestUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonServiceTest {

  @Autowired private PersonService personService;

  @Test
  void convertToDatabaseModel() {
    TestUtil testUtil=new TestUtil();
    Person expectedPerson=testUtil.person();

    de.fraunhofer.iais.eis.Person person=new de.fraunhofer.iais.eis.PersonBuilder()
            ._familyName_("Doe")
            ._givenName_("John")
            ._emailAddress_(Util.asList("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            ._phoneNumber_(Util.asList("923452876"))
            .build();
    Person convertedPerson=personService.convertToDatabaseModel(person);

    System.out.println(expectedPerson.toJson());
    System.out.println(convertedPerson.toJson());
    assertEquals(convertedPerson.getFamilyName(), expectedPerson.getFamilyName());
    assertEquals(convertedPerson.getGivenName(), expectedPerson.getGivenName());
    assertEquals(convertedPerson.getEmail(), expectedPerson.getEmail());
    assertEquals(convertedPerson.getHomepage(), expectedPerson.getHomepage());
    assertEquals(convertedPerson.getPhoneNumber(), expectedPerson.getPhoneNumber());
  }

  @Test
  void convertToDatabaseModel_TestList() {
    TestUtil testUtil=new TestUtil();
    Set<Person> expectedPersons=testUtil.persons();

    List<de.fraunhofer.iais.eis.Person> personToConvert=new ArrayList<>();
    de.fraunhofer.iais.eis.Person person=new de.fraunhofer.iais.eis.PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._emailAddress_(Util.asList("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            ._phoneNumber_(Util.asList("923452876"))
            .build();
    de.fraunhofer.iais.eis.Person person2=new de.fraunhofer.iais.eis.PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6642"))
            ._familyName_("Doe2")
            ._givenName_("John2")
            ._emailAddress_(Util.asList("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.test.com")
            ._phoneNumber_(Util.asList("923452876"))
            .build();
    personToConvert.add(person);
    personToConvert.add(person2);

    Set<Person> convertedPerson=personService.convertToDatabaseModel(personToConvert);

    assertTrue(CompareUtil.compareSet(convertedPerson, expectedPersons));
  }

  @Test
  void convertToDatabaseModel_TestEveryPersonValue() {
    de.fraunhofer.iais.eis.Person person;

    // Person without familyName
    person=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            //._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();
    assertThat(personService.convertToDatabaseModel(person))
            .isEqualTo(new inesctec.inestec.paris.builder.PersonBuilder()
                    //.familyName("Doe")
                    .givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build());

    // Person without givenName
    person=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            //._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();
    assertThat(personService.convertToDatabaseModel(person))
            .isEqualTo(new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe")
                    //.givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build());

    // Person without phoneNumber
    person=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            //._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();
    System.out.println(personService.convertToDatabaseModel(person).toJson2());
    assertThat(personService.convertToDatabaseModel(person).toJson2())
            .isEqualTo(new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe")
                    .givenName("John")
                    .phoneNumber(new ArrayList<>())
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build().toJson2());

    // Person without email
    person=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            //._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();
    assertThat(personService.convertToDatabaseModel(person).toJson2())
            .isEqualTo(new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe")
                    .givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(new ArrayList<>())
                    .homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build().toJson2());

    // Person without homepage
    person=new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            //._homepage_("www.john-doe.com")
            .build();
    assertThat(personService.convertToDatabaseModel(person))
            .isEqualTo(new inesctec.inestec.paris.builder.PersonBuilder()
                    .familyName("Doe")
                    .givenName("John")
                    .phoneNumber(List.of("923452876"))
                    .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    //.homepage("www.john-doe.com")
                    .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                    .build());

    // Person without ids_id
    person=new PersonBuilder()
            ._familyName_("Doe")
            ._givenName_("John")
            ._phoneNumber_(List.of("923452876"))
            ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            ._homepage_("www.john-doe.com")
            .build();
    Person convertedPerson=personService.convertToDatabaseModel(person);
    assertTrue(Objects.equals(convertedPerson.getFamilyName(), "Doe"));
    assertTrue(Objects.equals(convertedPerson.getGivenName(), "John"));
    assertTrue(Objects.equals(convertedPerson.getPhoneNumber(), List.of("923452876")));
    assertTrue(Objects.equals(convertedPerson.getEmail(), List.of("john.doe@gmail.com", "john.doe@hotmail.com")));
    assertTrue(Objects.equals(convertedPerson.getHomepage(), "www.john-doe.com"));

  }

  @Test
  void convertToInfomodel_TestEveryPersonValue() {
    // Tests complete Person
    Person person=new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build();

    de.fraunhofer.iais.eis.Person convertedPerson=personService.convertToInfomodel(person);
    assertTrue(Objects.equals(convertedPerson.getId(), URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")));
    assertTrue(Objects.equals(convertedPerson.getFamilyName(), "Doe"));
    assertTrue(Objects.equals(convertedPerson.getGivenName(), "John"));
    assertTrue(Objects.equals(convertedPerson.getPhoneNumber(), List.of("923452876")));
    assertTrue(Objects.equals(convertedPerson.getEmailAddress(), List.of("john.doe@gmail.com", "john.doe@hotmail.com")));
    assertTrue(Objects.equals(convertedPerson.getHomepage(), "www.john-doe.com"));


    // Person without familyName
    person=new inesctec.inestec.paris.builder.PersonBuilder()
            //.familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build();
    assertThat(personService.convertToInfomodel(person))
            .isEqualTo(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
                    //._familyName_("Doe")
                    ._givenName_("John")
                    ._phoneNumber_(List.of("923452876"))
                    ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    ._homepage_("www.john-doe.com")
                    .build());

    // Person without givenName
    person=new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            //.givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build();
    assertThat(personService.convertToInfomodel(person))
            .isEqualTo(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
                    ._familyName_("Doe")
                    //._givenName_("John")
                    ._phoneNumber_(List.of("923452876"))
                    ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    ._homepage_("www.john-doe.com")
                    .build());

    // Person without phoneNumber
    person=new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            //.phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build();
    assertThat(personService.convertToInfomodel(person).toRdf())
            .isEqualTo(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
                    ._familyName_("Doe")
                    ._givenName_("John")
                    //._phoneNumber_(List.of("923452876"))
                    ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    ._homepage_("www.john-doe.com")
                    .build().toRdf());

    // Person without email
    person=new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            //.email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build();
    assertThat(personService.convertToInfomodel(person).toRdf())
            .isEqualTo(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
                    ._familyName_("Doe")
                    ._givenName_("John")
                    ._phoneNumber_(List.of("923452876"))
                    //._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    ._homepage_("www.john-doe.com")
                    .build().toRdf());

    // Person without homepage
    person=new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            //.homepage("www.john-doe.com")
            .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build();
    assertThat(personService.convertToInfomodel(person))
            .isEqualTo(new PersonBuilder(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"))
                    ._familyName_("Doe")
                    ._givenName_("John")
                    ._phoneNumber_(List.of("923452876"))
                    ._emailAddress_(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
                    //._homepage_("www.john-doe.com")
                    .build());

    // Person without ids_id
    person=new inesctec.inestec.paris.builder.PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(List.of("923452876"))
            .email(List.of("john.doe@gmail.com", "john.doe@hotmail.com"))
            .homepage("www.john-doe.com")
            //.ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
            .build();
    convertedPerson=personService.convertToInfomodel(person);
    assertTrue(Objects.equals(convertedPerson.getFamilyName(), "Doe"));
    assertTrue(Objects.equals(convertedPerson.getGivenName(), "John"));
    assertTrue(Objects.equals(convertedPerson.getPhoneNumber(), List.of("923452876")));
    assertTrue(Objects.equals(convertedPerson.getEmailAddress(), List.of("john.doe@gmail.com", "john.doe@hotmail.com")));
    assertTrue(Objects.equals(convertedPerson.getHomepage(), "www.john-doe.com"));

  }
}