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
package inesctec.inestec.paris.service;

import inesctec.inestec.paris.dto.ConnectorDto;
import inesctec.inestec.paris.dto.ConnectorWithListOfParticipantsDto;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.Person;
import inesctec.inestec.paris.repository.ConnectorRepository;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectorService {

    private final ConnectorRepository repository;

    public ConnectorService(ConnectorRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Connector save(Connector connector, Participant1 participant){
        connector.setParticipantSet(participant);
        return repository.save(connector);
    }

    public void save(List<Connector> connectorList){
        connectorList.stream().forEach(v -> repository.save(v));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Connector save(Connector connector){
        repository.save(connector);
        return connector;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Connector update(Connector connector){
        Connector connectorOnDB=findConnectorByConnectorIdsID(
                String.valueOf(connector.getConnectorID()))
                .orElseThrow(() -> new ResourceNotFoundException("Connector", "ID", String.valueOf(connector.getConnectorID()))
                );

        // Update and Save the Connector
        connectorOnDB.update(connector);

        Connector updatedConnector;
        try {
            updatedConnector=repository.save(connectorOnDB);
            return updatedConnector;
        }catch (Exception e){
            System.err.println(e.toString());
            System.err.println(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }


    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Connector removeParticipant(Connector connector){
        Connector connectorOnDB=findConnectorByConnectorIdsID(
            String.valueOf(connector.getConnectorID()))
            .orElseThrow(() -> new ResourceNotFoundException("Connector", "ID", String.valueOf(connector.getConnectorID()))
            );

        // Update the Connector On DB to have the same participants as the updated one
        connectorOnDB.setParticipantSet(connector.getParticipantSet());

        try {
            Connector updatedConn=repository.save(connectorOnDB);
            return updatedConn ;
        }catch (Exception e){
            System.err.println(e);
            throw new RuntimeException(e.getMessage());
        }


    }

    public Optional<Connector> findByID(Long id){
        return repository.findById(id);
    }

    public Iterable<Connector> findAll(){
        return repository.findAll();
    }

    public void remove(Connector connector){
        repository.delete(connector);
    }

    public void remove(List<Connector> connector){
        connector.stream().forEach(v -> repository.delete(v));
    }

    public void remove(){
        repository.deleteAll();
    }

    public Integer size(){
        return ((List<Connector>) this.findAll()).size();
    }

    @Transactional
    public Optional<Connector> findConnectorByConnectorIdsID(String ids_id){
        return repository.findConnectorByConnectorID(URI.create(ids_id));
    }

    /**
     * Get all Connectors that have a search term in their ConnectorID
     * @param term term to search for (e.g., producer)
     * */
    @Transactional
    public List<ConnectorWithListOfParticipantsDto> findConnectorsBySearchTerm(String term){
        List<ConnectorWithListOfParticipantsDto> connectorsListWithTheKeyword= new ArrayList<>();
        this.findAll().forEach(connector -> {
            // If the connectorID contains the search term, e.g., producer), adds the Connector to the list to return
            if(connector.getConnectorID().toString().contains(term)){
                connectorsListWithTheKeyword.add(new ConnectorWithListOfParticipantsDto(connector));
            }
        });
        return connectorsListWithTheKeyword;
    }

    public Optional<Connector> findConnectorsByConnectorIdsEndpoint(String ids_id){
        return repository.findConnectorsByConnectorIdsEndpoint(URI.create(ids_id));
    }

    public List<Connector> findConnectorsFromParticipant(Set<Participant1> participantSet){
        return repository.findConnectorsByParticipantSetIn(participantSet);
    }
    public List<Connector> findConnectorsFromParticipant(Participant1 participant1){
        return this.findConnectorsFromParticipant(Set.of(participant1));
    }

    @Transactional
    public Optional<List<String>> findParticipantIDsByConnectorIdsID(Connector connector){
        return repository.findParticipantIDsByConnectorIdsID(connector.getConnectorID().toString());
    }

    //@Transactional(propagation = Propagation.REQUIRED)
    public Connector associateConnectorToParticipant(Connector connector, Participant1 participant){
        connector.setParticipantSet(participant);
        return this.update(connector);
    }
}
