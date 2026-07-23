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
import inesctec.inestec.paris.converter.UriConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.net.URI;


@Schema(description = "Connector Dto")
@NoArgsConstructor
@Getter @Setter
public class ConnectorDto {

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



    public ConnectorDto(URI personIdsID) {
        this.connectorID = personIdsID;
    }

    public ConnectorDto(URI connectorID, String connectorUuid) {
        this.connectorUuid = connectorUuid;
        this.connectorID = connectorID;
    }

    @Override
    public String toString() {
        return "ConnectorDto{" +
                ", uuid='" + connectorUuid + '\'' +
                ", connectorIdsID=" + connectorID +
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
}
