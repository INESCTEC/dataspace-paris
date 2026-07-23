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

import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.UriConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
public class LogicalConstraintDto {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogicalConstraintDto.class);

    /* Constraint-ID */
    @Convert(converter = UriConverter.class)
    private URI idsID;

    private List<ConstraintDto> constraints;

    /**
     * The Operator in a constraint expression.
     * Can be: AND, XONE, OR
     */
    @Enumerated(EnumType.STRING)
    private LogicalConstraintOperator logicalOperator;

    public LogicalConstraintDto(URI idsID, List<ConstraintDto> constraints, LogicalConstraintOperator logicalOperator) {
        this.idsID = idsID;
        this.constraints = constraints;
        this.logicalOperator = logicalOperator;
    }
    public LogicalConstraintDto() {
        this.constraints = new ArrayList<>();
    }

    public void setIdsID(URI idsID) {
        this.idsID = idsID;
    }

    // TODO: remove this method
    public void setConstraints(List<ConstraintDto> constraints) {
        this.constraints = constraints;
    }

    public void setLogicalOperator(LogicalConstraintOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public void addConstraint(ConstraintDto c){
        if (validate(this.logicalOperator)){
            constraints.add(c);
        }
        else {
            // If is NONE
            if(constraints.isEmpty()){
                constraints.add(c);
            }
            else throw new RuntimeException("Participant Refinement shouldn't be a list of Constraints");
        }

    }

    public void addConstraint(List<ConstraintDto> c){
        if (validate(this.logicalOperator)){
            constraints.addAll(c);
        } else throw new RuntimeException("Participant Refinement shouldn't be a list of Constraints");
    }

    /*
    * Returns true if LogicalConstraintOperator is AND, OR or XONE
    * Means that it is possible to initialize a list of Constraints
    * */
    public boolean validate(LogicalConstraintOperator logicalOperator){
        if(!this.logicalOperator.equals(LogicalConstraintOperator.NONE)){
            return true;
        }
        else return false;
    }

    public String toJson(){
        JsonConverter jsonConverter=new JsonConverter();
        try {
            return jsonConverter.object2Json(this).toPrettyString().toString();
        } catch (Exception e) {
            LOGGER.error(String.valueOf(e));
            throw new InvalidValueException("Logical Constraint");
        }
    }

    @Override
    public String toString() {
        return "LogicalConstraint{" +
                "ids_ID=" + idsID +
                ", constraints=" + constraints +
                ", logicalOperator=" + logicalOperator +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogicalConstraintDto that)) return false;

        return getIdsID().equals(that.getIdsID()) && Objects.equals(ObjectUtils.defaultIfNull(getConstraints(), null), ObjectUtils.defaultIfNull(that.getConstraints(), null))
            && getLogicalOperator() == that.getLogicalOperator();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdsID(), getConstraints(), getLogicalOperator());
    }
}
