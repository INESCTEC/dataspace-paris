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

import inesctec.inestec.paris.model.ParticipantCertification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Optional;

@Repository
public interface ParticipantCertificationRepository extends CrudRepository<ParticipantCertification, Long> {
    void deleteAll();

    Optional<ParticipantCertification> findParticipantCertificationByParticipantCertificationID(Long aLong);

    //Optional<ParticipantCertification> findParticipantCertificationByCertificationIdsID(URI uri);
    Optional<ParticipantCertification> findParticipantCertificationByCertificationIdsID(URI uri);
    Optional<ParticipantCertification> findFirstByCertificationIdsIDOrderByVersionDesc(URI uri);
}
