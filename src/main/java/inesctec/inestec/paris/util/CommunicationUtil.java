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
package inesctec.inestec.paris.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import inesctec.inestec.paris.config.ConnectorDeserializer;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.model.Connector;
import lombok.NoArgsConstructor;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
public class CommunicationUtil{
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationUtil.class);

    public static String serialize(Object participant){
      Serializer serializer=new Serializer();
      try {
        return serializer.serialize(participant);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

  public static String serialize(List<?> participants){
    Serializer serializer=new Serializer();
    try {
      return serializer.serialize(participants, Lang.JSONLD);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<?> deserializeList(String string, Class<?> valueType){
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode arrayNode = null;
    try {
      arrayNode = objectMapper.readTree(string);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    if(valueType.isAssignableFrom(ParticipantFraunDto.class)){
      List<ParticipantFraunDto> partDTOList = new ArrayList<>();

      for (JsonNode node : arrayNode) {
        ParticipantFraunDto partDTO = deserialize(node.toString(), ParticipantFraunDto.class);
        partDTOList.add(partDTO);
      }
      return partDTOList;
    }
    return null;
  }

  public static <T> T deserialize(String string, Class<?> valueType){
    Serializer serializer=new Serializer();

    // Handles Participant types
    if (valueType.isAssignableFrom(Participant.class)){
      try {
        return (T) serializer.deserialize(string, Participant.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // Handles ParticipantFraunDto types
    if(valueType.isAssignableFrom(ParticipantFraunDto.class)){
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = null;
      try {
        rootNode = objectMapper.readTree(string);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      // Deserialize Participant
      JsonNode participantNode = rootNode.get("participant");
      Participant participant = null;
      try {
        participant = serializer.deserialize(participantNode.toString(), Participant.class);
      } catch (IOException e) {
        // The exception can be thrown if the Participant does not have the context. In this case, first try to use the converter and only if it fails, throw the exception
        JsonConverter jsonConverter=new JsonConverter();
        try {
          participant=jsonConverter.json2Object(Participant.class, participantNode);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }

      // Deserialize Set<Connector>
      JsonNode connectorSetNode = rootNode.get("connectors");
      Set<Connector> connectorSet = null;
      try {
        connectorSet = Connector.deserializeSet(connectorSetNode.toString());
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }

      // Create and return ParticipantFraunDto
      ParticipantFraunDto participantFraunDto = new ParticipantFraunDto(participant, connectorSet);
      return (T) participantFraunDto;
    }
    return null;
  }



}
