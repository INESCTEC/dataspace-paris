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

import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.LeftOperand;
import inesctec.inestec.paris.exceptions.InvalidValueException;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.model.Constraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.URI;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintDto {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintDto.class);

    /* Constraint-ID */
    @Convert(converter = UriConverter.class)
    private URI constraint_ids_ID;

    /**
     * The left operand in a constraint expression.
     */
    private LeftOperand leftOperand;


    /**
     * The operator function applied to operands of a Constraint
     */
    private String operator;

    /**
     * The value of the right operand in a constraint expression. Value should be a rdfs:Resource or
     * literal values. Either ids:rightOperand or ids:rightOperandReference should be used in an
     * ids:Constraint.
     */
    private String rightOperand;


    /**
     * The reference IRI of the right operand in a constraint expression. Has to be dereferenced in
     * order to receive the actual value. Either ids:rightOperand or ids:rightOperandReference should be
     * used in an ids:Constraint.
     */
    private String rightOperandReference;


    /**
     * The unit of measurement of a Constraint.
     */
    @Convert(converter = UriConverter.class)
    private URI unit;


    /**
     * The reference to the PIP which provides the current state of the feature of interest (as
     * referrenced by the leftOperand) can be retrieved.
     */
    private String pipEndpoint;

   /* private LogicalConstraintDto logicalConstraint;*/


    @Override
    public String toString() {
        return "Constraint{" +
                "constraint_ids_ID=" + constraint_ids_ID +
                ", leftOperand='" + leftOperand + '\'' +
                ", operator='" + operator + '\'' +
                ", rightOperand='" + rightOperand + '\'' +
                ", rightOperandReference='" + rightOperandReference + '\'' +
                ", unit=" + unit +
                ", pipEndpoint='" + pipEndpoint + '\'' +
                '}';
    }

    public String toJson(){

        JsonConverter jsonConverter=new JsonConverter();
        try {
            return jsonConverter.object2Json(this).toPrettyString().toString();
        } catch (Exception e) {
            LOGGER.error(String.valueOf(e));
            throw new InvalidValueException("Constraint");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstraintDto that)) return false;
        return getConstraint_ids_ID().equals(that.getConstraint_ids_ID())
            && getLeftOperand() == that.getLeftOperand()
            && Objects.equals(getOperator(), that.getOperator())
            && Objects.equals(getRightOperand(), that.getRightOperand())
            && Objects.equals(getRightOperandReference(), that.getRightOperandReference())
            && Objects.equals(getUnit(), that.getUnit())
            && Objects.equals(getPipEndpoint(), that.getPipEndpoint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConstraint_ids_ID(), getLeftOperand(), getOperator(), getRightOperand(), getRightOperandReference(), getUnit(), getPipEndpoint());
    }

    public Constraint convertToConstraint(){
        return new inesctec.inestec.paris.builder.ConstraintBuilder()
            .constraint_ids_ID(this.getConstraint_ids_ID().toString())
            .unit(this.getUnit())
            .leftOperand(this.getLeftOperand())
            .operator(this.getOperator().toString())
            .rightOperand(this.getRightOperand())
            .rightOperandReference(this.getRightOperandReference())
            .pipEndpoint(this.getPipEndpoint())
            .build();
    }
}
