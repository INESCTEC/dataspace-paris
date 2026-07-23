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
package inesctec.inestec.paris.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import inesctec.inestec.paris.model.Connector;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class ConnectorDeserializer extends JsonDeserializer<Set<Connector>> {
  @Override
  public Set<Connector> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    Set<Connector> connSet = new HashSet<>();
    JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
    if (jsonNode.isArray()) {
      for (JsonNode node : jsonNode) {
        // Deserialize each Connector object from JSON and add it to the set
        Connector conn = deserializeConn(node);
        if (conn != null) {
          connSet.add(conn);
        }
      }
    }
    return connSet;
  }

  private Connector deserializeConn(JsonNode jsonNode) {

    // Instantiate and return a Conn object
    return new Connector(URI.create(jsonNode.get("connectorID").asText()),
            jsonNode.get("connectorUuid").asText(),
            URI.create(jsonNode.get("connectorIdsEndpoint").asText()));
  }
}