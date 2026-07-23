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

import de.fraunhofer.iais.eis.BusinessIdentifierBuilder;
import inesctec.inestec.paris.interfaces.IConverter;
import inesctec.inestec.paris.model.BusinessIdentifier;
import inesctec.inestec.paris.repository.BusinessIdentifierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Service
public class BusinessIdentifierService implements IConverter<de.fraunhofer.iais.eis.BusinessIdentifier, BusinessIdentifier> {

    private final BusinessIdentifierRepository repository;

    public BusinessIdentifierService(BusinessIdentifierRepository repository) {
        this.repository = repository;
    }

    public BusinessIdentifier save(BusinessIdentifier BusinessIdentifier){
        return repository.save(BusinessIdentifier);
    }

    public void save(List<BusinessIdentifier> BusinessIdentifierList){
        BusinessIdentifierList.stream().forEach(v -> repository.save(v));
    }

    public Optional<BusinessIdentifier> findByID(Long id){
        return repository.findById(id);
    }

    public List<BusinessIdentifier> findAll(){
        List<BusinessIdentifier> bi=new ArrayList<>();
        repository.findAll().forEach(bi::add);
        return bi;
    }

    public void remove(BusinessIdentifier businessIdentifier){
        repository.delete(businessIdentifier);
    }

    public void remove(List<BusinessIdentifier> businessIdentifier){
        businessIdentifier.stream().forEach(v -> repository.delete(v));
    }

    public void remove(){
        repository.deleteAll();
    }

    public Integer size(){
        return ((List<BusinessIdentifier>) this.findAll()).size();
    }

    @Transactional
    public Optional<BusinessIdentifier> findBusinessIdentifierByBusinessIdentifierIdsID(String ids_id){
        return repository.findBusinessIdentifierByBusinessIdentifierIdsID(URI.create(ids_id));
    }

    @Transactional
    public Optional<BusinessIdentifier> findBusinessIdentifierByBusinessIdentifierIdsID(URI ids_id){
        return repository.findBusinessIdentifierByBusinessIdentifierIdsID(ids_id);
    }

    @Override
    public BusinessIdentifier convertToDatabaseModel(de.fraunhofer.iais.eis.BusinessIdentifier infomodelObject){
        return new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                .identifierNumber(infomodelObject.getIdentifierNumber())
                .identifierSystem(infomodelObject.getIdentifierSystem())
                .ids_ID(infomodelObject.getId())
                .build();
    }

    @Override
    public de.fraunhofer.iais.eis.BusinessIdentifier convertToInfomodel(BusinessIdentifier databaseObject) {
        if(databaseObject.getBusinessIdentifierIdsID()==null){
            return new BusinessIdentifierBuilder()
                ._identifierNumber_(databaseObject.getIdentifierNumber())
                ._identifierSystem_(databaseObject.getIdentifierSystem())
                .build();
        }
        return new BusinessIdentifierBuilder(databaseObject.getBusinessIdentifierIdsID())
            ._identifierNumber_(databaseObject.getIdentifierNumber())
            ._identifierSystem_(databaseObject.getIdentifierSystem())
            .build();
    }

    public Set<BusinessIdentifier> convertToDatabaseModel(List<de.fraunhofer.iais.eis.BusinessIdentifier>businessIdentifiersList){
        // If it is null, it returns an empty set
        if(businessIdentifiersList==null) return new HashSet<>();

        Set<BusinessIdentifier> businessIdentifiers=new HashSet<>();
        businessIdentifiersList.forEach(p-> {
            businessIdentifiers.add(convertToDatabaseModel(p));
                }
        );
        return businessIdentifiers;
    }

    public List<de.fraunhofer.iais.eis.BusinessIdentifier> convertToInfomodel(Set<BusinessIdentifier> businessIdentifiersList){
        List<de.fraunhofer.iais.eis.BusinessIdentifier> BusinessIdentifiers=new ArrayList<>();
        businessIdentifiersList.forEach(p-> {
            BusinessIdentifiers.add(convertToInfomodel(p));
                        }
                );
        return BusinessIdentifiers;
    }
}
