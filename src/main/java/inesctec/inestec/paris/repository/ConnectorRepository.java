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
package inesctec.inestec.paris.repository;

import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Constraint;
import inesctec.inestec.paris.model.Participant1;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ConnectorRepository extends CrudRepository<Connector, Long> {
    void deleteAll();

    Optional<Connector> findConnectorByConnectorID(URI id);

    List<Connector> findConnectorsByParticipantSetIn(Set<Participant1> participant);

    @Query(value = "select distinct p.participant_idsid" +
            " from participant p " +
            "       left join participant_connector pc on p.id_participant = pc.id_participant" +
            "       left join connector c on c.id_connector = pc.id_connector" +
            " where c.connectorID= :id", nativeQuery = true)
    Optional<List<String>> findParticipantIDsByConnectorIdsID(@Param("id") String id);

    Optional<Connector> findConnectorsByConnectorIdsEndpoint(URI id);
}
