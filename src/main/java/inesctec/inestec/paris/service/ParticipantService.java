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

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.RdfResource;
import inesctec.inestec.paris.builder.ParticipantBuilder;
import inesctec.inestec.paris.dto.BusinessIdentifierDto;
import inesctec.inestec.paris.dto.ConnectorDto;
import inesctec.inestec.paris.dto.Participant1Dto;
import inesctec.inestec.paris.dto.PersonDto;
import inesctec.inestec.paris.enums.IdentifierSystem;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.exceptions.ResourceAlreadyExistsException;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.model.BusinessIdentifier;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.LogicalConstraint;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.Person;
import inesctec.inestec.paris.repository.EvaluationFacilityRepository;
import inesctec.inestec.paris.repository.ParticipantRepository;
import inesctec.inestec.paris.util.ParticipantUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.net.URI;
import java.util.stream.Collectors;

@Service
public class ParticipantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantService.class);
    private final ParticipantRepository repository;
    private final EvaluationFacilityRepository evaluationFacilityRepository;

    public ParticipantService(ParticipantRepository repository, EvaluationFacilityRepository evaluationFacilityRepository) {
        this.evaluationFacilityRepository=evaluationFacilityRepository;
        this.repository = repository;
    }

    @Transactional
    public Participant1 save(Participant1 participant){
        return repository.save(participant);
        /*try{
            return repository.save(participant);}
        catch (DataIntegrityViolationException ex){
            LOGGER.error("Trying to insert a duplicate value on a unique column");
            return null;
        }*/
    }

    public void save(List<Participant1> participantList){
        repository.saveAll(participantList);
    }

    public Optional<Participant1> findByID(Long id){
        return repository.findById(id);
    }

    public Iterable<Participant1> findAll(){
        return repository.findAll();
    }

    public void remove(Participant1 participant){
        repository.delete(participant);
    }

    public void remove(){
        repository.deleteAll();
    }

    public Integer size(){
        return ((List<Participant1>) this.findAll()).size();
    }

    public Optional<Participant1> findParticipantByParticipantIDSID(URI participant_id){
        return repository.findFirstByParticipantIdsIDOrderByVersionDesc(participant_id);
    }

    @Transactional
    public Participant1 update(Participant1 participant2) {
        Participant1 participantOnBD = repository.findFirstByParticipantIdsIDOrderByVersionDesc(participant2.getParticipantIdsID())
                .orElseThrow(() -> new ResourceNotFoundException("Participant", "ID", String.valueOf(participant2.getParticipantIdsID())));
        participantOnBD.update(participant2);


        return repository.save(participantOnBD);
    }

    public Set<Participant1> findParticipant1ByConnectorSet(Set<Connector> connectors){
        return repository.findParticipant1ByConnectorSetIn(connectors)
                .orElseThrow(() -> new ResourceNotFoundException("Participant", "Connector", "ID", connectors.stream().findFirst().get().getConnectorID().toString()));
    }

    @Transactional
    public Optional<Participant1> findParticipant1ByParticipantIdsID(URI participantIdsID){
        return repository.findFirstByParticipantIdsIDOrderByVersionDesc(participantIdsID);
    }

    public Set<Participant1> findParticipantByName(String name){
        return repository.findParticipant1ByName(name).orElseGet(() -> new HashSet<>());
    }

    public void addTitleToParticipant(String name, Participant1 participant1){

        List<String> association =  repository.findAssociationBetweenParticipantAndTitle(participant1.getParticipantIdsID().toString(), name);
        if(association.isEmpty()) {
            LOGGER.debug("Adding the new title to the Participant");
            participant1.setName(name);
            // Add another title to the Participant
            this.update(participant1);
        }
        else {
            LOGGER.debug("Participant already has the title");
            throw new ResourceAlreadyExistsException("Participant '"+participant1.getParticipantIdsID()+"' already has the title '"+name+"'");
        }
    }

    public void removeTitleFromParticipant(String name, Participant1 participant1){

        List<String> association =  repository.findAssociationBetweenParticipantAndTitle(participant1.getParticipantIdsID().toString(), name);
        if(association.isEmpty()) {
            LOGGER.debug("Participant already has the title");
            throw new ResourceNotFoundException("Title", "Participant", "ID", participant1.getParticipantIdsID().toString());
        }
        else {
            LOGGER.debug("Removing the title from the Participant");
            participant1.getName().remove(name);
            // Add another title to the Participant
            this.update(participant1);
        }
    }

    public void addDescriptionToParticipant(String name, Participant1 participant1){

        List<String> association =  repository.findAssociationBetweenParticipantAndDescription(participant1.getParticipantIdsID().toString(), name);
        if(association.isEmpty()) {
            LOGGER.debug("Adding the new description to the Participant");
            participant1.setDescription(name);
            // Add another description to the Participant
            this.update(participant1);
        }
        else {
            LOGGER.debug("Participant already has the description");
            throw new ResourceAlreadyExistsException("Participant '"+participant1.getParticipantIdsID()+"' already has the description '"+name+"'");
        }
    }

    public void removeDescriptionFromParticipant(String name, Participant1 participant1){

        List<String> association =  repository.findAssociationBetweenParticipantAndDescription(participant1.getParticipantIdsID().toString(), name);
        if(association.isEmpty()) {
            LOGGER.debug("Participant does not has the description");
            throw new ResourceNotFoundException("Description", "Participant", "ID", participant1.getParticipantIdsID().toString());
        }
        else {
            LOGGER.debug("Removing the description from the Participant");
            participant1.getDescription().remove(name);
            this.update(participant1);
        }
    }

    /**
     * Convert a Fraunhofer Participant to a table Participant
     * @param fraun_participant infomodel participant
     * @param certification certification
     * @return database Participant with only the basic components (connections with other tables are done on another method)
     * */
    public Participant1 convertToDatabaseModel(Participant fraun_participant, ParticipantCertification certification) {

        if(fraun_participant==null) return null;
        /**
         * If no LegalName is defined, an exception is thrown.
         * LegalName needs to be unique
         * */
        if(fraun_participant.getLegalName().isEmpty()){
            throw new InvalidValueException("Legal Name", fraun_participant.getLegalName().toString());
        }

        Participant1 participant=new ParticipantBuilder(
                fraun_participant.getId(), fraun_participant.getLegalName(), certification)
                .primarySite(
                        fraun_participant.getPrimarySite()==null ?
                                null : fraun_participant.getPrimarySite().getSiteAddress())
                .corporateEmailAddress(fraun_participant.getCorporateEmailAddress().isEmpty() ?
                        null : fraun_participant.getCorporateEmailAddress())
                .corporateHomepage(fraun_participant.getCorporateHomepage())
                .vatID(fraun_participant.getVatID())
                .registryCourt(fraun_participant.getJurisdiction())
                .legalForm(fraun_participant.getLegalForm())
                .name(
                    fraun_participant.getTitle().isEmpty() ?
                            null :
                            fraun_participant.getTitle().get(0).getValue()) // The App is only getting the first title, so I need to garantee that there is only one title for each participant
                .description(
                        fraun_participant.getDescription().isEmpty() ?
                                null :
                                fraun_participant.getDescription().stream().map(RdfResource::getValue).collect(Collectors.toList()))
                .version(fraun_participant.getVersion())
                .build();
            return participant;

    }

    public Participant1Dto convertToParticipantDto(Participant1 participant1){

        LOGGER.debug("Converting Participant to DTO");

        Set<inesctec.inestec.paris.model.Connector> connectorSetFromParticipant=participant1.getConnectorSet();
        Set<Person> personsSet=participant1.getMemberPerson();
        LogicalConstraint logicalConstraint=participant1.getParticipantRefinement();
        List<Participant1Dto> memberPart=new ArrayList<>();

        Participant1Dto participant1DTO=new Participant1Dto();
        /*if(participant1.getMemberParticipant()!=null){
            participant1.getMemberParticipant().forEach(p -> {
                Optional<Participant1> parentOp=this.findParticipantByParticipantIDSID(p);
                if(parentOp.isPresent())
                    memberPart.add(this.convertToParticipantDto(parentOp.get()));
            });
        }
        participant1DTO.setMemberParticipant(memberPart);*/

        participant1DTO.setParticipantIdsID(participant1.getParticipantIdsID());
        participant1DTO.setLegalName(participant1.getLegalName());
        participant1DTO.setName(participant1.getName());
        participant1DTO.setCorporateEmailAddress(participant1.getCorporateEmailAddress());
        participant1DTO.setCorporateHomepage(participant1.getCorporateHomepage());
        participant1DTO.setVersion(participant1.getVersion());

        if(participant1.getMemberPerson()!=null){
            List<PersonDto> persons=personsSet.stream().map(Person::convertToPersonDto).toList();
            participant1DTO.setMemberPerson(new HashSet<>(persons));
        }

        if(logicalConstraint!=null){
            participant1DTO.setParticipantRefinement(ObjectUtils.defaultIfNull(
                    logicalConstraint.convertToLogicalConstDto(),
                    null));
        }

        if(participant1.getBusinessIdentifier()!=null){
            Set<BusinessIdentifierDto> businessIdentifierDtos=
                    participant1.getBusinessIdentifier().stream().map(bi->bi.convertToBusinessIdentifierDto()).collect(Collectors.toSet());
            participant1DTO.setBusinessIdentifier(businessIdentifierDtos);
        }

        participant1DTO.setParticipantCertification(participant1.getParticipantCertification() == null ?
                null : participant1.getParticipantCertification().convertToCertificationDto());
        participant1DTO.setPrimarySite(participant1.getPrimarySite());
        participant1DTO.setVatID( participant1.getVatID());
        participant1DTO.setRegistryCourt(participant1.getRegistryCourt());
        participant1DTO.setLegalForm(participant1.getLegalForm());
        participant1DTO.setDescription(participant1.getDescription());

        if(connectorSetFromParticipant!=null){
            Set<ConnectorDto> connectorDtoSet=new HashSet<>();
            connectorSetFromParticipant.forEach(connector -> connectorDtoSet.add(connector.convertToConnectorDto()));
            participant1DTO.setConnectorSet(connectorDtoSet);
        }

        return participant1DTO;
    }
}
