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
package inesctec.inestec.paris.builder;

import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.Person;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PersonBuilder {
  private String familyName;
  private String givenName;
  private List<String> phoneNumber;
  private List<String> email;
  private String homepage;
  private Set<Participant1> participant;

  private URI ids_id;

  public PersonBuilder(String ids_id) {
    this.ids_id = URI.create(ids_id);
  }
  public PersonBuilder() {
  }

  public PersonBuilder familyName(String familyName) {
    this.familyName = familyName;
    return this;
  }

  public PersonBuilder givenName(String givenName) {
    this.givenName = givenName;
    return this;
  }

  public PersonBuilder phoneNumber(List<String> phoneNumber) {
    this.phoneNumber = phoneNumber;
    return this;
  }

  public PersonBuilder email(List<String> email) {
    this.email = email;
    return this;
  }

  public PersonBuilder homepage(String homepage) {
    this.homepage = homepage;
    return this;
  }

  public PersonBuilder ids_id(String ids_id) {
    this.ids_id = URI.create(ids_id);
    return this;
  }

  public PersonBuilder participant(Participant1 participant) {
    if(this.participant==null) this.participant=new HashSet<>();
    this.participant.add(participant);
    participant.getMemberPerson().add(build());


    return this;
  }

  public PersonBuilder participant(Set<Participant1> participant) {
    this.participant = participant;
    return this;
  }


  public Person build() {
    return new Person(ids_id, familyName, givenName, phoneNumber, email, homepage, participant);
  }

  /*public Person buildBasicPerson() {
    return new Person(ids_id);
  }*/
}