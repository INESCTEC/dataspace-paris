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
import inesctec.inestec.paris.dto.ConstraintDto;
import inesctec.inestec.paris.dto.LogicalConstraintDto;
import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import inesctec.inestec.paris.converter.UriConverter;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static inesctec.inestec.paris.config.DatabaseConstants.URI_COLUMN_LENGTH;


@Getter
@Table(name = "logical_constraint", uniqueConstraints = {
    @UniqueConstraint(name = "UniqueIDSID_Version", columnNames = { "idsID", "participantVersion" })})
@Entity
public class LogicalConstraint implements Serializable {

    // Create a unique key with version and ids_id. The version is only used to not lose the history of the participant. Do not use it in the DTOs or the infomodel

    private static final Logger LOGGER = LoggerFactory.getLogger(LogicalConstraint.class);
    /**
     * Only one of ids:and, ids:or, or ids:xone can be used for a specific ids:LogicalConstraint!
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id_logical_constraint;

    /* Constraint-ID */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI idsID;

    @OneToMany(mappedBy="logicalConstraint", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Constraint> constraints;

    /**
     * The Operator in a constraint expression.
     * Can be: AND, XONE, OR
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LogicalConstraintOperator logicalOperator;

    @OneToOne(mappedBy = "participantRefinement", cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    private Participant1 participant;

    @Column(name="participantVersion") //This version is not used on the infomodel. It is only to keep the Participant history
    private String version;

    public LogicalConstraint() {
        this.constraints=new ArrayList<>();
    }

    public LogicalConstraint(URI idsID, List<Constraint> constraints, LogicalConstraintOperator logicalOperator, Participant1 participant, String version) {
        this.idsID = idsID;
        this.constraints = constraints;
        this.logicalOperator = logicalOperator;
        this.participant = participant;
        this.version = version;
    }

    public LogicalConstraint(LogicalConstraintDto logicalConstraintDto) {
        this.idsID = logicalConstraintDto.getIdsID();
        this.constraints = logicalConstraintDto.getConstraints().stream().map(ConstraintDto::convertToConstraint).toList();
        this.logicalOperator = logicalConstraintDto.getLogicalOperator();
    }

    @Transactional
    public void update(LogicalConstraint logicalConstraint) {
        this.idsID = logicalConstraint.idsID;
        this.logicalOperator = logicalConstraint.logicalOperator;
    }

    public void setId_logical_constraint(Long id_logical_constraint) {
        this.id_logical_constraint = id_logical_constraint;
    }

    public void setIdsID(URI ids_ID) {
        this.idsID = ids_ID;
    }

    public void setLogicalOperator(LogicalConstraintOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public void setParticipant(Participant1 participant) {
        this.participant = participant;
    }

    public void addConstraint(Constraint c){
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

    public void addConstraint(List<Constraint> c){
        if (validate(this.logicalOperator)){
            constraints.addAll(c);
        } else throw new RuntimeException("Participant Refinement should not be a Logic Constraint");
    }

    /*
    * Returns true if LogicalConstraintOperator is AND, OR or XONE
    * Means that it is possible to initialize a list of Constraints
    * */
    public boolean validate(LogicalConstraintOperator logicalOperator){
        // IF not NONE
        if(!logicalOperator.equals(LogicalConstraintOperator.NONE)){
            return true;
        }
        else return false;
    }

    @Override
    public String toString() {
        return "LogicalConstraint{" +
                "ids_ID=" + idsID +
                ", constraints=" + constraints +
                ", logicalOperator=" + logicalOperator +
                ", version=" + version +
                '}';
    }

    public LogicalConstraintDto convertToLogicalConstDto(){
        if (this==null){
            return null;
        }
        else {
            List<Constraint> constraints = this.getConstraints();

            LogicalConstraintDto logicalConstraintDto = new LogicalConstraintDto();
            logicalConstraintDto.setLogicalOperator(this.getLogicalOperator());
            logicalConstraintDto.setIdsID(this.getIdsID());
            logicalConstraintDto.setConstraints(ObjectUtils.defaultIfNull(
                    constraints.stream().map(Constraint::convertToConstraintDto).collect(Collectors.toList()),
                    null));
            return logicalConstraintDto;
        }
    }

    public String toJson(){
        return convertToLogicalConstDto().toJson();
    }

    // TODO: verificar se a versao deve ser incluida aqui ou nao
    @Override
    public boolean equals(Object o) {
        LOGGER.debug("Checking if the Logical Constraints are equal...");
        if (this == o) return true;
        if (!(o instanceof LogicalConstraint that)) return false;
        /*System.out.println("\nIds_ID " +  Objects.equals(getIdsID(), that.getIdsID())
                + "\nConstraints " +   Objects.equals(getConstraints(), that.getConstraints())
                + "\nOperator " +   (getLogicalOperator() == that.getLogicalOperator())
               // + "\nLogicalConstraint -> Id_logical_constraint" +   Objects.equals(ObjectUtils.defaultIfNull(getParticipant(), null), ObjectUtils.defaultIfNull(that.getParticipant(), null))
                );*/

        return Objects.equals(ObjectUtils.defaultIfNull(getIdsID(), null), ObjectUtils.defaultIfNull(that.getIdsID(), null))
                && Objects.equals(ObjectUtils.defaultIfNull(getConstraints(), null), ObjectUtils.defaultIfNull(that.getConstraints(), null))
                && getLogicalOperator() == that.getLogicalOperator();
               // && Objects.equals(ObjectUtils.defaultIfNull(getParticipant(), null), ObjectUtils.defaultIfNull(that.getParticipant(), null));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId_logical_constraint(), getIdsID(), getConstraints(), getLogicalOperator());
    }

    public LogicalConstraint deepCopy() {
        return new LogicalConstraint(this.idsID, this.constraints.stream().map(Constraint::deepCopy).toList(), this.logicalOperator, this.participant, this.version);
    }
}
