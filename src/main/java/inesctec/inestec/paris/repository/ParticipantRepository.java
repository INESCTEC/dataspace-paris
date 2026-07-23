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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.net.URI;
import java.util.Set;

@Repository
public interface ParticipantRepository extends CrudRepository<Participant1, Long> {
    /*@Transactional
    Optional<List<Participant1>> findParticipant1ByParticipantIdsIDOrderByVersionDesc(URI participantID);
*/
    @Transactional
    Optional<Participant1> findFirstByParticipantIdsIDOrderByVersionDesc(URI participantID);

    Optional<Set<Participant1>> findParticipant1ByConnectorSetIn(Set<Connector> connectorSet);

    void deleteAll();

    Optional<Set<Participant1>> findParticipant1ByName(String name);

    @Query(value = "select distinct p.participant_idsid " +
            "from participant p " +
            "         left join participant_name pn on p.id_participant = pn.participant1_id_participant " +
            "where pn.name= :name and p.participant_idsid= :participantID", nativeQuery = true)
    List<String> findAssociationBetweenParticipantAndTitle(@Param("participantID") String participantID, @Param("name") String name);

    @Query(value = "select distinct p.participant_idsid " +
        "from participant p " +
        "         left join participant_description pn on p.id_participant = pn.participant1_id_participant " +
        "where pn.description= :description and p.participant_idsid= :participantID", nativeQuery = true)
    List<String> findAssociationBetweenParticipantAndDescription(@Param("participantID") String participantID, @Param("description") String description);

}
