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
import inesctec.inestec.paris.dto.ConnectorDto;
import inesctec.inestec.paris.dto.ConnectorWithListOfParticipantsDto;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.util.CommunicationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@RestController
@ApiResponse
@Tag(name = "3. Connectors", description = "API for Information regarding the Connectors")
@RequestMapping("/api")
public class ConnectorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorController.class);

    private final ConnectorService service;
    private final ParticipantService participantService;

    private final PersonService personService;
    private final LogicalConstraintService logicalConstraintService;
    private final BusinessIdentifierService businessIdentifierService;
    private final ParticipantCertificationService certificationService;

    private final JsonConverter jsonConverter;
    private ParticipantConverter participantConverter;


    public ConnectorController(ParticipantService participantService,
                               ParticipantCertificationService certificationService,
                               ConnectorService service,
                               PersonService personService,
                               LogicalConstraintService logicalConstraintService,
                               BusinessIdentifierService businessIdentifierService) {
        this.service = service;
        this.certificationService = certificationService;
        this.participantService = participantService;
        this.personService = personService;
        this.logicalConstraintService = logicalConstraintService;
        this.businessIdentifierService = businessIdentifierService;
        this.jsonConverter = new JsonConverter();
        this.participantConverter = new ParticipantConverter(this.participantService, this.certificationService,
                this.logicalConstraintService, this.personService, this.businessIdentifierService);
    }

    /**
     * Get a List of Connectors
     *
     * @return List of Connectors or error response.
     */
    @GetMapping(value = {"connectors"})
    @Operation(summary = "Get a list of all Connectors", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConnectorWithListOfParticipantsDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "404", description = "Resource not found")
    })
    public ResponseEntity getListOfConnectors() {
        /*Set<Connector> connectors= StreamSupport
                .stream(service.findAll().spliterator(), false)
                .collect(Collectors.toSet());*/
        Set<ConnectorWithListOfParticipantsDto> connectors= new HashSet<>();
        service.findAll().forEach(c-> connectors.add(new ConnectorWithListOfParticipantsDto(c)));
        if (connectors.isEmpty()){
            return new ResponseEntity<>(
                    "No Connectors are registered", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(connectors);
    }


    /**
     * Gets Connectors by ID
     *
     * @return Connectors Self-description or error response.
     */
    @GetMapping(value = {"connector"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get the connector by ConnectorID", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ConnectorWithListOfParticipantsDto.class))}),
            @ApiResponse(responseCode = "404", description = "Invalid Connector ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity getConnector(
            @Parameter(description = "The element uri (Connector).")
            @RequestParam("connectorId") final URI id) {
        Optional<Connector> connector=service
                .findConnectorByConnectorIdsID(String.valueOf(id));

        if(connector.isEmpty()) {
            return new ResponseEntity<>(
                    "Connector not found with ID : '" + id + "'",
                    HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(new ConnectorWithListOfParticipantsDto(connector.get()));
    }

    /**
     * Gets Connectors by Endpoint
     *
     * @return Connectors Self-description or error response.
     */
    @GetMapping(value = {"connector/endpoint"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get the connector by Endpoint", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConnectorWithListOfParticipantsDto.class))}),
            @ApiResponse(responseCode = "404", description = "Invalid Connector IDS Endpoint"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity getConnectorByEndpoint(
            @Parameter(description = "The element uri (connectorIdsEndpoint).")
            @RequestParam("connectorIdsEndpoint") final URI endpoint) {
        Optional<Connector> connector=service
                .findConnectorsByConnectorIdsEndpoint(String.valueOf(endpoint));

        if(connector.isEmpty()) {
            return new ResponseEntity<>(
                    "Connector not found with IDS Endpoint : '" + endpoint + "'",
                    HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(new ConnectorWithListOfParticipantsDto(connector.get()));
    }



    /**
     * Insert Connector
     *
     * @return Connector Self-description or error response.
     */
    @PostMapping(path = "connector", consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(
            summary = "Add Connector",
            responses = {
                    @ApiResponse(responseCode = "201", description ="Item created",
                            content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ConnectorDto.class))}),
                    @ApiResponse(responseCode = "400", description ="Bad input parameters"),
                    @ApiResponse(responseCode = "404", description ="The Participant given does not exist"),
                    @ApiResponse(responseCode = "409", description ="The ConnectorID already exists"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @RequestBody(required = true, description = "The Connector",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConnectorDto.class))
            }
    )
    public ResponseEntity<?> addConnectors(InputStream inputStream) {

        JsonNode json = null;
        try {
            json = jsonConverter.inputStream2Json(inputStream);
            Connector connector = jsonConverter.json2Object(Connector.class, json);


            Optional<Connector> connectorOnDB=service.findConnectorByConnectorIdsID(
                    String.valueOf(connector.getConnectorID()));
            // If the Connector already exists
            if(!connectorOnDB.isEmpty()) {
                return new ResponseEntity<>(
                        "Connector with ID : '" + connector.getConnectorID() + "' already exists",
                        HttpStatus.CONFLICT);
            }

            // In the case that it is a Connector never registered on the Database
            return ResponseEntity.ok(service.save(connector));
        } catch (Exception e) {
            //System.out.println("Exceção");
            throw new RuntimeException(e);
        }
    }

    /**
     * Associate Connector with Participant
     *
     * @return Connector Self-description or error response.
     */
    @PostMapping(path = "connector/participant")
    @Operation(
            summary = "Associate Connector and Participant",
            responses = {
                    @ApiResponse(responseCode = "201", description ="Item Associated",
                        content =
                            {
                                @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ConnectorWithListOfParticipantsDto.class))}),
                    @ApiResponse(responseCode = "400", description ="Bad input parameters"),
                    @ApiResponse(responseCode = "404", description ="The Participant given does not exist"),
                    @ApiResponse(responseCode = "409", description ="The Connector given does not exist"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<?> associateConnectorWithParticipant(
            @Parameter(description = "The element uri (Participant_ID).", required = true)
            @RequestParam("participantId") final URI participantIdsId,
            @Parameter(description = "The element uri (Connector_ID).", required = true)
            @RequestParam("connectorId") final URI connectorIdsId) {

        Optional<Participant1> participant=participantService
                .findParticipantByParticipantIDSID(participantIdsId);
        if(participant.isEmpty()) {
            return new ResponseEntity<>(
                    "Participant not found with ID : '" + participantIdsId + "'",
                    HttpStatus.NOT_FOUND);
        }

        Optional<Connector> connector=service.findConnectorByConnectorIdsID(connectorIdsId.toString());
        if(connector.isEmpty()) {
            return new ResponseEntity<>(
                    "Connector not found with ID : '" + connectorIdsId + "'",
                    HttpStatus.NOT_FOUND);
        }

        // Adds the Connector to a/another Participant

        // If the given Participant already has this connector, sends a warning with this information
        if(participant.get().getConnectorSet().contains(connector.get())){
            return new ResponseEntity<>(
                    "Connector with ID : '" + connector.get().getConnectorID() + "' already registered on this Participant",
                    HttpStatus.ACCEPTED);
        }
        else {
            ConnectorWithListOfParticipantsDto responseToReturn= new ConnectorWithListOfParticipantsDto(
                service.associateConnectorToParticipant(connector.get(),participant.get()));
            // Add Participant to the Connector
            return ResponseEntity.ok(responseToReturn);
        }
    }

    // It is not possible update the association. Only to create and remove
    /**
     * Update Connector
     *
     * @return Connector Self-description or error response.
     */
    @PutMapping(path = "connector", consumes = { MediaType.APPLICATION_JSON_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(
            summary = "Update Connector",
            responses = {
                    @ApiResponse(responseCode = "201", description ="Item updated",
                            content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ConnectorDto.class))}),
                    @ApiResponse(responseCode = "400", description ="Bad input parameters"),
                    @ApiResponse(responseCode = "409", description ="The ConnectorID does not exists"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @RequestBody(required = true, description = "The Connector",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConnectorDto.class))
            }
    )
    public ResponseEntity<?> updateConnector(InputStream inputStream) {
        LOGGER.debug("Updating Connector...");
        JsonNode json = null;
        try {
            json = jsonConverter.inputStream2Json(inputStream);
            Connector connector = jsonConverter.json2Object(Connector.class, json);
            //System.err.println(connector);

            Connector updatedConnector;
            // Update returns an ResourceNotFoundException if the connector is not on DB
            try {
                updatedConnector=service.update(connector);
                LOGGER.debug("... Connector updated.");
            }catch (ResourceNotFoundException ex){
                return new ResponseEntity<>(
                        "Connector with ID : '" + connector.getConnectorID() + "' does not exist",
                        HttpStatus.NOT_FOUND);
            }
            catch (ConstraintViolationException ex){
                return new ResponseEntity<>(
                        "'connectorIdsEndpoint' should be unique. Chose another value for this field.",
                        HttpStatus.NOT_ACCEPTABLE);
            }

            //System.out.println(service.findConnectorsFromParticipant(participant.get()));
            return ResponseEntity.ok(updatedConnector);
        } catch (Exception e) {
            LOGGER.error(e.toString());
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //return responseEntities.withObject(participant, HttpStatus.CREATED);
    }

    /**
     * Remove Connector
     *
     * @return OK or error response.
     */
    @DeleteMapping(path = "connector",
            produces = { MediaType.APPLICATION_JSON_VALUE })
    @Operation(
            summary = "Remove Connector",
            responses = {
                    @ApiResponse(responseCode = "201", description ="Item removed",
                            content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Connector.class))}),
                    @ApiResponse(responseCode = "400", description ="Bad input parameters"),
                    @ApiResponse(responseCode = "409", description ="The ConnectorID does not exists"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<?> removeConnector(
            @Parameter(description = "The element uri (Connector_ID).", required = true)
            @RequestParam("connectorId") final URI connectorIdsId) {

        LOGGER.debug("Removing Connector "+connectorIdsId);
        Optional<Connector> connector=service.findConnectorByConnectorIdsID(connectorIdsId.toString());
        if(connector.isEmpty()) {
            LOGGER.error("Connector with ID '"+connectorIdsId + "' not found.");
            return new ResponseEntity<>(
                    "Connector not found with ID : '" + connectorIdsId + "'",
                    HttpStatus.NOT_FOUND);
        }

        try {
            Set<Participant1> participantSet=participantService.findParticipant1ByConnectorSet(Set.of(connector.get()));

            participantSet.forEach(p->p.setConnectorSet((Set<Connector>) null));
            LOGGER.debug("... Removing the Connector from the Participant it belongs to... ");
            participantSet.forEach(p->participantService.update(p));
            LOGGER.debug("... Remove Connector... ");
            service.remove(connector.get());
        }catch (Exception ex){
            LOGGER.error("Error while removing Connector...");
            LOGGER.error("..."+ex.getMessage());
            return new ResponseEntity<>(
                    "Error while deleting connector: '" + ex.getMessage() + "'",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LOGGER.debug("... Connector removed.");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Remove Association between Connector and Participant
     *
     * @return OK or error response.
     */
    @DeleteMapping(path = "connector/participant")
    @Operation(
            summary = "Remove Association between Connector and Participant",
            responses = {
                    @ApiResponse(responseCode = "201", description ="Item removed"),
                    @ApiResponse(responseCode = "400", description ="Bad input parameters"),
                    @ApiResponse(responseCode = "404", description ="The Participant or Connector given do not exist"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<?> removeAssociationBetweenConnectorAndParticipant(
            @Parameter(description = "The element uri (Participant_ID).", required = true)
            @RequestParam("participantId") final URI participantIdsId,
            @Parameter(description = "The element uri (Connector_ID).", required = true)
            @RequestParam("connectorId") final URI connectorIdsId) {


        LOGGER.debug("Removing Association between Connector "+connectorIdsId + " and Participant "+participantIdsId);

        Optional<Participant1> participant=participantService
                .findParticipantByParticipantIDSID(participantIdsId);
        if(participant.isEmpty()) {
            return new ResponseEntity<>(
                    "Participant not found with ID : '" + participantIdsId + "'",
                    HttpStatus.NOT_FOUND);
        }

        Optional<Connector> connector=service.findConnectorByConnectorIdsID(connectorIdsId.toString());
        if(connector.isEmpty()) {
            LOGGER.error("Connector with ID '"+connectorIdsId + "' not found.");
            return new ResponseEntity<>(
                    "Connector not found with ID : '" + connectorIdsId + "'",
                    HttpStatus.NOT_FOUND);
        }

        try {
            // Remove the Participant from the set
            Set<Participant1> updatedParticipantSet=connector.get().getParticipantSet();
            updatedParticipantSet.remove(participant.get());

            connector.get().setParticipantSet(updatedParticipantSet);
            service.update(connector.get());
        }catch (Exception ex){
            LOGGER.error("Error while removing Connector...");
            LOGGER.error("..."+ex.getMessage());
            return new ResponseEntity<>(
                    "Error while deleting assocition between connector and participant: '" + ex.getMessage() + "'",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LOGGER.debug("... Association removed.");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets Participant with the Connector ID
     *
     * @return Participant Self-description or error response.
     */
    @GetMapping(value = {"connectors/participants"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get the participant that the ConnectorID belongs to", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConnectorDto.class))}),
            @ApiResponse(responseCode = "404", description = "Invalid Connector ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity getParticipants(
            @Parameter(description = "The element uri (Connector).")
            @RequestParam("connectorID") final URI id) {
        Optional<Connector> connectorOptional=service.findConnectorByConnectorIdsID(id.toString());

        if(connectorOptional.isEmpty()) {
            return new ResponseEntity<>(
                    "Connector not found with ID : '" + id + "'",
                    HttpStatus.NOT_FOUND);
        }

        Set<Participant1> participant;
        try {
             participant=participantService.findParticipant1ByConnectorSet(Set.of(connectorOptional.get()));
        }
        catch (ResourceNotFoundException exception){
            // This should never happen
            return new ResponseEntity<>(
                    "Connector with ID : '" + id + "' does not belong to any participant.",
                    HttpStatus.NOT_FOUND);
        }
        Set<ParticipantFraunDto> participantSetFraun=new HashSet<>();
        participant.forEach(p->participantSetFraun.add(participantConverter.convertToParticipantFraunDto(p)));
        return ResponseEntity.ok(participantSetFraun);
    }

    /**
     * Gets Connector using search terms
     *
     * @return List of Connectors that have that term or error response.
     */
    @GetMapping(value = {"connector/keyword"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get the Connector that has the search term in the ID", responses = {
        @ApiResponse(responseCode = "200", description = "successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity getConnectorBySearchTerm(
        @Parameter(description = "The keyword to search for")
        @RequestParam("keyword") final String keyword) {
        List<ConnectorWithListOfParticipantsDto> connectors=service
            .findConnectorsBySearchTerm(keyword);

        return ResponseEntity.ok(connectors);
    }
}
