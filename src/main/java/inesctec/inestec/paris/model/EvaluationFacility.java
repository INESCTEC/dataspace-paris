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
import inesctec.inestec.paris.dto.EvaluationFacilityDto;
import inesctec.inestec.paris.converter.UriConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

import static inesctec.inestec.paris.config.DatabaseConstants.URI_COLUMN_LENGTH;

@Getter
@Setter
@Table(name = "evaluation_facility")
@Entity
@NoArgsConstructor
public class EvaluationFacility implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_evaluationFacility;

    /* EvaluationFacility-ID */
    @Convert(converter = UriConverter.class)
    @Column(columnDefinition = "varchar(2048)", length = URI_COLUMN_LENGTH, unique = true)
    private URI evaluationFacilityID;

    @OneToMany(mappedBy="evaluationFacility",fetch = FetchType.EAGER/*, cascade = CascadeType.ALL, orphanRemoval = true*/)
    @JsonBackReference
    private Set<ParticipantCertification> certifications;

    public EvaluationFacility(URI evaluationFacilityID) {
        this.evaluationFacilityID = evaluationFacilityID;
    }

    @Override
    public String toString() {
        return "EvaluationFacility{" +
            "evaluationFacilityID=" + evaluationFacilityID +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvaluationFacility that)) return false;
        /*System.out.println("evaluationFacility ID: " + Objects.equals(ObjectUtils.defaultIfNull(getId_evaluationFacility(), null), ObjectUtils.defaultIfNull(that.getId_evaluationFacility(), null))
                + "\nevaluationFacility IDS ID: " + Objects.equals(ObjectUtils.defaultIfNull(getEvaluationFacilityID(), null), ObjectUtils.defaultIfNull(that.getEvaluationFacilityID(), null))
                + "\nCertifications: " + Objects.equals(ObjectUtils.defaultIfNull(getCertifications(), null), ObjectUtils.defaultIfNull(that.getCertifications(), null)));
        */
        return Objects.equals(ObjectUtils.defaultIfNull(getEvaluationFacilityID(), null), ObjectUtils.defaultIfNull(that.getEvaluationFacilityID(), null))
                /*&& Objects.equals(ObjectUtils.defaultIfNull(getCertifications(), null), ObjectUtils.defaultIfNull(that.getCertifications(), null))*/;
    }

    public EvaluationFacilityDto convertToEvaluationFacilityDto(){
        if (this.equals(null)){
            return null;
        }
        else {
            return new EvaluationFacilityDto(this.getEvaluationFacilityID());
        }
    }

    public void update(EvaluationFacility evaluationFacility){
        this.evaluationFacilityID=evaluationFacility.getEvaluationFacilityID();
        // I'm not updating the certificationSet because argument never has this value
    }
    @Override
    public int hashCode() {
        return Objects.hash(getId_evaluationFacility(), getEvaluationFacilityID());
    }
}
