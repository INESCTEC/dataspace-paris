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
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import inesctec.inestec.paris.dto.ConnectorDto;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.exceptions.ResourceAlreadyExistsException;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.json.ErrorMessage;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.util.CommunicationUtil;
import inesctec.inestec.paris.util.ParticipantUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
@ApiResponse
@Tag(name = "2. Participants", description = "API for Information regarding the Participants")
@RequestMapping("/api")
public class ParticipantController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantController.class);

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

    public ParticipantController(ParticipantService service, ParticipantCertificationService certificationService, ConnectorService connectorService, JsonConverter jsonConverter, EvaluationFacilityService evaluationFacilityService, PersonService personService, ParticipantService participantService, ConstraintService constraintService,
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
     * Get a List of Participants or a Participant by ID
     * @param id optional. The Participant ID
     *
     * @return List of Participants or error response.
     */
    @GetMapping(value = {"participants"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a list of all Participants", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParticipantFraunDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResponseEntity<?> getListOfParticipants(
        @Parameter(description = "The element uri (Participant).")
        @RequestParam(value = "participantId", required = false) final URI id
    ) {
        // No ID was defined. Therefore, print all participants
        if(id==null){
            List<ParticipantFraunDto> participants=new ArrayList();
            service.findAll().forEach(p->{
                participants.add(participantConverter.convertToParticipantFraunDto(p));
            });

            if(participants.isEmpty()){
                return new ResponseEntity<>(
                    "No Participants are registered", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(participants);
        }
        else {
            Optional<Participant1> participant=service
                .findParticipantByParticipantIDSID(id);

            if(participant.isEmpty()) {
                return new ResponseEntity<>(
                    "Participant not found with ID : '" + id + "'",
                    HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok( participantConverter.convertToParticipantFraunDto(participant.get()).toJson3());

        }

    }


    /**
     * Insert a Participant
     *
     * @return Participant Self-description or error response.
     */
    @PostMapping(path = "participants", consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(
            summary = "Add or Update Participant",
            responses = {
                    @ApiResponse(responseCode = "201", description ="Item created",
                            content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Participant.class))}),
                    @ApiResponse(responseCode = "400", description ="Bad input parameters"),
                    @ApiResponse(responseCode = "409", description ="The Participant already exists"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @RequestBody(required = true, description = "The Participant",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Participant.class))
            }
    )
    @Transactional
    public ResponseEntity<?> addParticipant(InputStream inputStream, final HttpServletRequest request) {

        Participant1 responseParticipant;
        LOGGER.info("Adding a Participant!");
        // If using the Fraunhofer Serializer, I would need to add the Context for each participant

        JsonNode json = null;
        Participant participant;
        try {
            json = jsonConverter.inputStream2Json(inputStream);
            LOGGER.debug("Converting input stream to JsonNode with success");
            participant = jsonConverter.json2Object(Participant.class, json);
            LOGGER.debug("Converting JsonNode to Participant with success");

            Participant1 addedParticipant=participantUtil.addParticipant(participant);

            return ResponseEntity.ok(addedParticipant);
        }
        catch (Exception e) {
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
            if(e instanceof ResourceNotFoundException){
                return new ResponseEntity<>(
                        new ErrorMessage( e.getMessage()).toJson(),
                        HttpStatus.valueOf(409));
            }
            return new ResponseEntity<>(
                    new ErrorMessage( e.getMessage()).toJson(),
                    HttpStatus.valueOf(500));
        }

            //System.err.println(json.toString());
            //Participant1 participant1=new ParticipantConverter(service,certificationService).convertToDatabaseParticipant(participant);


    }

    /**
     * Update the Participant title
     *
     * @return Participant Self-description or error response.
     */
    @PutMapping(path = "participant/title",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(
            summary = "Update the Participant title",
            responses = {
                    @ApiResponse(responseCode = "201", description ="Item created",
                            content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Participant.class))}),
                    @ApiResponse(responseCode = "400", description ="Bad input parameters"),
                    @ApiResponse(responseCode = "404", description ="The Participant given does not exist"),
                    @ApiResponse(responseCode = "409", description ="The title already exists"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Transactional
    public ResponseEntity<?> updateParticipantTitle(
            @Parameter(description = "The element uri (Participant_ID).", required = true)
            @RequestParam("participantId") final URI participantIdsId,
            @Parameter(description = "The new name (Title).", required = true)
            @RequestParam("participantTitle") final String title) {

        LOGGER.info("Adding a Title to a Participant!");

        Optional<Participant1> participant=service
                .findParticipantByParticipantIDSID(participantIdsId);
        if(participant.isEmpty()) {
            return new ResponseEntity<>(
                    "Participant not found with ID : '" + participantIdsId + "'",
                    HttpStatus.NOT_FOUND);
        }

        service.addTitleToParticipant(title, participant.get());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets Participant by Title
     *
     * @return List of Participants that have that title or error response.
     */
    @GetMapping(value = {"participant/title"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get the Participants that have the title", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity getParticipantsByTitle(
            @Parameter(description = "The Title.")
            @RequestParam("title") final String name) {
        Set<Participant1> participants=service
                .findParticipantByName(name);

        List<ParticipantFraunDto> participantFraunDtos=new ArrayList<>();
        participants.forEach(part -> {
            participantFraunDtos.add(participantConverter.convertToParticipantFraunDto(part));
        });
        return ResponseEntity.ok(CommunicationUtil.serialize(participantFraunDtos));
    }

    /**
     * Remove Title from Participant
     *
     * @return OK or error response.
     */
    @DeleteMapping(value = {"participant/title"})
    @Operation(summary = "Remove Title from Participant", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "404", description = "Invalid Participant ID"),
            @ApiResponse(responseCode = "409", description = "The title does not exist on the Participant"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity removeTitleFromParticipant(
            @Parameter(description = "The element uri (Participant_ID).", required = true)
            @RequestParam("participantId") final URI participantIdsId,
            @Parameter(description = "The name (Title).", required = true)
            @RequestParam("participantTitle") final String title) {
        LOGGER.info("Removing Title '"+title+"' from Participant with ID '"+participantIdsId+"'");
        Optional<Participant1> participant=service
                .findParticipantByParticipantIDSID(participantIdsId);

        if(participant.isEmpty()) {
            return new ResponseEntity<>(
                    "Participant not found with ID : '" + participantIdsId + "'",
                    HttpStatus.NOT_FOUND);
        }
        try {
            service.removeTitleFromParticipant(title, participant.get());
        }catch (ResourceNotFoundException exception){
            return new ResponseEntity<>(
                    new ErrorMessage( exception.getMessage()).toJson(),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets Connectors by Participant ID
     *
     * @return List of Connectors belonging to a Participant or error response.
     */
    @GetMapping(value = {"participant/connectors"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get the Connectors belonging to a ParticipantID", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "404", description = "Invalid Participant ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity getConnectorsByParticipant(
            @Parameter(description = "The element uri (Participant).")
            @RequestParam("participantId") final URI id) {
        Optional<Participant1> participant=service
                .findParticipantByParticipantIDSID(id);

        if(participant.isEmpty()) {
            return new ResponseEntity<>(
                    "Participant not found with ID : '" + id + "'",
                    HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(connectorService
                                .findConnectorsFromParticipant(participant.get()));
    }

    /**
     * Remove Participant
     *
     * @return OK or error response.
     */
    @DeleteMapping(value = {"participants"})
    @Operation(summary = "Remove the Participant", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "404", description = "Invalid Participant ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity removeParticipant(
            @Parameter(description = "The element uri (Participant).")
            @RequestParam("participantId") final URI id) {
        LOGGER.info("Removing Participant with ID '"+id+"'");
        Optional<Participant1> participant=service
                .findParticipantByParticipantIDSID(id);

        if(participant.isEmpty()) {
            return new ResponseEntity<>(
                    "Participant not found with ID : '" + id + "'",
                    HttpStatus.NOT_FOUND);
        }
        try {
            participantUtil.removeParticipant(participant.get());
        }catch (Exception exception){
            return new ResponseEntity<>(
                    new ErrorMessage( exception.getMessage()).toJson(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * Builds a Dummy Participant
     *
     * @return  Dummy Participant.
     */
    //@SecurityRequirements
    @GetMapping(value = {"participant/dummyParticipant"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get a dummy Participant", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity getDummyParticipant() {
        try {
            return ResponseEntity.ok(participantConverter.buildDummyParticipant());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Remove Description from Participant
     *
     * @return OK or error response.
     */
    @DeleteMapping(value = {"participant/description"})
    @Operation(summary = "Remove Description from Participant", responses = {
        @ApiResponse(responseCode = "200", description = "successful operation"),
        @ApiResponse(responseCode = "404", description = "Participant ID or Description not found"),
        @ApiResponse(responseCode = "409", description = "The description does not exist on the Participant"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity removeDescriptionFomParticipant(
        @Parameter(description = "The element uri (Participant_ID).", required = true)
        @RequestParam("participantId") final URI participantIdsId,
        @Parameter(description = "The description.", required = true)
        @RequestParam("description") final String description) {
        LOGGER.info("Removing Description '"+description+"' from Participant with ID '"+participantIdsId+"'");
        Optional<Participant1> participant=service
            .findParticipantByParticipantIDSID(participantIdsId);

        if(participant.isEmpty()) {
            return new ResponseEntity<>(
                "Participant not found with ID : '" + participantIdsId + "'",
                HttpStatus.NOT_FOUND);
        }
        try {
            service.removeDescriptionFromParticipant(description, participant.get());
        }catch (ResourceNotFoundException exception){
            return new ResponseEntity<>(
                new ErrorMessage( exception.getMessage()).toJson(),
                HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Add the Participant description
     *
     * @return Participant Self-description or error response.
     */
    @PostMapping(path = "participant/description")
    @Operation(
        summary = "Add a description",
        responses = {
            @ApiResponse(responseCode = "201", description ="Item created"),
            @ApiResponse(responseCode = "400", description ="Bad input parameters"),
            @ApiResponse(responseCode = "404", description ="The Participant given does not exist"),
            @ApiResponse(responseCode = "409", description ="The description already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @Transactional
    public ResponseEntity<?> addDescription(
        @Parameter(description = "The element uri (Participant_ID).", required = true)
        @RequestParam("participantId") final URI participantIdsId,
        @Parameter(description = "The description to add.", required = true)
        @RequestParam("description") final String description) {

        LOGGER.info("Adding a description to a Participant!");

        Optional<Participant1> participant=service
            .findParticipantByParticipantIDSID(participantIdsId);
        if(participant.isEmpty()) {
            return new ResponseEntity<>(
                "Participant not found with ID : '" + participantIdsId + "'",
                HttpStatus.NOT_FOUND);
        }

        try {
            service.addDescriptionToParticipant(description, participant.get());
        }catch (ResourceAlreadyExistsException exception){
            return new ResponseEntity<>(
                new ErrorMessage( exception.getMessage()).toJson(),
                HttpStatus.CONFLICT);
        }


        return new ResponseEntity<>(HttpStatus.OK);
    }

}
