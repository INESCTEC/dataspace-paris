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
package inesctec.inestec.paris.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.util.LocalDateTimeTypeAdapter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.util.ZonedDateTimeTypeAdapter;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BusinessIdentifierDto {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantCertification.class);

  /* Identifier-ID */
  @Convert(converter = UriConverter.class)
  private URI businessIdentifierIdsID;

  private String identifierSystem;

  private String identifierNumber;

  public String toJson(){
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
            .setPrettyPrinting()
            .create();
    String json = gson.toJson(this);
    return json;
  }
}
