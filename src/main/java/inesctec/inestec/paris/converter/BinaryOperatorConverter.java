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

import de.fraunhofer.iais.eis.BinaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;


public class BinaryOperatorConverter implements Converter<String, BinaryOperator> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryOperatorConverter.class);
    /**
     * Returns a BinaryOperator
     * */
    @Override
    public BinaryOperator convert(String source) {
        String operator=source.substring(source.lastIndexOf("/") +1).trim();

        /*System.out.println("TEST 2" + StringUtils.substringAfterLast(source, "/"));
        System.out.println("OPERATOR" + operator);*/
        if(source==null)
            return null;
        else {
            try {
                return BinaryOperator.valueOf(operator);
            }
            catch (IllegalArgumentException exception){
                LOGGER.error(exception.getMessage());
                LOGGER.error("Returning null");
                return null;
            }
        }
    }

    public String convert2(String source) {
        String operator=StringUtils.substringAfterLast(source, "/");
        return source == null ? null : operator;
    }
}
