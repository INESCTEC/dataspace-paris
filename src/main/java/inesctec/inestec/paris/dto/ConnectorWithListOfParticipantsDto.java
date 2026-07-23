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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Participant1;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Convert;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;


@NoArgsConstructor
@Getter @Setter
public class ConnectorWithListOfParticipantsDto {

    /** Certificate UUID. */
    @JsonProperty
    public String connectorUuid;

    /* Connector-ID */
    @JsonProperty
    @Convert(converter = UriConverter.class)
    public URI connectorID;

    /* Connector Endpoint */
    @Convert(converter = UriConverter.class)
    @JsonProperty
    private URI connectorIdsEndpoint;

    @JsonProperty("participants")
    private List<URI> participantsList;


    public ConnectorWithListOfParticipantsDto(Connector connector) {
        this.connectorUuid = connector.getConnectorUuid();
        this.connectorID = connector.getConnectorID();
        this.connectorIdsEndpoint=connector.getConnectorIdsEndpoint();
        this.participantsList=connector.getParticipantSet().stream().map(Participant1::getParticipantIdsID).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ConnectorWithListOfParticipantsDto{" +
            "connectorUuid='" + connectorUuid + '\'' +
            ", connectorID=" + connectorID +
            ", connectorIdsEndpoint=" + connectorIdsEndpoint +
            ", participantsList=" + participantsList +
            '}';
    }

    public String toJson(){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(this);
        //System.out.println(json.toString());
        return json;
    }

    public String toJson_ToString(){
        JsonConverter jsonConverter=new JsonConverter();
        try {
            return jsonConverter.object2Json(this).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
