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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;
import inesctec.inestec.paris.config.ConnectorDeserializer;
import inesctec.inestec.paris.config.ConnectorSerializer;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.converter.JsonConverter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ParticipantFraunDto implements Serializable {

    @JsonProperty
    private Participant participant;

    @JsonProperty
    @JsonSerialize(using = ConnectorSerializer.class) // This prevents the IDS Serializer from adding
    // @id to the URI attributes
    @JsonDeserialize(using = ConnectorDeserializer.class)
    private Set<Connector> connectors;

    public ParticipantFraunDto() {
    }
    public ParticipantFraunDto(Participant participant, Set<Connector> connectors) {
        this.participant = participant;
        this.connectors = connectors;
    }

    public ParticipantFraunDto(Participant participant) {
        this.participant = participant;
        this.connectors = new HashSet<>();
    }

    public ParticipantFraunDto(Participant participant, Connector connector) {
        this.participant = participant;
        this.connectors = new HashSet<>();
        this.connectors.add(connector);
    }

    @Override
    public String toString() {
        return "ParticipantFraunDto{" +
                ", participant='" + participant + '\'' +
                ", connector=" + connectors +
                '}';
    }

    public String toSimpleJson() {
        JsonConverter jsonConverter=new JsonConverter();
        JsonNode part = null;
        String response="";
        try {
            part = jsonConverter.object2Json(this.participant);

            JsonNode conn =jsonConverter.object2Json(this.connectors);

            response="{" +
                    "\"participant\":" + part +
                    ", \"connectors\":" + conn +
                    '}';

            response = response.replace("\n", "").replace(" ", "")
                    .replace("\r", "");
            System.out.println("Formated: "+response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response.toString();
    }

    public Participant getParticipant() {
        return participant;
    }

    public Set<Connector> getConnectors() {
        return connectors;
    }

    public String toJson(){

        JsonConverter jsonConverter=new JsonConverter();
        try {
            return jsonConverter.object2Json(this).toPrettyString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson3(){

        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer());
        objectMapper.registerModule(module);

        // Enable pretty printing if needed
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Convert object to JSON string
        String jsonString = null;
        try {
            jsonString = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Print JSON string
        //System.out.println(jsonString);
        return jsonString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantFraunDto that)) return false;
        return participant.equals(that.participant) && connectors.equals(that.connectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participant, connectors);
    }

    public void convertConnectorSetToMutable(){
        if (this.connectors != null) {
            this.connectors = new HashSet<>(connectors);
        }
    }

}
