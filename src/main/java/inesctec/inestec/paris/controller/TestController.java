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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiResponse
@RequiredArgsConstructor
public class TestController {

    @Autowired
    JdbcTemplate jdbcTemplate;


    @DeleteMapping(path = "/debug")
    @Operation(tags = {"8. Clear Database"}, description = "Clear Database", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    @Transactional
    public void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "constraint_rule");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "part_refinement");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "logical_constraint");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_certification");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "evaluation_facility");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person_email");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person_phone_number");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_person");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "person");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "business_identifier");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_connector");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "connector");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_email");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_legal_name");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_name");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant_description");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "participant");
    }
}
