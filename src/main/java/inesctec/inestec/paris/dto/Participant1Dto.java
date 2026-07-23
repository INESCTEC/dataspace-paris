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
package inesctec.inestec.paris.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.LocalDateTimeTypeAdapter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.util.ZonedDateTimeTypeAdapter;
import lombok.*;

import javax.persistence.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participant1Dto {


    /* Participant-ID */
    @Convert(converter = UriConverter.class)
    private URI participantIdsID;

    /* The complete legal name of the IDS Participant, */
    private List<String> legalName;

    /* The commonly used name or term for the IDS Participant
    * Also known as title */
    private List<String> name;


    private List<String> description;


    /* Email address for contacting the participant on a general level. */
    private List<String> corporateEmailAddress;

    /* General official homepage of the participant. */
    @Convert(converter = UriConverter.class)
    private URI corporateHomepage;

    //private List<Participant1Dto> memberParticipant;

    /* Conditions which needs to be satisfied that a single Participant can be seen as a member of the
    subject Participant. For instance, all Participants with their headquarter in
    Europe might be a potential consumer of GDPR-releated data.*/
    private LogicalConstraintDto participantRefinement;

    private Set<PersonDto> memberPerson;

    private ParticipantCertificationDto participantCertification;

    private String primarySite;

    /* The Value-added Tax ID of the IDS Participant.
    * identifierNumber
    * e.g. the NIF number
    * unique Validation is working */
    private String vatID;

    /* The Tax / Fiscal ID of the IDS Participant, e.g. NIF
    * identifierSystem */
    @Enumerated(EnumType.STRING)
    private Set<BusinessIdentifierDto> businessIdentifier;

    /* The responsible court for the organization. Usually a city and country.
    * Same as jurisdiction */
    private String registryCourt;

    /* The legal form of the IDS Participant. */
    private String legalForm;

    private String version;

    private Set<ConnectorDto> connectorSet;

    public void setBusinessIdentifier(Set<BusinessIdentifierDto> businessIdentifierDtos) {
        this.businessIdentifier = businessIdentifierDtos;
    }

    public void setBusinessIdentifier(BusinessIdentifierDto businessIdentifierDtos) {
        if(this.businessIdentifier==null) this.businessIdentifier=new HashSet<>();
        this.businessIdentifier.add(businessIdentifierDtos);
    }

    /*public void setMemberParticipant(List<Participant1Dto> memberParticipant) {
        this.memberParticipant = memberParticipant;
    }

    public void setMemberParticipant(Participant1Dto memberParticipant) {
        this.memberParticipant.add(memberParticipant);
    }

    public void setMemberParticipant() {
        this.memberParticipant=new ArrayList<>();
    }
*/

    @Override
    public String toString() {
        return "Participant1{" +
                "participantID='" + participantIdsID + '\'' +
                ", legalName='" + legalName + '\'' +
                ", name='" + name + '\'' +
                ", corporateEmailAddress='" + corporateEmailAddress + '\'' +
                ", corporateHomepage=" + corporateHomepage +
                //", memberParticipant=" + memberParticipant +
                //", children=" + children +
                ", participantRefinement='" + participantRefinement + '\'' +
                ", primarySite='" + primarySite + '\'' +
                ", vatID='" + vatID + '\'' +
                ", businessIdentifier='" + businessIdentifier + '\'' +
                ", registryCourt='" + registryCourt + '\'' +
                ", legalForm='" + legalForm + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public String toJson(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(this);
        //System.out.println(json.toString());
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Participant1 that)) {
            return false;
        }
        return Objects.equals(getParticipantIdsID(), that.getParticipantIdsID())
                && CompareUtil.compareStrings(getLegalName(),that.getLegalName())
                && CompareUtil.compareStrings(getName(), that.getName())
                && Objects.equals(getDescription(), that.getDescription())
                && CompareUtil.compareStrings(getCorporateEmailAddress(),that.getCorporateEmailAddress())
                && Objects.equals(getCorporateHomepage(), that.getCorporateHomepage())
                && Objects.equals(getParticipantRefinement(), that.getParticipantRefinement())
                && CompareUtil.compareSet(getMemberPerson(), that.getMemberPerson())
                && Objects.equals(getParticipantCertification(),that.getParticipantCertification())
                && Objects.equals(getPrimarySite(),that.getPrimarySite())
                && Objects.equals(getVatID(),that.getVatID())
                && Objects.equals(getBusinessIdentifier() , that.getBusinessIdentifier())
                && Objects.equals(getRegistryCourt(), that.getRegistryCourt())
                && Objects.equals(getLegalForm(), that.getLegalForm())
                && Objects.equals(getVersion(),that.getVersion())
                && CompareUtil.compareSet(getConnectorSet(), that.getConnectorSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParticipantIdsID(), getLegalName(), getName(), getDescription(), getCorporateEmailAddress(), getCorporateHomepage(), getParticipantRefinement(), getMemberPerson(), getParticipantCertification(), getPrimarySite(), getVatID(), getBusinessIdentifier(), getRegistryCourt(), getLegalForm(), getVersion(), getConnectorSet());
    }
}
