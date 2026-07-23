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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import inesctec.inestec.paris.dto.ConnectorDto;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.LocalDateTimeTypeAdapter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.util.ZonedDateTimeTypeAdapter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static inesctec.inestec.paris.config.DatabaseConstants.URI_COLUMN_LENGTH;

@Getter
@Setter
@NoArgsConstructor
@Table
@Entity
public class Connector  implements Serializable {
    /**
     * Serial version uid.
     **/
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id_connector;


    /** Connector UUID. */
    @Column
    private String connectorUuid;

    /* Connector-ID */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH, unique = true)
    private URI connectorID;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "participant_connector",
            joinColumns = @JoinColumn(name = "id_connector"),
            inverseJoinColumns = @JoinColumn(name = "id_participant"))
    @JsonIgnore
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE})
    private Set<Participant1> participantSet;


    /* Connector Endpoint */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH, unique = true)
    private URI connectorIdsEndpoint;


    public Connector(URI connectorID, URI connectorIdsEndpoint) {
        this.connectorID = connectorID;
        this.connectorIdsEndpoint = connectorIdsEndpoint;
    }

    public Connector(URI connectorID, String connectorUuid, URI connectorIdsEndpoint) {
        this.connectorUuid = connectorUuid;
        this.connectorID = connectorID;
        this.connectorIdsEndpoint = connectorIdsEndpoint;
    }

  public static Connector deserialize(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json, Connector.class);
  }

  public static Set<Connector> deserializeSet(String string) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    // Use a TypeReference to capture the generic type information for Set<Connector>
    TypeReference<Set<Connector>> typeReference = new TypeReference<Set<Connector>>() {};

    // Convert JSON to Set<Connector>
    JsonNode rootNode = objectMapper.readTree(string);
    if (rootNode.isArray()) {
      return objectMapper.readValue(string, typeReference);
    } else if (rootNode.isObject()) {
      // If it's a single Connector, create a set with a single Connector
      Connector connector = objectMapper.treeToValue(rootNode, Connector.class);
      Set<Connector> connectorSet = new HashSet<>();
      connectorSet.add(connector);
      return connectorSet;
    } else {
      throw new IllegalArgumentException("Invalid JSON format");
    }
  }

  public void update(Connector connector){
        this.connectorUuid=connector.getConnectorUuid();
        this.connectorID=connector.getConnectorID();
        this.connectorIdsEndpoint=connector.getConnectorIdsEndpoint();
        // I'm not updating the participantSet because the association can never be updated. Only removed
    }

    @Override
    public String toString() {
        return "Connector{" +
                "connectorUuid='" + connectorUuid + '\'' +
                ", connectorID=" + connectorID +
                ", connectorIdsEndpoint=" + connectorIdsEndpoint +
                '}';
    }

    public void setParticipantSet(Set<Participant1> participant) {
        this.participantSet = participant;
    }
    public void setParticipantSet(Participant1 participant) {
        if(this.participantSet ==null) {
            this.participantSet =new HashSet<>();
        }
        this.participantSet.add(participant);
    }

  public void removeParticipant(Participant1 participant) {
      this.participantSet.removeIf(p-> participant.getParticipantIdsID().equals(p.getParticipantIdsID()));
  }

    public String toJson(){
        JsonConverter jsonConverter=new JsonConverter();
        try {
            return jsonConverter.object2Json(this).toPrettyString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson_ToString(){
        JsonConverter jsonConverter=new JsonConverter();
        try {
            return jsonConverter.object2Json(this).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toSimpleJson(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .create();
        String json = gson.toJson(this);
        //System.out.println(json.toString());
        return json;
    }

    /*public Set<ConnectorDto> convertToConnectorDto(Set<Connector> connectors){
        if (connectors.isEmpty()){
            return null;
        }
        else {
            Set<ConnectorDto> newConnectorsDto=new HashSet<>();
            connectors.forEach(conn-> {
                ConnectorDto connectorDto=new ConnectorDto();
                connectorDto.setConnectorID(conn.getConnectorID());
                connectorDto.setConnectorUuid(conn.getConnectorUuid());
                connectorDto.setConnectorIdsEndpoint(conn.getConnectorIdsEndpoint());

                newConnectorsDto.add(connectorDto);
            });
            return newConnectorsDto;
        }
    }*/

    public ConnectorDto convertToConnectorDto(){
        if (this==null){
            return null;
        }
        else {

            ConnectorDto connectorDto = new ConnectorDto();
            connectorDto.setConnectorID(this.getConnectorID());
            connectorDto.setConnectorUuid(this.getConnectorUuid());
            connectorDto.setConnectorIdsEndpoint(this.getConnectorIdsEndpoint());
            return connectorDto;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Connector connector)) return false;
        /*System.out.println("ConnectorUuid: "+Objects.equals(getConnectorUuid(), connector.getConnectorUuid()) +
                "   \nConnectorID: "+ Objects.equals(getConnectorID(), connector.getConnectorID()) +
                "\nConnectorIdsEndpoint: "+ Objects.equals(getConnectorIdsEndpoint(), connector.getConnectorIdsEndpoint()));*/
        return Objects.equals(ObjectUtils.defaultIfNull(getConnectorUuid(), null), ObjectUtils.defaultIfNull(connector.getConnectorUuid(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getConnectorID(), null), ObjectUtils.defaultIfNull(connector.getConnectorID(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getConnectorIdsEndpoint(), null), ObjectUtils.defaultIfNull(connector.getConnectorIdsEndpoint(), null));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId_connector(), getConnectorUuid(), getConnectorID(), getConnectorIdsEndpoint());
    }


}
