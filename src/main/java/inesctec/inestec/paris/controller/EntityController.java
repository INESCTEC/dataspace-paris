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

import inesctec.inestec.paris.service.EntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/connector-certificates")
@ApiResponse
@Tag(name = "5. Connectors Certification", description = "API for Information regarding the Connectors Certification Organization")
@RequiredArgsConstructor
public class EntityController {

    private final EntityService service;

    @GetMapping("")
    @Operation(summary = "Retrieves all certificates", description = "Retrieves all Certificates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public List<String> getListOfCertificates() throws IOException {
        List <String> fileNames= new ArrayList();
        service.listFiles().stream().forEach(temp ->
            {
                String name=temp.getFileName().toString().replace(".crt", "");
                try {
                    // If it is a valid UUID, add the certificate name (and extension) to the list
                    UUID.fromString(name);
                    fileNames.add(temp.getFileName().toString());
                }catch (IllegalArgumentException ignored){
                }
            }
        );
        return fileNames;
    }

    @Operation(summary = "Retrieves the Certificate Organization",
        description = "Retrieves the Certificate Organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "404", description = "Invalid Connector ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getOrganization(
            @Parameter(description = "The certificate name (Connector-ID).")
            @RequestParam("id") final String id) {
        String response=service.getJsonCertificateAttributes(id);

        if(response == null || response.isEmpty()){
            return new ResponseEntity<>(
                    "Connector not found with ID : '"+id+"'",
                    HttpStatus.NOT_FOUND);
        }
        else
            return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieves the Certificate Organization"
        , description = "Retrieves Organization from the Certificate and validates the Public Key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "404", description = "Invalid value"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    @GetMapping(value = "/{id}/{publicKey}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getOrganization(
            @Parameter(description = "The certificate name (Connector-ID).")
            @RequestParam("id") final String id,

            @Parameter(description = "The public key (From Connector self-description).")
            @RequestParam("publicKey") final String publicKey) {
        String response=service.getJsonCertificateAttributes(id, publicKey);

        if(response == null || response.isEmpty()){
            return new ResponseEntity<>(
                    "Connector not found with ID : '"+id+"'",
                    HttpStatus.NOT_FOUND);
        }
        if(response.equals(publicKey)){
            return new ResponseEntity<>(
                    "Connector PublicKey does not equal the PublicKey of the certificate with ID: '"+id+"'",
                    HttpStatus.NOT_FOUND);
        }
        else
            return ResponseEntity.ok(response);
    }
}
