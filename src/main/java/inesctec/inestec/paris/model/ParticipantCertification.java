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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fraunhofer.iais.eis.CertificationLevel;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.dto.ParticipantCertificationDto;
import inesctec.inestec.paris.util.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Cascade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

import static inesctec.inestec.paris.config.DatabaseConstants.URI_COLUMN_LENGTH;

@Getter
@Setter
@Table(name = "participant_certification", uniqueConstraints = {
    @UniqueConstraint(name = "UniqueIDSID_Version", columnNames = { "certificationIdsID", "version" })})
@Entity
public class ParticipantCertification extends Certification implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantCertification.class);
    @Id /*@GeneratedValue(strategy = GenerationType.IDENTITY)*/
    private Long participantCertificationID;

    /**
     * End of the participant's IDS membership. */
    @Column(nullable = true)
    private ZonedDateTime membershipEnd;

    /**
     * Participant's certification description. Optional*/
    @Column
    private String description;

    /**
     * Participant's certification title. Optional*/
    @Column
    private String title;

    /**
     * Evaluation Facility that performed and issued given Certification
     */
    // TODO: nullable should be false
    @ManyToOne
    @JoinColumn(name="evaluationFacilityID", nullable=true)
    private EvaluationFacility evaluationFacility;


    @OneToOne//(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "participantCertificationID")
    @JsonBackReference
    private Participant1 participant;


    /* Certification-ID */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI certificationIdsID;

    @Column
    private String version;

    public ParticipantCertification(URI certificationIdsID, CertificationLevel certificationLevel,
                                    ZonedDateTime lastValidDate, EvaluationFacility evaluationFacility,
                                    Long participantCertificationID, ZonedDateTime membershipEnd,
                                    Participant1 participant, String description, String title, String version) {
        super(certificationLevel, lastValidDate);
        this.evaluationFacility=evaluationFacility;
        this.participantCertificationID = participantCertificationID;
        this.membershipEnd = membershipEnd;
        this.participant = participant;
        this.description=description;
        this.title=title;
        this.certificationIdsID=certificationIdsID;
        this.version=version;

    }

    public void update(ParticipantCertification certification) {
        this.evaluationFacility=certification.evaluationFacility;
        this.membershipEnd = certification.membershipEnd;
        this.description=certification.description;
        this.title=certification.title;
        this.certificationIdsID=certification.certificationIdsID;
        this.version=certification.version;
    }

    public void update(ParticipantCertification certification, EvaluationFacility evaluationFacility) {
        this.evaluationFacility=evaluationFacility;
        this.participantCertificationID = certification.participantCertificationID;
        this.membershipEnd = certification.membershipEnd;
        this.participant = certification.participant;
        this.description=certification.description;
        this.title=certification.title;
        this.certificationIdsID=certification.certificationIdsID;
        this.version=certification.version;
    }

    public ParticipantCertification() {
        super();
    }

    @Override
    public String toString() {
        return "ParticipantCertification{" +
                super.toString() +
                ", certificationIdsID=" + certificationIdsID +
                ", membershipEnd=" + membershipEnd +
                ", evaluationFacility=" + evaluationFacility +
                ", version=" + version +
                '}';

    }


    public ParticipantCertificationDto convertToCertificationDto(){
        EvaluationFacility ev=this.getEvaluationFacility();

        ParticipantCertificationDto certificationDto=new ParticipantCertificationDto();
        certificationDto.setCertificationIdsID(this.getCertificationIdsID());
        certificationDto.setCertificationLevel(this.getCertificationLevel());
        certificationDto.setDescription(this.getDescription());
        certificationDto.setTitle(this.getTitle());
        certificationDto.setEvaluationFacility(this.getEvaluationFacility()==null ? null :
                ev.convertToEvaluationFacilityDto());
        certificationDto.setMembershipEnd(this.getMembershipEnd());
        certificationDto.setLastValidDate(this.getLastValidDate());
        certificationDto.setVersion(this.getVersion());
        return certificationDto;
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

    public String toJson2() {
        JsonConverter converter=new JsonConverter();
        try {
            return converter.object2Json(this).toPrettyString();
        }catch (Exception exception) {
            LOGGER.error("Certification could not be converted into a JSON");
            return null;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantCertification that)) return false;
        /*System.out.println("getParticipantCertificationID: "+Objects.equals(ObjectUtils.defaultIfNull(getParticipantCertificationID(), null), ObjectUtils.defaultIfNull(that.getParticipantCertificationID(), null))
                + "\ngetCertificationIdsID: "+Objects.equals(ObjectUtils.defaultIfNull(getCertificationIdsID(), null), ObjectUtils.defaultIfNull(that.getCertificationIdsID(), null))
                + "\ngetMembershipEnd: "+Objects.equals(ObjectUtils.defaultIfNull(getMembershipEnd(), null), ObjectUtils.defaultIfNull(that.getMembershipEnd(), null))
                + "\ngetDescription: "+Objects.equals(ObjectUtils.defaultIfNull(getDescription(), null), ObjectUtils.defaultIfNull(that.getDescription(), null))
                + "\ngetTitle: "+Objects.equals(ObjectUtils.defaultIfNull(getTitle(), null), ObjectUtils.defaultIfNull(that.getTitle(), null))
                + "\ngetEvaluationFacility: "+Objects.equals(ObjectUtils.defaultIfNull(getEvaluationFacility(), null), ObjectUtils.defaultIfNull(that.getEvaluationFacility(), null))
                *//*+ "\ngetParticipant: "+Objects.equals(ObjectUtils.defaultIfNull(getParticipant(), null), ObjectUtils.defaultIfNull(that.getParticipant(), null))*//*);
        */
        return Objects.equals(ObjectUtils.defaultIfNull(getCertificationIdsID(), null), ObjectUtils.defaultIfNull(that.getCertificationIdsID(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getMembershipEnd(), null), ObjectUtils.defaultIfNull(that.getMembershipEnd(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getDescription(), null), ObjectUtils.defaultIfNull(that.getDescription(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getTitle(), null), ObjectUtils.defaultIfNull(that.getTitle(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getEvaluationFacility(), null), ObjectUtils.defaultIfNull(that.getEvaluationFacility(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getVersion(), null), ObjectUtils.defaultIfNull(that.getVersion(), null));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParticipantCertificationID(), getMembershipEnd(), getDescription(), getTitle(), getEvaluationFacility(), getCertificationIdsID(), getVersion());
    }
}
