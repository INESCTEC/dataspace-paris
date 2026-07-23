/* Copyright (C) [2023-2026], INESC TEC
 *
 * dataspace-paris (DataSpace Participant information Service)
 * Implements a trusted directory of participants and connectors in accordance with the International Data Space Reference Architecture.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations 
 * under the License.
 *
 * You can reach INESC TEC Technology Transfer Office (TTO) at tech-transfer@inesctec.pt, or
 * Campus da Faculdade de Engenharia da Universidade do Porto
 * Rua Dr. Roberto Frias
 * 4200-465 Porto
 * Portugal 
 */
package inesctec.inestec.paris.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.dto.PersonDto;
import inesctec.inestec.paris.util.LocalDateTimeTypeAdapter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.util.ZonedDateTimeTypeAdapter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import static inesctec.inestec.paris.config.DatabaseConstants.URI_COLUMN_LENGTH;

@Getter
@Setter
@NoArgsConstructor
@Table
@Entity
public class Person implements Serializable {
    /**
     * Serial version uid.
     **/
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long personID;

    /** Family name of a Person. */
    @Column
    private String familyName;

    /** Given name of a Person. */
    @Column
    private String givenName;

    /** Phone number of a persons. */
    @Column @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> phoneNumber;

    /** Email contact of a persons. */
    @Column @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> email;

    /* Website providing additional information about a persons. */
    @Column
    private String homepage;

    /* Person-ID */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH, unique = true)
    private URI personIdsID;


//    @ManyToOne
//    @JoinColumn(name="id_participant")
    @ManyToMany(mappedBy = "memberPerson", fetch=FetchType.EAGER)
    @JsonIgnore
    @Cascade({ org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.PERSIST})
    private Set<Participant1> participant;


    public Person(URI personIdsID) {
        this.personIdsID = personIdsID;
    }

    public Person(URI personIdsID, String familyName, String givenName, List<String> phoneNumber, List<String> email, String homepage, Set<Participant1> participant) {
        this.personIdsID = personIdsID;
        this.familyName = familyName;
        this.givenName = givenName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.homepage = homepage;
        this.participant = participant;
    }

    public void setParticipant(Set<Participant1> participant) {
        this.participant = participant;
    }

    public void setParticipant(Participant1 participant) {
        if(this.participant==null) this.participant=new HashSet<>();
        this.participant.add(participant);

        if(participant.getMemberPerson()==null) participant.setMemberPerson(new HashSet<>());
        participant.getMemberPerson().add(this);
    }

    public void update(Person updatedPerson) {
        this.personIdsID = updatedPerson.personIdsID;
        this.familyName = updatedPerson.familyName;
        this.givenName = updatedPerson.givenName;
        this.phoneNumber = updatedPerson.phoneNumber;
        this.email = updatedPerson.email;
        this.homepage = updatedPerson.homepage;
        //this.participant = updatedPerson.participant;
    }
    @Override
    public String toString() {
        return "Person{" +
            "familyName='" + familyName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", email=" + email +
                ", homepage='" + homepage + '\'' +
                ", person_ids_ID=" + personIdsID +
                '}';
    }

    public String toJson(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(this);
        return json;
    }

    public String toJson2(){
        JsonConverter jsonConverter=new JsonConverter();
        String json;
        try {
            json=jsonConverter.object2Json(this).toPrettyString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) {
            System.err.println("Person is not equal");
            return false;
        }
        /*System.out.println("PersonID: "+Objects.equals(getPersonID(), person.getPersonID()));
        System.out.println("FamilyName: "+Objects.equals(getFamilyName(), person.getFamilyName()));
        System.out.println("GivenName: "+Objects.equals(getGivenName(), person.getGivenName()));
        System.out.println("PhoneNumber: "+Objects.equals(getPhoneNumber(), person.getPhoneNumber()));
        System.out.println("Email: "+Objects.equals(getEmail(), person.getEmail()));
        System.out.println("Homepage: "+Objects.equals(getHomepage(), person.getHomepage()));
        System.out.println("PersonIdsID: "+ Objects.equals(getPersonIdsID(), person.getPersonIdsID()));*/
        return Objects.equals(getFamilyName(), person.getFamilyName())
                && Objects.equals(getGivenName(), person.getGivenName())
                && Objects.equals(getPhoneNumber(), person.getPhoneNumber())
                && Objects.equals(getEmail(), person.getEmail())
                && Objects.equals(getHomepage(), person.getHomepage())
                && Objects.equals(getPersonIdsID(), person.getPersonIdsID())
                /*&& Objects.equals(getParticipant(), person.getParticipant())*/;
    }

    public boolean equalsWithParticipant(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) {
            System.err.println("Person is not equal");
            return false;
        }
        /*System.out.println("PersonID: "+Objects.equals(getPersonID(), person.getPersonID()));
        System.out.println("FamilyName: "+Objects.equals(getFamilyName(), person.getFamilyName()));
        System.out.println("GivenName: "+Objects.equals(getGivenName(), person.getGivenName()));
        System.out.println("PhoneNumber: "+Objects.equals(getPhoneNumber(), person.getPhoneNumber()));
        System.out.println("Email: "+Objects.equals(getEmail(), person.getEmail()));
        System.out.println("Homepage: "+Objects.equals(getHomepage(), person.getHomepage()));
        System.out.println("PersonIdsID: "+ Objects.equals(getPersonIdsID(), person.getPersonIdsID()));*/
        return Objects.equals(getFamilyName(), person.getFamilyName())
                && Objects.equals(getGivenName(), person.getGivenName())
                && Objects.equals(getPhoneNumber(), person.getPhoneNumber())
                && Objects.equals(getEmail(), person.getEmail())
                && Objects.equals(getHomepage(), person.getHomepage())
                && Objects.equals(getPersonIdsID(), person.getPersonIdsID())
                && Objects.equals(getParticipant(), person.getParticipant());
    }


    @Override
    public int hashCode() {
        return Objects.hash(getPersonID(), getFamilyName(), getGivenName(), getPhoneNumber(), getEmail(), getHomepage(), getPersonIdsID()/*, getParticipant()*/);
    }

    /*public List<PersonDto> convertToPersonDto(Set<Person> persons){
        if (persons.isEmpty()){
            return null;
        }
        else {
            List<PersonDto> newPersonsSet=new ArrayList<>();
            persons.forEach(person-> {
                PersonDto newPerson=new PersonDto();
                newPerson.setPersonIdsID(person.getPersonIdsID());
                newPerson.setHomepage(person.getHomepage());
                newPerson.setEmail(person.getEmail());
                newPerson.setGivenName(person.getGivenName());
                newPerson.setFamilyName(person.getFamilyName());
                newPerson.setPhoneNumber(person.getPhoneNumber());

                newPersonsSet.add(newPerson);
            });
            return newPersonsSet;
        }
    }*/
    public PersonDto convertToPersonDto(){

        PersonDto newPerson=new PersonDto();
        newPerson.setPersonIdsID(this.getPersonIdsID());
        newPerson.setHomepage(this.getHomepage());
        newPerson.setEmail(this.getEmail());
        newPerson.setGivenName(this.getGivenName());
        newPerson.setFamilyName(this.getFamilyName());
        newPerson.setPhoneNumber(this.getPhoneNumber());
        return newPerson;
    }
}
