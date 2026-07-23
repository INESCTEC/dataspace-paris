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
package inesctec.inestec.paris.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import de.fraunhofer.iais.eis.LeftOperand;
import inesctec.inestec.paris.dto.ConstraintDto;
import inesctec.inestec.paris.converter.UriConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.*;

import static inesctec.inestec.paris.config.DatabaseConstants.URI_COLUMN_LENGTH;


@Getter
@Setter
@Table(name = "constraintRule", uniqueConstraints = {
    @UniqueConstraint(name = "UniqueIDSID_Version", columnNames = { "constraintIdsID", "participantVersion" })})
@Entity
@NoArgsConstructor
public class Constraint implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_constraint;

    /* Constraint-ID */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI constraintIdsID;

    /**
     * The left operand in a constraint expression.
     */
    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private LeftOperand leftOperand;


    /**
     * The operator function applied to operands of a Constraint
     */
    @Column(nullable = false)
    private String operator;

    /**
     * The value of the right operand in a constraint expression. Value should be a rdfs:Resource or
     * literal values. Either ids:rightOperand or ids:rightOperandReference should be used in an
     * ids:Constraint.
     */
    @Column
    private String rightOperand;


    /**
     * The reference IRI of the right operand in a constraint expression. Has to be dereferenced in
     * order to receive the actual value. Either ids:rightOperand or ids:rightOperandReference should be
     * used in an ids:Constraint.
     */
    @Column private String rightOperandReference;


    /**
     * The unit of measurement of a Constraint.
     */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI unit;


    /**
     * The reference to the PIP which provides the current state of the feature of interest (as
     * referrenced by the leftOperand) can be retrieved.
     */
    @Column
    private String pipEndpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_logical_constraint", nullable=false)
    @JsonBackReference
    private LogicalConstraint logicalConstraint;

    @Column(name = "participantVersion") //This version is not used on the infomodel. It is only to keep the Participant history
    private String version;


    public Constraint(URI constraintIdsID, LeftOperand leftOperand, String operator, String rightOperand, String rightOperandReference, URI unit, String pipEndpoint, LogicalConstraint logicalConstraint, String version) {
        this.constraintIdsID = constraintIdsID;
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.rightOperandReference = rightOperandReference;
        this.unit = unit;
        this.pipEndpoint = pipEndpoint;
        this.logicalConstraint = logicalConstraint;
        this.version=version;
    }

    public void update(Constraint constraint) {
        this.constraintIdsID = constraint.constraintIdsID;
        this.leftOperand = constraint.leftOperand;
        this.operator = constraint.operator;
        this.rightOperand = constraint.rightOperand;
        this.rightOperandReference = constraint.rightOperandReference;
        this.unit = constraint.unit;
        this.pipEndpoint = constraint.pipEndpoint;
        //this.logicalConstraint = logicalConstraint;
    }


    @Override
    public String toString() {
        return "Constraint{" +
                "constraint_ids_ID=" + constraintIdsID +
                ", leftOperand='" + leftOperand + '\'' +
                ", operator='" + operator + '\'' +
                ", rightOperand='" + rightOperand + '\'' +
                ", rightOperandReference='" + rightOperandReference + '\'' +
                ", unit=" + unit +
                ", pipEndpoint='" + pipEndpoint + '\'' +
                '}';
    }

    public ConstraintDto convertToConstraintDto(){
        ConstraintDto constraintDto=new ConstraintDto();
        constraintDto.setConstraint_ids_ID(this.getConstraintIdsID());
        constraintDto.setOperator(this.getOperator());
        constraintDto.setUnit(this.getUnit());
        constraintDto.setLeftOperand(this.getLeftOperand());
        constraintDto.setPipEndpoint(this.getPipEndpoint());
        constraintDto.setRightOperand(this.getRightOperand());
        constraintDto.setRightOperandReference(this.getRightOperandReference());
        return constraintDto;
    }
    public String toJson(){
        return (convertToConstraintDto()).toJson();
    }

    public boolean equalsIgnoreID(Object o) {
        if (this == o) return true;
        if (!(o instanceof Constraint that)) return false;
        return getLeftOperand() == that.getLeftOperand() && Objects.equals(getOperator(), that.getOperator()) && Objects.equals(getRightOperand(), that.getRightOperand()) && Objects.equals(getRightOperandReference(), that.getRightOperandReference()) && Objects.equals(getUnit(), that.getUnit()) && Objects.equals(getPipEndpoint(), that.getPipEndpoint()) && Objects.equals(getLogicalConstraint(), that.getLogicalConstraint());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Constraint that)) return false;
        return Objects.equals(ObjectUtils.defaultIfNull(getConstraintIdsID(), null), ObjectUtils.defaultIfNull(that.getConstraintIdsID(), null))
                && getLeftOperand() == that.getLeftOperand()
                && Objects.equals(ObjectUtils.defaultIfNull(getOperator(), null), ObjectUtils.defaultIfNull(that.getOperator(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getRightOperand(), null), ObjectUtils.defaultIfNull(that.getRightOperand(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getRightOperandReference(), null), ObjectUtils.defaultIfNull(that.getRightOperandReference(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getUnit(), null), ObjectUtils.defaultIfNull(that.getUnit(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getPipEndpoint(), null), ObjectUtils.defaultIfNull(that.getPipEndpoint(), null));
                //&& Objects.equals(ObjectUtils.defaultIfNull(getLogicalConstraint(), null), ObjectUtils.defaultIfNull(that.getLogicalConstraint(), null));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId_constraint(), getConstraintIdsID(), getLeftOperand(), getOperator(), getRightOperand(), getRightOperandReference(), getUnit(), getPipEndpoint()/*, getLogicalConstraint()*/);
    }

    public Constraint deepCopy() {
        return new Constraint(this.constraintIdsID, this.leftOperand, this.operator, this.rightOperand, this.rightOperandReference, this.unit,this.pipEndpoint, this.logicalConstraint, this.version);
    }
}
