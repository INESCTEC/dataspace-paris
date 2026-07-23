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
package inesctec.inestec.paris.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;

public class XMLGregorianCalendarDeserializer extends JsonDeserializer<XMLGregorianCalendar> {

  @Override
  public XMLGregorianCalendar deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
    try {
      String date=jsonParser.getText();

      // Handle the "UTC" part by removing it if present. This handles the dates from the Swagger interface
      if (date.endsWith("UTC")) {
        date = date.substring(0, date.length() - 3);
      }
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
    } catch (DatatypeConfigurationException e) {
      throw new IOException("Error deserializing XMLGregorianCalendar", e);
    }
  }
}