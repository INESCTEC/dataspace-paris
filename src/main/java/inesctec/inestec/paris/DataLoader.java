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
package inesctec.inestec.paris;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.model.*;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.ParticipantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@Component
public class DataLoader implements ApplicationRunner {

    private static final Logger LOG =
        LoggerFactory.getLogger(DataLoader.class);
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DataLoader.class);


    EvaluationFacilityService evaluationFacilityService;
    PersonService personService;
    ParticipantService participantService;
    ParticipantCertificationService participantCertificationService;
    LogicalConstraintService logicalConstraintService;
    ConstraintService constraintService;
    ConnectorService connectorService;
    BusinessIdentifierService businessIdentifierService;
    ParticipantUtil participantUtil;
    JsonConverter jsonConverter;


    @Autowired
    public DataLoader(EvaluationFacilityService evaluationFacilityService, PersonService personService,
                      ParticipantService participantService, ConstraintService constraintService,
                      ParticipantCertificationService participantCertificationService,
                      LogicalConstraintService logicalConstraintService, ConnectorService connectorService,
                      BusinessIdentifierService businessIdentifierService, ParticipantUtil participantUtil) {
        this.evaluationFacilityService = evaluationFacilityService;
        this.personService = personService;
        this.participantService = participantService;
        this.participantCertificationService = participantCertificationService;
        this.constraintService = constraintService;
        this.logicalConstraintService = logicalConstraintService;
        this.connectorService=connectorService;
        this.businessIdentifierService=businessIdentifierService;
        this.participantUtil=participantUtil;
        jsonConverter=new JsonConverter();
    }

    @Override
    public void run(ApplicationArguments args) {
        /*System.out.println("EV: "+evaluationFacilityService.size());
        System.out.println("personService: "+personService.size());
        System.out.println("participantService: "+participantService.size());
        System.out.println("certificationService: "+participantCertificationService.size());
        System.out.println("logicalConstraintService: "+logicalConstraintService.size());
        System.out.println("constraintService: "+constraintService.size());
        System.out.println("connectorService: "+connectorService.size());*/
        if(evaluationFacilityService.size()==0 && personService.size()==0
        && participantService.size()==0 && participantCertificationService.size()==0
        && logicalConstraintService.size()==0 && constraintService.size()==0 && connectorService.size()==0
        && businessIdentifierService.size()==0){
            loadDatabase();
        }
    }

    @Transactional
    public void loadDatabase(){
        LOGGER.info("Populating Database...");
        // Create EvaluationFacility Object



        try {
            // Add Producer to Database
            File file = ResourceUtils.getFile("classpath:populatingDatabase/producer.json");
            addParticipant(file);

            // Add Subcontracted to Database
            file = ResourceUtils.getFile("classpath:populatingDatabase/subcontracted.json");
            addParticipant(file);
        }
        catch (FileNotFoundException exception){
            System.err.println("File not found!!");
        }

        LOGGER.info("... Database Populated.");


    }

    private void addParticipant(File file){
        try {
            ParticipantFraunDto participant = jsonConverter.json2Object(ParticipantFraunDto.class,
                    jsonConverter.file2Json(file));

            Participant1 addedParticipant=participantUtil.addParticipant(participant.getParticipant());

            // Handles the Connector
            for (Connector connector : participant.getConnectors())  {
                // In the case that it is a Connector never registered on the Database
                connector.setParticipantSet(addedParticipant);
                connectorService.save(connector);
            }
        }
        catch (MismatchedInputException ex){
            LOGGER.info("Error Deserializing JSON");
            System.err.println(ex);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
