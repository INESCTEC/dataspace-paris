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
import inesctec.inestec.paris.dto.BusinessIdentifierDto;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.UriConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.net.URI;
import java.util.Objects;

import static inesctec.inestec.paris.config.DatabaseConstants.URI_COLUMN_LENGTH;

@Getter
@Setter
@Table(name = "business_identifier")
@Entity
@NoArgsConstructor
public class BusinessIdentifier {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantCertification.class);
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @JsonIgnore
  private Long id;

  /* Identifier-ID */
  @Convert(converter = UriConverter.class)
  @Column(length = URI_COLUMN_LENGTH) //It is not unique because a Participant can have multiple versions, and we need to keep its history
  private URI businessIdentifierIdsID;

  @Column
  private String identifierSystem;

  @Column
  private String identifierNumber;


  @ManyToOne
  @JoinColumn(name="id_participant")
  @JsonIgnore
  private Participant1 participant;

  public BusinessIdentifier(URI businessIdentifierIdsID, String identifierSystem, String identifierNumber) {
    this.businessIdentifierIdsID = businessIdentifierIdsID;
    this.identifierSystem = identifierSystem;
    this.identifierNumber = identifierNumber;
  }

  public BusinessIdentifier(URI businessIdentifierIdsID, String identifierSystem, String identifierNumber, Participant1 participant) {
    this.businessIdentifierIdsID = businessIdentifierIdsID;
    this.identifierSystem = identifierSystem;
    this.identifierNumber = identifierNumber;
    this.participant = participant;
  }

  @Transactional
  public void update(BusinessIdentifier businessIdentifier) {
    this.businessIdentifierIdsID = businessIdentifier.businessIdentifierIdsID;
    this.identifierSystem = businessIdentifier.identifierSystem;
    this.identifierNumber = businessIdentifier.identifierNumber;
  }

  public BusinessIdentifierDto convertToBusinessIdentifierDto(){
    if (this==null){
      return null;
    }
    else {

      BusinessIdentifierDto businessIdentifierDto=new BusinessIdentifierDto();
      businessIdentifierDto.setIdentifierSystem(this.getIdentifierSystem());
      businessIdentifierDto.setIdentifierNumber(this.getIdentifierNumber());
      businessIdentifierDto.setBusinessIdentifierIdsID(this.getBusinessIdentifierIdsID());
      return businessIdentifierDto;
    }
  }

  public String toJson() {
    JsonConverter converter=new JsonConverter();
    try {
      return converter.object2Json(this).toPrettyString();
    }catch (Exception exception) {
      LOGGER.error("Business Identifier could not be converted into a JSON");
      return null;
    }
  }

  @Override
  public String toString() {
    return "BusinessIdentifier{" +
            "businessIdentifierIdsID=" + businessIdentifierIdsID +
            ", identifierSystem='" + identifierSystem + '\'' +
            ", identifierNumber='" + identifierNumber + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BusinessIdentifier that)) return false;

    /*System.err.println("BusinessIdentifierIdsID: "+ Objects.equals(getBusinessIdentifierIdsID(), that.getBusinessIdentifierIdsID()));
    System.err.println("IdentifierSystem: "+Objects.equals(getIdentifierSystem(), that.getIdentifierSystem()));
    System.err.println("IdentifierNumber: "+Objects.equals(getIdentifierNumber(), that.getIdentifierNumber()));*/
    return Objects.equals(getBusinessIdentifierIdsID(), that.getBusinessIdentifierIdsID())
            && Objects.equals(getIdentifierSystem(), that.getIdentifierSystem())
            && Objects.equals(getIdentifierNumber(), that.getIdentifierNumber());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getBusinessIdentifierIdsID(), getIdentifierSystem(), getIdentifierNumber());
  }
}
