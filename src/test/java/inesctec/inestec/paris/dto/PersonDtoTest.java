package inesctec.inestec.paris.dto;

import com.google.common.collect.Sets;
import inesctec.inestec.paris.builder.PersonBuilder;
import inesctec.inestec.paris.model.Person;
import inesctec.inestec.paris.util.TestUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersonDtoTest {

  @Test
  void convertToPersonDto() {
    // Creates the Result Set
    List<String> phoneNumbers=new ArrayList<>();
    phoneNumbers.add("923452876");

    List<String> emails=new ArrayList<>();
    emails.add("john.doe@gmail.com");
    emails.add("john.doe@hotmail.com");

    List<PersonDto> expectedResult=new ArrayList<>();

    PersonDto person2=new PersonDto();
    person2.setPersonIdsID(URI.create("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652"));
    person2.setHomepage("www.john-doe.com");
    person2.setEmail(emails);
    person2.setGivenName("John");
    person2.setFamilyName("Doe");
    person2.setPhoneNumber(phoneNumbers);
    expectedResult.add(person2);


    expectedResult.forEach(System.out::println);
    // Creates a Set of Persons
    Person person=new PersonBuilder()
        .familyName("Doe")
        .givenName("John")
        .phoneNumber(phoneNumbers)
        .email(emails)
        .homepage("www.john-doe.com")
        .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
        .build();

    List<PersonDto> result=List.of(person.convertToPersonDto());
    result.forEach(System.out::println);

    //org.hamcrest.MatcherAssert.assertThat(result, Matchers.containsInAnyOrder(expectedResult.toArray()));
    //assertThat(result).hasSameElementsAs(expectedResult);
    //assertTrue(CollectionUtils.isEqualCollection(result, expectedResult));
    //assertTrue(result.containsAll(expectedResult));
    assertEquals(result.toString(), expectedResult.toString());

  }
}