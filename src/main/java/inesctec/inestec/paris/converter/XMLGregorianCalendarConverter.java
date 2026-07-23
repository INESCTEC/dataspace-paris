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
package inesctec.inestec.paris.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

public class XMLGregorianCalendarConverter implements Converter<XMLGregorianCalendar, LocalDateTime> {

    // TODO: remove unused methods and test all remaining methods
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLGregorianCalendarConverter.class);

    public XMLGregorianCalendar convertToXMLGregorianCalendar(LocalDateTime source) {
        LOGGER.debug("Converting from LocalDateTime to XMLGregorianCalendar");
        // Convert LocalDateTime to XMLGregorianCalendar
        XMLGregorianCalendar xmlGregorianCalendar = null;
        try {
            xmlGregorianCalendar = DatatypeFactory
                    .newInstance()
                    .newXMLGregorianCalendar(
                            GregorianCalendar
                                    .from(source.atZone(ZoneId.systemDefault()))
                    );
            return xmlGregorianCalendar;
        }  catch (IllegalArgumentException | NullPointerException ex){
            // Exception that handles the null's
            return null;
        }
        catch (DatatypeConfigurationException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        catch (Exception exception){
            LOGGER.error(exception.getMessage());
            return null;
        }
    }
    public XMLGregorianCalendar convertToXMLGregorianCalendar(ZonedDateTime source) {
        LOGGER.debug("Converting from LocalDateTime to XMLGregorianCalendar");
        // Convert LocalDateTime to XMLGregorianCalendar
        XMLGregorianCalendar xmlGregorianCalendar = null;
        try {
            xmlGregorianCalendar = DatatypeFactory
                    .newInstance()
                    .newXMLGregorianCalendar(
                            GregorianCalendar
                                    .from(source));
            return xmlGregorianCalendar;
        }  catch (IllegalArgumentException | NullPointerException ex){
            // Exception that handles the null's
            return null;
        }
        catch (DatatypeConfigurationException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        catch (Exception exception){
            LOGGER.error(exception.getMessage());
            return null;
        }
    }

    @Override
    public LocalDateTime convert(XMLGregorianCalendar xmlGregorianCalendar) {
        // Tries to convert from XMLGregorianCalendar to LocalDateTime
        // This handles dates with the Z, for instance: 2021-06-30Z
        if(xmlGregorianCalendar==null) return null;
        return xmlGregorianCalendar
                .toGregorianCalendar()
                .toZonedDateTime()
                .toLocalDateTime();
    }

    public ZonedDateTime convertToZonedDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        // Tries to convert from XMLGregorianCalendar to LocalDateTime
        // This handles dates with the Z, for instance: 2021-06-30Z
        if(xmlGregorianCalendar==null) return null;
        ZonedDateTime defaultZoned= xmlGregorianCalendar
                .toGregorianCalendar()
                .toZonedDateTime();
        return ZonedDateTime.of(defaultZoned.toLocalDateTime(),
                ZoneId.of("UTC"));
    }
}
