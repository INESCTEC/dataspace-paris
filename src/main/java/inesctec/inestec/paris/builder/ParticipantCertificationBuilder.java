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

import de.fraunhofer.iais.eis.CertificationLevel;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.converter.XMLGregorianCalendarConverter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.time.ZonedDateTime;

public class ParticipantCertificationBuilder {
  private CertificationLevel certificationLevel;
  private ZonedDateTime lastValidDate;
  private EvaluationFacility evaluationFacility;
  private Long participantCertificationID;
  private ZonedDateTime membershipEnd;
  private Participant1 participant;
  private String title;
  private String description;
  private String version;

  private URI ids_id;

  public ParticipantCertificationBuilder(String ids_id) {
    this.ids_id = URI.create(ids_id);
  }

  public ParticipantCertificationBuilder() {
  }

  public ParticipantCertificationBuilder certificationLevel(CertificationLevel certificationLevel) {
    this.certificationLevel = certificationLevel;
    return this;
  }

  public ParticipantCertificationBuilder lastValidDate(ZonedDateTime lastValidDate) {
    this.lastValidDate = lastValidDate;
    return this;
  }

  public ParticipantCertificationBuilder lastValidDate(XMLGregorianCalendar lastValidDate) {
    XMLGregorianCalendarConverter dateConverter=new XMLGregorianCalendarConverter();
    this.lastValidDate = dateConverter.convertToZonedDateTime(lastValidDate);
    return this;
  }

  public ParticipantCertificationBuilder evaluationFacility(EvaluationFacility evaluationFacility) {
    this.evaluationFacility = evaluationFacility;
    return this;
  }

  public ParticipantCertificationBuilder participantCertificationID(Long participantCertificationID) {
    this.participantCertificationID = participantCertificationID;
    return this;
  }

  public ParticipantCertificationBuilder membershipEnd(ZonedDateTime membershipEnd) {
    this.membershipEnd = membershipEnd;
    return this;
  }
  public ParticipantCertificationBuilder membershipEnd(XMLGregorianCalendar membershipEnd) {
    XMLGregorianCalendarConverter dateConverter=new XMLGregorianCalendarConverter();
    this.membershipEnd = dateConverter.convertToZonedDateTime(membershipEnd);
    return this;
  }

  public ParticipantCertificationBuilder description(String description) {
    this.description = description;
    return this;
  }

  public ParticipantCertificationBuilder title(String title) {
    this.title = title;
    return this;
  }

  public ParticipantCertificationBuilder participant(Participant1 participant) {
    this.participant = participant;
    return this;
  }

  public ParticipantCertificationBuilder ids_id(String ids_id) {
    this.ids_id = URI.create(ids_id);
    return this;
  }

  public ParticipantCertificationBuilder version(String version) {
    this.version = version;
    return this;
  }

  public ParticipantCertification build() {
    return new ParticipantCertification(ids_id, certificationLevel, lastValidDate, evaluationFacility,
        participantCertificationID, membershipEnd, participant, description, title, version);
    }
}