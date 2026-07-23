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

import de.fraunhofer.iais.eis.EvaluationFacilityBuilder;
import de.fraunhofer.iais.eis.ManagedEntity;
import de.fraunhofer.iais.eis.ParticipantCertificationBuilder;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.interfaces.IConverter;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.repository.ParticipantCertificationRepository;
import inesctec.inestec.paris.converter.XMLGregorianCalendarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ParticipantCertificationService implements IConverter<de.fraunhofer.iais.eis.ParticipantCertification, ParticipantCertification> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantCertificationService.class);
    private final ParticipantCertificationRepository repository;

    public ParticipantCertificationService(ParticipantCertificationRepository repository) {
        this.repository = repository;
    }

    public ParticipantCertification save(ParticipantCertification participantCertification){
        return repository.save(participantCertification);
    }

    public void save(List<ParticipantCertification> participantCertificationList){
        participantCertificationList.stream().forEach(v -> {
            //System.out.println(v.toString());
            repository.save(v);
        });
    }

    public ParticipantCertification update(ParticipantCertification participantCertification){
        ParticipantCertification certificationOnDB=this.findCertificationByIDSID(participantCertification.getCertificationIdsID());
        certificationOnDB.update(participantCertification);
        return repository.save(participantCertification);
    }

    @Transactional
    public ParticipantCertification update(ParticipantCertification participantCertification, EvaluationFacility evaluationFacility){
        ParticipantCertification certificationOnDB=this.findCertificationByIDSID(participantCertification.getCertificationIdsID());
        certificationOnDB.update(participantCertification);
        return repository.save(participantCertification);
    }

    public Optional<ParticipantCertification> findByID(Long id){
        return repository.findById(id);
    }

    public Iterable<ParticipantCertification> findAll(){
        return repository.findAll();
    }

    public void remove(ParticipantCertification participantCertification){
        repository.delete(participantCertification);
    }

    public void remove(List<ParticipantCertification> participantCertification){
        participantCertification.stream().forEach(v -> repository.delete(v));
    }

    public void remove(){
        repository.deleteAll();
    }

    public Integer size(){
        return ((List<ParticipantCertification>) this.findAll()).size();
    }

    public ParticipantCertification findParticipantCertificationByID(Long id){
        return repository.findParticipantCertificationByParticipantCertificationID(id).orElseThrow(() ->
            new ResourceNotFoundException("Certification"));
    }

    public ParticipantCertification findCertificationByIDSID(URI id){
        try {
            return repository.findFirstByCertificationIdsIDOrderByVersionDesc(id).orElseThrow(() ->
                new ResourceNotFoundException("Certification"));
        }catch (Exception exception){
            System.err.println("Exception: "+exception.toString());
            // In case the version is null
            return repository.findParticipantCertificationByCertificationIdsID(id).orElseThrow(() ->
                new ResourceNotFoundException("Certification"));
        }

    }

    public Optional<ParticipantCertification> findOptionalCertificationByIDSID(URI id){
        return repository.findFirstByCertificationIdsIDOrderByVersionDesc(id);
    }

    /**
     * Converts a DB Certification into a Infomodel Certification
     * @param databaseObject database Participant
     * @return certification in the infomodel format
     * */
    @Override
    public de.fraunhofer.iais.eis.ParticipantCertification convertToInfomodel(ParticipantCertification databaseObject){
        XMLGregorianCalendarConverter xmlGregorianCalendarConverter=new XMLGregorianCalendarConverter();
        return new ParticipantCertificationBuilder(databaseObject.getCertificationIdsID())
                ._certificationLevel_(databaseObject.getCertificationLevel())
                ._lastValidDate_( databaseObject.getLastValidDate()==null ? null:
                        xmlGregorianCalendarConverter.convertToXMLGregorianCalendar(databaseObject.getLastValidDate()))
                ._membershipEnd_(
                    databaseObject.getMembershipEnd()==null ?
                                null :
                                xmlGregorianCalendarConverter.convertToXMLGregorianCalendar(databaseObject.getMembershipEnd()))
                ._description_(
                    databaseObject.getDescription()!=null ?
                                List.of(new TypedLiteral(databaseObject.getDescription())) :
                                null)
                ._title_(databaseObject.getTitle()!=null ?
                        List.of(new TypedLiteral(databaseObject.getTitle())) :
                        null)
                ._evaluationFacility_(
                    databaseObject.getEvaluationFacility()!=null ?
                                new EvaluationFacilityBuilder(databaseObject.getEvaluationFacility().getEvaluationFacilityID())
                                        .build() : null)
                ._version_(databaseObject.getVersion())
                .build();
    }


    /**
     * Converts a Infomodel Certification into a DB Certification
     * @param infomodelObject infomodel Certification
     * @return certification in the database format
     * */
    @Override
    public ParticipantCertification convertToDatabaseModel(de.fraunhofer.iais.eis.ParticipantCertification infomodelObject) {

        if(infomodelObject==null) throw new InvalidValueException("Participant", "Certification", null);

        LOGGER.debug("Creating Certification...");

        // Add Participant Certification (Relationship is OneToOne)
        ParticipantCertification certification=new inesctec.inestec.paris.builder.ParticipantCertificationBuilder(infomodelObject.getId().toString())
            .version(infomodelObject.getVersion())
            .lastValidDate(infomodelObject.getLastValidDate())
            .membershipEnd(infomodelObject.getMembershipEnd())
            .description((infomodelObject.getDescription()==null || infomodelObject.getDescription().isEmpty()) ?
                null : infomodelObject.getDescription().stream().map(d->d.getValue()).collect(Collectors.joining()))
            .title((infomodelObject.getTitle()==null || infomodelObject.getTitle().isEmpty()) ?
                null : infomodelObject.getTitle().stream().map(d->d.getValue()).collect(Collectors.joining()))
            .build();

        try{
            // If getCertificationLevel().getId() is Invalid, the Fraunhofer makes it null
            if(infomodelObject.getCertificationLevel().getId()!=null){
                certification.setCertificationLevel(infomodelObject.getCertificationLevel());
            }
        }
        catch (RuntimeException ex){
            LOGGER.error("Certification Level has an invalid value...");
            LOGGER.error("... It cannot be 'null'");
            throw new InvalidValueException("Certification Level", null);
        }

        // Handles the Evaluation Facility
        EvaluationFacilityService.createAndAssociateEvaluationFacilityWithCertification(infomodelObject, certification);

        return certification;
    }
}
