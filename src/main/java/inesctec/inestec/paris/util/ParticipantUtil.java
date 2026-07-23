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
package inesctec.inestec.paris.util;

import de.fraunhofer.iais.eis.Participant;
import inesctec.inestec.paris.builder.LogicalConstraintBuilder;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.dto.LogicalConstraintDto;
import inesctec.inestec.paris.dto.Participant1Dto;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.dto.PersonDto;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.exceptions.ResourceAlreadyExistsException;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.model.*;
import inesctec.inestec.paris.service.*;
import org.apache.commons.lang3.ObjectUtils;
import org.assertj.core.api.ProxifyMethodChangingTheObjectUnderTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class ParticipantUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantUtil.class);

    @Qualifier("ActivePrf")
    @Autowired private String activeProfile;

    @Autowired private EvaluationFacilityService evaluationFacilityService;
    @Autowired private PersonService personService;
    @Autowired private ParticipantService participantService;
    @Autowired private ParticipantCertificationService participantCertificationService;
    @Autowired private BusinessIdentifierService businessIdentifierService;
    @Autowired private ConnectorService connectorService;
    @Autowired private ConstraintService constraintService;
    @Autowired private LogicalConstraintService logicalConstraintService;

    private ParticipantConverter participantConverter;

    inesctec.inestec.paris.model.LogicalConstraint logicalConstraint;
    //List<inesctec.inestec.paris.model.Constraint> constraints;

    Set<inesctec.inestec.paris.model.BusinessIdentifier> businessIdentifiers;

    boolean partIsInserted;
    boolean hasEvaluationFacility;
    ParticipantCertification certification;

    /**
     * Chck if this is an insert or an "update"
     *
     * */
    @Transactional(noRollbackFor = {ResourceAlreadyExistsException.class, InvalidValueException.class})
    public Participant1 addParticipant(Participant fraun_participant) throws Exception{
        participantConverter=new ParticipantConverter(this.participantService, this.participantCertificationService, this.logicalConstraintService, this.personService, this.businessIdentifierService);
        Optional<Participant1> participant1Optional=participantService.findParticipantByParticipantIDSID(fraun_participant.getId());
        if(participant1Optional.isPresent()){
            // Check if the version is different
            if(!Objects.equals(participant1Optional.get().getVersion(), fraun_participant.getVersion())){
                // If it is different
                LOGGER.info("It is an update.");
                LOGGER.info("A new version of the Participant is being created ...");
                 return insertNewVersionOfParticipant(fraun_participant);

            }
            else {
                // If the Participant version is not different, throw exception
                LOGGER.error("Participant already exists on DB");
                throw new ResourceAlreadyExistsException("Participant", "ID", participant1Optional.get().getParticipantIdsID().toString());
            }
        }
        else
        {
            LOGGER.info("Participant "+ fraun_participant.getId()+" is being created ...");
             return insertParticipant(fraun_participant);

        }
    }
    // I don't need to Rollback because of these exceptions, because I'm only checking the database, not inserting anything
    // All the checks that could rollback, I'm verifying beforehand
    // https://reflectoring.io/spring-transactions-and-exceptions/
    @Transactional(noRollbackFor = {ResourceAlreadyExistsException.class, InvalidValueException.class})
    public Participant1 insertParticipant(Participant fraun_participant) throws Exception {
        boolean hasRefinement;
        boolean hasBusinessIdentifier;
        partIsInserted=false;
        hasEvaluationFacility= false;

        //ParticipantCertification certification;
        try {
            certification=participantCertificationAndEvaluationFacility(fraun_participant);
        }
        catch (InvalidValueException exception){
            throw new Exception(exception.getMessage());
        }

        AtomicReference<Participant1> participant = new AtomicReference<>();
        participantService.findParticipantByParticipantIDSID(fraun_participant.getId()).ifPresentOrElse(
                (v) -> {
                    // If the Participant version is not different, throw exception
                    LOGGER.error("Participant already exists on DB");
                    throw new ResourceAlreadyExistsException("Participant", "ID", v.getParticipantIdsID().toString());

                },
                () -> {
                    LOGGER.info("Participant is being created ...");
                    participant.set(participantService.convertToDatabaseModel(fraun_participant, certification));

                });

        if (fraun_participant.getParticipantRefinement()!=null){
            hasRefinement=true;
            logicalConstraint=logicalConstraintService.convertToDatabaseModel(fraun_participant.getParticipantRefinement(), participant.get());
            participant.get().setParticipantRefinement(logicalConstraint);
            //logicalConstraint.getConstraints();
        } else {
            hasRefinement = false;
        }



        // TODO -> handle the hierarchy
     /* AtomicReference<Participant1> parentParticipant=new AtomicReference<>();
      if(!fraun_participant.getMemberParticipant().isEmpty()){
        LOGGER.info("Creating the Children Hierarchy ...");
        List<Participant1> memberParticipantList=new ArrayList<>();

        fraun_participant.getMemberParticipant().stream().forEach(
                v-> {
                  Participant1 memberParticipant=participantConverter.convertToDatabaseParticipant(v);
                  // Find the referenced Participant on the DB
                  parentParticipant.set(participantService
                          .findParticipantByParticipantIDSID(memberParticipant.getParticipantIdsID())
                          .orElseThrow(() -> {
                                    LOGGER.error("Participant referenced on the memberParticipant does not exist.");
                                    throw new ResourceNotFoundException("Member Participant", "ID", v.getId().toString());
                                  }

                          ));

                  // Add the new Participant as a Child of the ReferencedParticipant
                  //parentParticipant.get().addChild(participant.get());
                  *//*participantService.update(parentParticipant.get());*//*

                  // Add the converted member participant to the list of converted member participants
                  memberParticipantList.add(memberParticipant);
                }
        );
        // Set the Member Participant of the new Participant as the referenced Participant
        //participant.get().setMemberParticipant(memberParticipantList);
        //System.err.println(participant.get().getMemberParticipant().toJson());
      }*/


        certification.setParticipant(participant.get());
        participant.get().setParticipantCertification(certification);



        if (fraun_participant.getBusinessIdentifier()!=null){
            hasBusinessIdentifier=true;
            fraun_participant.getBusinessIdentifier().forEach(p->{
                businessIdentifierService.findBusinessIdentifierByBusinessIdentifierIdsID(p.getId().toString()).ifPresentOrElse(
                        (v) -> {
                            LOGGER.error("Business Identifier with ID already exists on DB", ResourceAlreadyExistsException.class);
                            throw new ResourceAlreadyExistsException("Business Identifier", "ID", v.getBusinessIdentifierIdsID().toString());
                        },
                        () -> {
                            LOGGER.debug("Business Identifier is being created ...");
                            businessIdentifiers=businessIdentifierService.convertToDatabaseModel(
                                    fraun_participant.getBusinessIdentifier());
                            participant.get().setBusinessIdentifier(businessIdentifiers);
                            businessIdentifiers.forEach(bi -> bi.setParticipant(participant.get()));
                        });
            });
        } else {
            hasBusinessIdentifier = false;
            participant.get().setBusinessIdentifier((Set<BusinessIdentifier>) null);
        }

        // Get List of MemberPersons
        if(fraun_participant.getMemberPerson()!=null &&
                !fraun_participant.getMemberPerson().isEmpty()){
            Set<inesctec.inestec.paris.model.Person> personsToAdd=new HashSet<>();
            Set<inesctec.inestec.paris.model.Person> personsToUpdate=new HashSet<>();
            fraun_participant.getMemberPerson()
                    .forEach(p-> {
                                /*
                                 * If a Person already exists, update person
                                 * */
                                personService.findPersonByPersonIdsID(String.valueOf(p.getId())).ifPresentOrElse(
                                        (personOnDB) -> {
                                            LOGGER.debug("Person with ID "+personOnDB.getPersonIdsID() +" already exists");
                                            LOGGER.debug("The Participant will be added to this person");

                                            // Add Person to list of Persons
                                            if (!"dev".equals(activeProfile)) {
                                                // This can only be made with PostGRES. The test database throws detached exception
                                                personOnDB.setParticipant(participant.get());
                                            }
                                            personsToUpdate.add(personOnDB);
                                        },
                                        () -> {
                                            LOGGER.debug("Person is being created...", ParticipantUtil.class);
                                            Person personToAdd=personService.convertToDatabaseModel(p);
                                            personToAdd.setParticipant(participant.get());
                                            personsToAdd.add(personToAdd);
                                        });
                            }
                    );
            // The Person is added when adding the Participant (because of the Cascade.ALL)
            insertParticipantAndPersonOnDB(participant.get(), personsToUpdate, personsToAdd);

        }

        LOGGER.debug("Adding the Certification to the Database...");


        if(partIsInserted==false){
            insertParticipant(participant.get());
        }


        LOGGER.debug("Certification created!!", ParticipantUtil.class);

        LOGGER.info("Participant was created successfully!!");
        return participant.get();
    }


    private Participant1 insertParticipant(Participant1 child/*, AtomicReference<Participant1> parent*/) {

        // If EV exists, saves it to Database before the Participant
        if(hasEvaluationFacility) {
            LOGGER.debug("Adding Evaluation Facility to Database...");
            evaluationFacilityService.save(certification.getEvaluationFacility());
            LOGGER.debug("... Evaluation Facility Added.");
        }
        LOGGER.debug("Adding Participant to Database...");

        // Participant already inserts Certifications, Persons (not update) and Refinement
        Participant1 insertedParticipant=participantService.save(child);
        partIsInserted=true;
        return insertedParticipant;
    /*if(parent.get()!=null){
      LOGGER.info("Adding the Child to the Database...");

      participantService.save(child);
      LOGGER.info("... It finished.");
      partIsInserted=true;
    }
    else {
      LOGGER.info("Adding Participant to Database...");
      participantService.save(child);
      partIsInserted=true;
    }*/

    }

    private void insertParticipantAndPersonOnDB(Participant1 participant, Set<Person> personsToUpdate, Set<Person> personsToAdd) {

        Participant1 insertedParticipant=insertParticipant(participant);

        // Updates the relationship in the test environment (dev)
        if ("dev".equals(activeProfile)) {
            LOGGER.debug("Adding Person to Database...");
            for (Person person : personsToUpdate) {
                // Handles bidirectional relationship. Adds a new row to the 'participant_person' table
                person.setParticipant(insertedParticipant);
                personService.save(person);
                LOGGER.debug("...Saved");
            }
        }




        LOGGER.debug("... Person added to Database.");
    }

    /*
     *  Create a:
     *       - ParticipantCertification and
     *       - Evaluation Facility
     * */
    private ParticipantCertification participantCertificationAndEvaluationFacility(Participant fraun_participant) throws InvalidValueException {

        // Add Participant Certification (Relationship is OneToOne)
        ParticipantCertification certification=participantCertificationService
                .convertToDatabaseModel(fraun_participant.getParticipantCertification());

        /*
         * Checks if the Evaluation Facility exists
         * If it not exists, create one
         */
        if(fraun_participant.getParticipantCertification().getEvaluationFacility()!=null){
            LOGGER.debug("Evaluation Facility is defined");

            // Check If evaluationFacility exists
            evaluationFacilityService.findByEvaluationFacilityID(fraun_participant.getParticipantCertification()
                            .getEvaluationFacility().getId())
                    .ifPresentOrElse(
                            (v) ->{
                                // If it exists, the certification will be overriden (by default, a new Evaluation Facility is created)
                                LOGGER.debug("Evaluation Facility exists on DB, and it will be used");
                                EvaluationFacilityService.associateEvaluationFacilityWithCertification(v, certification);
                            },
                            ()-> {
                                // This case was already hadled by default when converting a InfoModel certification into a DB certification
                                LOGGER.debug("Evaluation Facility does not exist on the Database...");
                                LOGGER.debug("...One will be created...");

                                LOGGER.debug("Evaluation Facility created!!");

                            });
            hasEvaluationFacility=true;
        }
        else {
            LOGGER.debug("No Evaluation Facility is defined.");
        }

        return certification;
    }

    @Transactional
    public void removeParticipant(Participant1 participantToRemove) {

        LOGGER.info("Removing Participant...");

        // These are the rows from table 'participant_person'
        List<Person> associatedPersonsToRemove=personService.findPersonsByParticipant(participantToRemove);
        LOGGER.debug("... Removing the Participant from the Person it is associated with... ");
        associatedPersonsToRemove.forEach(p->{
            LOGGER.debug(p.getPersonIdsID().toString());
            p.setParticipant((Set<Participant1>) null);
            personService.update(p);
        });

        // These are the rows from table 'participant_connector'
        List<Connector> associatedConnectorsToRemove=connectorService.findConnectorsFromParticipant(participantToRemove);
        LOGGER.debug("... Removing the Participant from the Connector it is associated with... ");
        associatedConnectorsToRemove.forEach(conn->{
            LOGGER.debug(conn.getConnectorID().toString());
            conn.removeParticipant(participantToRemove);
            connectorService.removeParticipant(conn);
        });

        LOGGER.debug("... Remove Participant... ");
        participantService.remove(participantToRemove);
        LOGGER.info("... Participant removed!!");
    }


    @Transactional
    public Participant1 insertNewVersionOfParticipant(Participant newVersionOfParticipant) {
        Participant1 newParticipant=participantConverter.convertToDatabaseParticipant(newVersionOfParticipant);

        // Business identifiers - It creates a new entry with a different Participant

        // Update member persons
        Set<Person> newParticipantMemberPerson = newParticipant.getMemberPerson();

        if (newParticipantMemberPerson!=null) {

            List<Person> personsOnDBFromParticipant=personService.findPersonByParticipantIDS_ID(newParticipant.getParticipantIdsID());

            // If the Persons on DB are equal to the ones to insert, update the newParticipant to use the Person pn DB (for the save to handle the person update)
            if (CompareUtil.comparePersonsDTO(newParticipantMemberPerson, new HashSet<>(personsOnDBFromParticipant))) {
                newParticipant.setMemberPerson(new HashSet<>(personsOnDBFromParticipant));
            } // The participant.save() handles the else
        }

        // Update ParticipantCertification. If present, a new Certification is always inserted
        certification=newParticipant.getParticipantCertification(); //This allows the insertParticipant to usr the variable
        if (certification != null && certification.getEvaluationFacility()!=null) {
            evaluationFacilityService.findByEvaluationFacilityID(certification.getEvaluationFacility().getEvaluationFacilityID())
                .ifPresentOrElse(
                    (v) -> newParticipant.getParticipantCertification().setEvaluationFacility(v),
                    () ->  hasEvaluationFacility = true);
               // The insert will add the new EV

        }

        // In case the new Participant has a Refinement
        if(newVersionOfParticipant.getParticipantRefinement()!=null){
            logicalConstraintService.findLogicalConstraintBy_IDS_ID(newVersionOfParticipant.getParticipantRefinement().getId()).ifPresent(
                (logicalConstraintOnDB) -> {
                    // If the Logical Constraint in the Database is equal to the one to insert, add a new version of it (checks the DTOs because of the database ID)
                    if(logicalConstraintOnDB.convertToLogicalConstDto().equals(newParticipant.getParticipantRefinement().convertToLogicalConstDto())){

                        LogicalConstraint updatedRefinement=logicalConstraintOnDB.deepCopy();
                        updatedRefinement.setVersion(newVersionOfParticipant.getVersion());
                        updatedRefinement.getConstraints().forEach(constraint -> {
                            constraint.setVersion(newVersionOfParticipant.getVersion());
                        });
                        newParticipant.setParticipantRefinement(updatedRefinement);

                    }
                    //else is already handled
                    // If there are no Logical Constraint with that ID on the DB, the participantSave will habdle this
                }
            );


        }

        insertParticipant(newParticipant);

        return newParticipant;
    }


    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = {ResourceAlreadyExistsException.class, InvalidValueException.class})
    public void addParticipant(ParticipantFraunDto participant) throws Exception {
        participant.convertConnectorSetToMutable();

        Participant1 addedParticipant=this.addParticipant(participant.getParticipant());

        // Handles the Connector
        for (Connector connector : participant.getConnectors())  {
            LOGGER.debug("... Connector with ID "+ connector.getConnectorID());


            Optional<Connector> connectorOnDB=connectorService.findConnectorByConnectorIdsID(
                String.valueOf(connector.getConnectorID()));

            // If the Connector already exists, update the association
            if(connectorOnDB.isPresent()) {
                LOGGER.debug("... Connector already exists ...");

                Participant1 participantOnDB=participantService.findParticipant1ByParticipantIdsID(participant.getParticipant().getId()).get();
                // Add Participant to the Connector
                LOGGER.debug("... Associating Connector With Participant ...");
                connectorOnDB.get().setParticipantSet(participantOnDB);
                participantOnDB.setConnectorSet(connectorOnDB.get());

                connectorService.update(connectorOnDB.get());

                LOGGER.debug("... Associated.");

            }
            else {
                LOGGER.debug("... Saving Connector ..."+ connector.getConnectorID());
                // In the case that it is a Connector never registered on the Database
                connector.setParticipantSet(addedParticipant);
                connectorService.save(connector);
                // Retrieve, from the Database, the updated list of Connectors associated with a Participant
                addedParticipant.setConnectorSet(
                    new HashSet<>(connectorService.findConnectorsFromParticipant(addedParticipant)));

                LOGGER.debug("... Associating Connector With Participant ..."+ connector.getConnectorID());
                LOGGER.debug("... Associated.");
            }
        };
    }
}
