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

import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.util.RdfResource;
import inesctec.inestec.paris.model.Constraint;
import inesctec.inestec.paris.model.LogicalConstraint;

import java.net.URI;

public class ConstraintBuilder {
  private Long id_constraint;
  private URI constraint_ids_ID;
  private LeftOperand leftOperand;
  private String operator;
  private String rightOperand;
  private String rightOperandReference;
  private URI unit;
  private String pipEndpoint;
  private LogicalConstraint logicalConstraint;
  private String version;

  public ConstraintBuilder(String constraint_ids_ID) {
    this.constraint_ids_ID = URI.create(String.valueOf(constraint_ids_ID));
  }
  public ConstraintBuilder() {
  }

  public ConstraintBuilder constraint_ids_ID(String constraint_ids_ID) {
    this.constraint_ids_ID = URI.create(constraint_ids_ID);
    return this;
  }

  public ConstraintBuilder leftOperand(LeftOperand leftOperand) {
    this.leftOperand = leftOperand;
    return this;
  }

  public ConstraintBuilder operator(String operator) {
    this.operator = operator;
    return this;
  }

  public ConstraintBuilder rightOperand(String rightOperand) {
    this.rightOperand = rightOperand;
    return this;
  }

  public ConstraintBuilder rightOperand(RdfResource rightOperand) {
    this.rightOperand = rightOperand==null ? null:rightOperand.getValue();
    return this;
  }

  public ConstraintBuilder rightOperandReference(String rightOperandReference) {
    this.rightOperandReference = rightOperandReference==null ? null:rightOperandReference.toString();
    return this;
  }

  public ConstraintBuilder rightOperandReference(URI rightOperandReference) {
    this.rightOperandReference = rightOperandReference==null ? null:rightOperandReference.toString();
    return this;
  }

  public ConstraintBuilder version(String version) {
    this.version = version;
    return this;
  }

  public ConstraintBuilder unit(String unit) {
    this.unit = URI.create(unit);
    return this;
  }
  public ConstraintBuilder unit(URI unit) {
    this.unit = unit;
    return this;
  }

  public ConstraintBuilder pipEndpoint(String pipEndpoint) {
    this.pipEndpoint = pipEndpoint;
    return this;
  }

  public ConstraintBuilder pipEndpoint(URI pipEndpoint) {
    this.pipEndpoint = pipEndpoint==null ? null:pipEndpoint.toString();
    return this;
  }

  public ConstraintBuilder logicalConstraint(LogicalConstraint logicalConstraint) {
    this.logicalConstraint = logicalConstraint;
    return this;
  }

  public Constraint build() {
    return new Constraint(constraint_ids_ID, leftOperand, operator, rightOperand, rightOperandReference, unit, pipEndpoint, logicalConstraint, version);
  }

}