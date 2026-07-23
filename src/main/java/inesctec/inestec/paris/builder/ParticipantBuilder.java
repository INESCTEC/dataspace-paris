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

import inesctec.inestec.paris.model.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticipantBuilder {
  private URI participantID;
  private List<String> legalName;
  private List<String> name;
  private List<String> description = null;
  private List<String> corporateEmailAddress;
  private URI corporateHomepage;
  //private List<URI> memberParticipant = null;
  private LogicalConstraint participantLogicalConstraint = null;
  private Set<Person> memberPerson = null;
  private ParticipantCertification participantCertification;
  private String primarySite;
  private String vatID;
  private Set<BusinessIdentifier> businessIdentifier=null;
  private String registryCourt;
  private String legalForm;
  private String version;
  private Set<Connector> connectorSet = null;

  public ParticipantBuilder(URI participantID, List<String> legalName, ParticipantCertification participantCertification)
  {
    this.participantID=participantID;
    this.legalName=legalName;
    this.participantCertification=participantCertification;
  }

  public ParticipantBuilder(URI participantID, String legalName, ParticipantCertification participantCertification)
  {
    this.participantID=participantID;
    this.legalName=new ArrayList<>();
    this.legalName.add(legalName);
    this.participantCertification=participantCertification;
  }

  public ParticipantBuilder(URI participantID)
  {
    this.participantID=participantID;
  }

  public ParticipantBuilder participantID(URI participantID) {
    this.participantID = participantID;
    return this;
  }

  public ParticipantBuilder version(String version) {
    this.version = version;
    return this;
  }
  public ParticipantBuilder legalName(List<String> legalName) {
    this.legalName = legalName;
    return this;
  }

  // Only adds the first name (Due to the App behavior)
  public ParticipantBuilder name(List<String> name) {
    this.name = name.stream().findFirst().stream().toList();
    return this;
  }

  public ParticipantBuilder legalName(String legalName) {
    if(this.legalName==null) this.legalName=new ArrayList<>();
    this.legalName.add(legalName);
    return this;
  }

  // Overrides the name. It can only have 1 name (Due to the App behavior)
  public ParticipantBuilder name(String name) {
    if(name==null) return this;
    if(this.name==null) this.name=new ArrayList<>();
    this.name.clear();
    this.name.add(name);
    return this;
  }

  public ParticipantBuilder description(List<String> description) {
    this.description = description;
    return this;
  }
  public ParticipantBuilder description(String description) {
    if(this.description==null) this.description=new ArrayList<>();
    this.description.add(description);
    return this;
  }

  public ParticipantBuilder corporateEmailAddress(List<String> corporateEmailAddress) {
    this.corporateEmailAddress = corporateEmailAddress;
    return this;
  }

  public ParticipantBuilder corporateEmailAddress(String corporateEmailAddress) {
    if(this.corporateEmailAddress==null) this.corporateEmailAddress=new ArrayList<>();
    this.corporateEmailAddress.add(corporateEmailAddress);
    return this;
  }

  public ParticipantBuilder corporateHomepage(URI corporateHomepage) {
    this.corporateHomepage = corporateHomepage;
    return this;
  }

 /* public ParticipantBuilder memberParticipant(URI memberParticipant) {
    if(this.memberParticipant==null) this.memberParticipant=new ArrayList<>();
    this.memberParticipant.add(memberParticipant);
    return this;
  }

  public ParticipantBuilder memberParticipant(List<Participant1> memberParticipant) {
    this.memberParticipant = memberParticipant.stream().map(v->v.getParticipantIdsID()).toList();
    return this;
  }

  public ParticipantBuilder memberParticipant(Participant1 memberParticipant) {
    if(this.memberParticipant==null) this.memberParticipant=new ArrayList<>();
    this.memberParticipant.add(memberParticipant.getParticipantIdsID());
    return this;
  }*/


  public ParticipantBuilder participantRefinement(LogicalConstraint participantLogicalConstraint) {
    this.participantLogicalConstraint = participantLogicalConstraint;
    return this;
  }

  public ParticipantBuilder participantCertification(ParticipantCertification participantCertification) {
    this.participantCertification = participantCertification;
    return this;
  }

  public ParticipantBuilder memberPerson(Set<Person> memberPerson) {
    this.memberPerson = memberPerson;
    return this;
  }

  public ParticipantBuilder memberPerson(Person memberPerson) {
    if(this.memberPerson==null){
      this.memberPerson=new HashSet<>();
    }
    this.memberPerson.add(memberPerson);
    return this;
  }


  public ParticipantBuilder connectorSet(Set<Connector> connectorSet) {
    this.connectorSet = connectorSet;
    return this;
  }

  public ParticipantBuilder primarySite(String primarySite) {
    this.primarySite = primarySite;
    return this;
  }

  public ParticipantBuilder vatID(String vatID) {
    this.vatID = vatID;
    return this;
  }

  public ParticipantBuilder businessIdentifier(BusinessIdentifier businessIdentifier) {
    if(this.businessIdentifier==null) this.businessIdentifier=new HashSet<>();
    this.businessIdentifier.add(businessIdentifier);
    return this;
  }

  public ParticipantBuilder businessIdentifier(Set<BusinessIdentifier> businessIdentifier) {
    this.businessIdentifier = businessIdentifier;
    return this;
  }

  public ParticipantBuilder registryCourt(String registryCourt) {
    this.registryCourt = registryCourt;
    return this;
  }

  public ParticipantBuilder legalForm(String legalForm) {
    this.legalForm = legalForm;
    return this;
  }

  public Participant1 build() {
    return new Participant1(
        participantID, legalName, name, description, corporateEmailAddress, corporateHomepage,
        /*memberParticipant,*/ participantLogicalConstraint, memberPerson, participantCertification,
        primarySite, vatID, businessIdentifier, registryCourt, legalForm, connectorSet, version);
  }
  public Participant1 buildBasic() {
    return new Participant1(
        participantID, legalName, participantCertification);
  }

}