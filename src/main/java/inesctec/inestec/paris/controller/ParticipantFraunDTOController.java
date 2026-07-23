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
package inesctec.inestec.paris.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.dto.ConnectorWithListOfParticipantsDto;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.exceptions.ResourceAlreadyExistsException;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.json.ErrorMessage;
import inesctec.inestec.paris.json.ListOfParticipantsInsertedOrNotMessage;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.CommunicationUtil;
import inesctec.inestec.paris.util.MyJacksonObjectMapper;
import inesctec.inestec.paris.util.ParticipantUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@ApiResponse
@Tag(name = "4. Participant And Connector", description = "API for Information regarding the Complete Participant")
@RequestMapping("/api")
public class ParticipantFraunDTOController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantFraunDTOController.class);

    private final ParticipantService service;
    private final ParticipantCertificationService certificationService;

    private final ConnectorService connectorService;

    private final JsonConverter jsonConverter;
    private final ParticipantConverter participantConverter;

    private ParticipantUtil participantUtil;

    private EvaluationFacilityService evaluationFacilityService;
    private PersonService personService;
    private ConstraintService constraintService;
    private LogicalConstraintService logicalConstraintService;
    private BusinessIdentifierService businessIdentifierService;

    public ParticipantFraunDTOController(ParticipantService service, ParticipantCertificationService certificationService, ConnectorService connectorService, JsonConverter jsonConverter, EvaluationFacilityService evaluationFacilityService, PersonService personService, ParticipantService participantService, ConstraintService constraintService,
                                         LogicalConstraintService logicalConstraintService, BusinessIdentifierService businessIdentifierService, ParticipantUtil participantUtil) {
        this.service = service;
        this.certificationService = certificationService;
        this.connectorService = connectorService;
        this.jsonConverter = jsonConverter;
        this.evaluationFacilityService = evaluationFacilityService;
        this.personService = personService;
        this.constraintService = constraintService;
        this.logicalConstraintService = logicalConstraintService;
        this.businessIdentifierService = businessIdentifierService;
        this.participantUtil = participantUtil;
        this.participantConverter = new ParticipantConverter(this.service, this.certificationService, this.logicalConstraintService, this.personService, this.businessIdentifierService);
    }

    /**
     * Insert Participant
     *
     * @return Participant Self-description or error response.
     */
    @PostMapping(path = "participants/dto", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE },
        produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(
        summary = "Add Participant",
        responses = {
            @ApiResponse(responseCode = "201", description ="Item created",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParticipantFraunDto.class))}),
            @ApiResponse(responseCode = "400", description ="Bad input parameters"),
            @ApiResponse(responseCode = "409", description ="The Participant already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @RequestBody(required = true, description = "The participant",
        content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ParticipantFraunDto.class)),
            @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                schema = @Schema(type = "string"),
                examples = @ExampleObject(value = "....."))
        }
    )
    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<?> addParticipant(InputStream inputStream, final HttpServletRequest request) {

        boolean isNotList=false;
        MyJacksonObjectMapper objectMapper = new MyJacksonObjectMapper();


        LOGGER.info("Adding a Participant with Connectors!");

        JsonNode json;

        try {
            json = jsonConverter.inputStream2Json(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<ParticipantFraunDto> participantList = new ArrayList<>();
        ParticipantFraunDto participant = null;
        try {
            // Sees if it is only one participant
            participant=CommunicationUtil.deserialize(json.toString(), ParticipantFraunDto.class);
            isNotList=true;
        }catch (NullPointerException exception){
            // In case it is a json file with multiple Participants
            CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, ParticipantFraunDto.class);
            try {
                participantList = jsonConverter.json2ListOfObject(json, collectionType);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // TODO: se o ID da pessoa já existir na BD, o código retorna um erro sem explicação para o Swagger
        if(isNotList){
            LOGGER.debug("Is inserting one Participant");
            try {
                this.insertPartWithConnectorSet(participant);
                LOGGER.info("Inserted Participant with ID '"+participant.getParticipant().getId()+"'!");
                return ResponseEntity.ok(participant.toJson());
            } catch (Exception e) {
                System.err.println(e.toString());
                if(e instanceof InvalidValueException){
                    return new ResponseEntity<>(
                        new ErrorMessage("Participant is invalid", e.getMessage()).toJson(),
                        HttpStatus.valueOf(400));
                }
                if(e instanceof ResourceAlreadyExistsException){
                    return new ResponseEntity<>(
                        new ErrorMessage( e.getMessage()).toJson(),
                        HttpStatus.valueOf(409));
                }
                return new ResponseEntity<>(
                    new ErrorMessage( e.getMessage()).toJson(),
                    HttpStatus.valueOf(500));
            }
        }

        //TODO 05/11/2024 - remove the participant_id from the Swagger response
        List<String> resourceAlreadyExistsException_list=new ArrayList<>();
        List<String> successfullyInsertedParticipants=new ArrayList<>();
        for(ParticipantFraunDto part : participantList){
            try {
                this.insertPartWithConnectorSet(part);
                successfullyInsertedParticipants.add(part.getParticipant().getId().toString());
                LOGGER.info("Inserted Participant with ID '"+part.getParticipant().getId()+"'!");
                Optional<Connector> connectorOnDB=connectorService.findConnectorByConnectorIdsID(
                    String.valueOf(part.getConnectors().stream().findFirst().get().getConnectorID()));

                // If the Connector already exists, update the association
                if(connectorOnDB.isPresent()) {
                    ConnectorWithListOfParticipantsDto responseToReturn= new ConnectorWithListOfParticipantsDto(
                        service.findParticipantByParticipantIDSID(part.getParticipant().getId()).get().getConnectorSet().stream().findFirst().get());

                }
                } catch (Exception e) {
                System.err.println(e.toString());
                if(e instanceof InvalidValueException){
                    return new ResponseEntity<>(
                        new ErrorMessage("Participant is invalid", e.getMessage()).toJson(),
                        HttpStatus.valueOf(400));
                }
                if(e instanceof ResourceAlreadyExistsException){
                    resourceAlreadyExistsException_list.add(part.getParticipant().getId().toString());

                }
            }
        };

        if(resourceAlreadyExistsException_list.isEmpty()){
            return ResponseEntity.ok(json.toPrettyString());
        }else {
            return ResponseEntity.ok(new ListOfParticipantsInsertedOrNotMessage( successfullyInsertedParticipants,
                resourceAlreadyExistsException_list).toJson());
        }

    }

    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = {ResourceAlreadyExistsException.class, InvalidValueException.class})
    public void insertPartWithConnectorSet(ParticipantFraunDto participant) throws Exception {
        participantUtil.addParticipant(participant);
    }

}
