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
import de.fraunhofer.iais.eis.Participant;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.interfaces.IConverter;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.repository.EvaluationFacilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Service
public class EvaluationFacilityService implements IConverter<de.fraunhofer.iais.eis.EvaluationFacility, EvaluationFacility> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationFacilityService.class);

    private final EvaluationFacilityRepository repository;

    public EvaluationFacilityService(EvaluationFacilityRepository repository) {
        this.repository = repository;
    }

    public EvaluationFacility save(EvaluationFacility evaluationFacility){
        return repository.save(evaluationFacility);
    }

    @Transactional
    public EvaluationFacility update(EvaluationFacility evaluationFacility){
        EvaluationFacility evaluationFacilityOnBD = repository.findEvaluationFacilitiesByEvaluationFacilityID(evaluationFacility.getEvaluationFacilityID())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation Facility", "ID", String.valueOf(evaluationFacility.getEvaluationFacilityID())));

        evaluationFacilityOnBD.update(evaluationFacility);
        return repository.save(evaluationFacilityOnBD);
    }

    public void save(List<EvaluationFacility> evaluationFacilityList){
        evaluationFacilityList.stream().forEach(ev -> repository.save(ev));
    }

    public Optional<EvaluationFacility> findByID(Long id){
        return repository.findById(id);
    }

    public List<EvaluationFacility> findAll(){
        List ev = new ArrayList<>();
        repository.findAll().forEach(ev::add);
        return ev;
    }

    public void remove(EvaluationFacility evaluationFacility){
        repository.delete(evaluationFacility);
    }

    public void remove(List<EvaluationFacility> evaluationFacility){
        evaluationFacility.stream().forEach(v -> repository.delete(v));
    }

    public void remove(){
        repository.deleteAll();
    }

    public Integer size(){
        return ((List<EvaluationFacility>) this.findAll()).size();
    }

    public Optional<EvaluationFacility> findByEvaluationFacilityID(URI fa) {
        return repository.findEvaluationFacilitiesByEvaluationFacilityID(fa);
    }

    public static void createAndAssociateEvaluationFacilityWithCertification(de.fraunhofer.iais.eis.ParticipantCertification fran_certification,
                                                                    ParticipantCertification certification) {
        // Creates a new Evaluation Facility using the certification variable
        if(fran_certification.getEvaluationFacility()!=null){
            EvaluationFacility evaluationFacility= new EvaluationFacility(
                    fran_certification.getEvaluationFacility().getId());

            // Set the relations between ParticipantCertification and EvaluationFacility
            associateEvaluationFacilityWithCertification(evaluationFacility, certification);
        }

    }

    public static void associateEvaluationFacilityWithCertification(EvaluationFacility evaluationFacility,
                                                                    ParticipantCertification certification) {
        // Set the relations between ParticipantCertification and EvaluationFacility
        certification.setEvaluationFacility(evaluationFacility);

        Set<ParticipantCertification> certificationSet=new HashSet<>();
        certificationSet.add(certification);
        if(evaluationFacility.getCertifications()!=null) {
            certificationSet.addAll(evaluationFacility.getCertifications());
        }

        evaluationFacility.setCertifications(certificationSet);

    }

    @Override
    public EvaluationFacility convertToDatabaseModel(de.fraunhofer.iais.eis.EvaluationFacility infomodelObject) {
        return new EvaluationFacility(infomodelObject.getId());
    }

    @Override
    public de.fraunhofer.iais.eis.EvaluationFacility convertToInfomodel(EvaluationFacility databaseObject) {
        return new EvaluationFacilityBuilder(databaseObject.getEvaluationFacilityID()).build();
    }
}
