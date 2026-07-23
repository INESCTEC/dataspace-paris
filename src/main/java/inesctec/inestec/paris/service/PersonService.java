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

import de.fraunhofer.iais.eis.PersonBuilder;
import inesctec.inestec.paris.dto.PersonDto;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.interfaces.IConverter;
import inesctec.inestec.paris.model.EvaluationFacility;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.Person;
import inesctec.inestec.paris.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Service
public class PersonService implements IConverter<de.fraunhofer.iais.eis.Person, Person> {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    public Person save(Person person){
        return repository.save(person);
    }

    public void save(List<Person> personList){
        personList.stream().forEach(v -> repository.save(v));
    }

    public Person update(Person person){
        Person personOnBD = repository.findPersonByPersonIdsID(person.getPersonIdsID())
                .orElseThrow(() -> new ResourceNotFoundException("Person", "ID", String.valueOf(person.getPersonIdsID())));

        personOnBD.update(person);
        return repository.save(personOnBD);
    }

    public Optional<Person> findByID(Long id){
        return repository.findById(id);
    }

    public List<Person> findAll(){
        List<Person> list=new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }

    public void remove(Person person){
        repository.delete(person);
    }

    public void remove(List<Person> person){
        person.stream().forEach(v -> repository.delete(v));
    }

    public void remove(){
        repository.deleteAll();
    }

    public Integer size(){
        return ((List<Person>) this.findAll()).size();
    }

    @Transactional
    public Optional<Person> findPersonByPersonIdsID(String ids_id){
        return repository.findPersonByPersonIdsID(URI.create(ids_id));
    }

    public List<Person> findPersonsByParticipant(Participant1 participant){
        return repository.findPersonByParticipant(participant);
    }

    public List<Person> findPersonByParticipantIDS_ID(URI participantIds_id){
        return repository.findPersonByParticipantIDS_ID(participantIds_id.toString());
    }

    
    public Set<Person> convertToDatabaseModel(List<de.fraunhofer.iais.eis.Person> personsList){
        // If it is null, it returns an empty set
        if(personsList==null) return new HashSet<>();

        Set<Person> persons=new HashSet<>();
        personsList.forEach(p-> {
                    persons.add(convertToDatabaseModel(p));
                }
        );
        return persons;
    }

    

    @Override
    public Person convertToDatabaseModel(de.fraunhofer.iais.eis.Person infomodelObject) {
        return new inesctec.inestec.paris.builder
            .PersonBuilder()
            .familyName(infomodelObject.getFamilyName())
            .givenName(infomodelObject.getGivenName())
            .email((infomodelObject.getEmailAddress()==null || infomodelObject.getEmailAddress().isEmpty()) ?
                new ArrayList<>() : infomodelObject.getEmailAddress())
            .homepage(infomodelObject.getHomepage())
            .phoneNumber((infomodelObject.getPhoneNumber()==null || infomodelObject.getPhoneNumber().isEmpty()) ?
                new ArrayList<>() : infomodelObject.getPhoneNumber())
            .ids_id(infomodelObject.getId().toString())
            .build();
    }

    @Override
    public de.fraunhofer.iais.eis.Person convertToInfomodel(Person databaseObject) {
        if(databaseObject.getPersonIdsID()==null){
            return new PersonBuilder()
                ._familyName_(databaseObject.getFamilyName())
                ._givenName_(databaseObject.getGivenName())
                ._emailAddress_(databaseObject.getEmail())
                ._homepage_(databaseObject.getHomepage())
                ._phoneNumber_(databaseObject.getPhoneNumber())
                .build();
        }
        return new PersonBuilder(databaseObject.getPersonIdsID())
            ._familyName_(databaseObject.getFamilyName())
            ._givenName_(databaseObject.getGivenName())
            ._emailAddress_(databaseObject.getEmail())
            ._homepage_(databaseObject.getHomepage())
            ._phoneNumber_(databaseObject.getPhoneNumber())
            .build();
    }

    public List<de.fraunhofer.iais.eis.Person> convertToInfomodel(Set<Person> personsList){
        List<de.fraunhofer.iais.eis.Person> persons=new ArrayList<>();
        personsList.forEach(p-> {
                persons.add(convertToInfomodel(p));
            }
        );
        return persons;
    }

}
