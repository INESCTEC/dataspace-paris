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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fraunhofer.iais.eis.CertificationLevel;
import inesctec.inestec.paris.util.LocalDateTimeTypeAdapter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.util.ZonedDateTimeTypeAdapter;
import lombok.*;

import javax.persistence.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ParticipantCertificationDto {

    /* Certification-ID */
    @Convert(converter = UriConverter.class)
    private URI certificationIdsID;

    /**
     * Participant's certification description. Optional*/
    private String description;

    /**
     * Participant's certification title. Optional*/
    private String title;

    @Enumerated(EnumType.STRING)
    private CertificationLevel certificationLevel;

    /**
     * Last date the given Certification is considered valid.
     */
    @JsonDeserialize(using = DateDeserializers.CalendarDeserializer.class)
    private ZonedDateTime lastValidDate;

    /**
     * End of the participant's IDS membership. */
    @JsonDeserialize(using = DateDeserializers.CalendarDeserializer.class)
    private ZonedDateTime membershipEnd;

    /**
     * Evaluation Facility that performed and issued given Certification
     */
    private EvaluationFacilityDto evaluationFacility;

    private String version;

    public String toJson(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(this);
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantCertificationDto that)) return false;
        return Objects.equals(getCertificationIdsID(), that.getCertificationIdsID())
            && Objects.equals(getDescription(), that.getDescription())
            && Objects.equals(getTitle(), that.getTitle())
            && getCertificationLevel() == that.getCertificationLevel()
            && Objects.equals(getLastValidDate(), that.getLastValidDate())
            && Objects.equals(getMembershipEnd(), that.getMembershipEnd())
            && Objects.equals(getEvaluationFacility(), that.getEvaluationFacility())
            && Objects.equals(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCertificationIdsID(), getDescription(), getTitle(), getCertificationLevel(), getLastValidDate(), getMembershipEnd(), getEvaluationFacility(), getVersion());
    }
}
