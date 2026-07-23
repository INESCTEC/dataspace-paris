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
package inesctec.inestec.paris.builder;

import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import inesctec.inestec.paris.model.Constraint;
import inesctec.inestec.paris.model.LogicalConstraint;
import inesctec.inestec.paris.model.Participant1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class LogicalConstraintBuilder{
  private Long id_logical_constraint;
  private URI ids_ID;
  private List<Constraint> constraints;
  private LogicalConstraintOperator logicalOperator;
  private Participant1 participant;
  private String version;
  // TALVEZ USAR O ID DA BD EM VEZ DA VERSAO

  public LogicalConstraintBuilder(URI ids_ID, LogicalConstraintOperator logicalOperator, Constraint constraint) {
        this.ids_ID = ids_ID;
        this.logicalOperator = logicalOperator;
        this.constraints=new ArrayList<>();
        this.constraints.add(constraint);
    }

    public LogicalConstraintBuilder(URI ids_ID, LogicalConstraintOperator logicalOperator, List<Constraint> constraints) {
        this.ids_ID = ids_ID;
        this.logicalOperator = logicalOperator;
        //True if is not NONE
        if(validate(logicalOperator)){
            this.constraints=constraints;
        }
        else {
            throw new RuntimeException("Participant Refinement shouldn't be a list of Constraints");
        }
    }

    public LogicalConstraintBuilder(URI ids_ID, Constraint constraint) {
        this.ids_ID = ids_ID;
        this.logicalOperator = LogicalConstraintOperator.NONE;
        this.constraints=new ArrayList<>();
        this.constraints.add(constraint);
    }

    public LogicalConstraintBuilder(URI ids_ID) {
        this.ids_ID = ids_ID;
        this.logicalOperator = LogicalConstraintOperator.NONE;
        this.constraints=new ArrayList<>();
    }

    public LogicalConstraintBuilder(URI ids_ID, LogicalConstraintOperator logicalOperator) {
        this.ids_ID = ids_ID;
        this.logicalOperator = logicalOperator;
        this.constraints=new ArrayList<>();
    }

  public LogicalConstraintBuilder id_logical_constraint(Long id_logical_constraint) {
    this.id_logical_constraint = id_logical_constraint;
    return this;
  }

  public LogicalConstraintBuilder version(String version) {
    this.version = version;
    return this;
  }

  public LogicalConstraintBuilder ids_ID(URI ids_ID) {
    this.ids_ID = ids_ID;
    return this;
  }

  public LogicalConstraintBuilder constraints(Constraint constraint) {
    this.constraints.add(constraint);
    return this;
  }

  public LogicalConstraintBuilder constraints(List<Constraint> constraints) {
    this.constraints=constraints;
    return this;
  }

  public LogicalConstraintBuilder logicalOperator(LogicalConstraintOperator logicalOperator) {
    this.logicalOperator = logicalOperator;
    return this;
  }

  public LogicalConstraintBuilder participant(Participant1 participant) {
    this.participant = participant;
    return this;
  }

    public LogicalConstraint build() {
        return new LogicalConstraint(ids_ID,  constraints, logicalOperator, participant, version);
    }

  /*public LogicalConstraint buildIDSLogicalConstraint() {
    return new LogicalConstraint(ids_ID,  constraints, logicalOperator, participant);
  }*/

    /*public LogicalConstraint buildIDSConstraint() {
        return new LogicalConstraint(id_logical_constraint, ids_ID, constraints, logicalOperator, participant);
    }*/

    /*
     * Returns true if LogicalConstraintOperator is AND, OR or XONE
     * Means that it is possible to initialize a list of Constraints
     * */
    public boolean validate(LogicalConstraintOperator logicalOperator){
        if(!logicalOperator.equals(LogicalConstraintOperator.NONE)){
            return true;
        }
        else return false;
    }

}
