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
import inesctec.inestec.paris.model.BusinessIdentifier;
import inesctec.inestec.paris.model.Constraint;
import inesctec.inestec.paris.model.LogicalConstraint;
import inesctec.inestec.paris.model.Participant1;

import java.net.URI;

public class BusinessIdentifierBuilder {
  private URI ids_ID;
  private String identifierSystem;
  private String identifierNumber;
  private Participant1 participant;

  public BusinessIdentifierBuilder(String ids_ID) {
    this.ids_ID = URI.create(ids_ID);
  }
  public BusinessIdentifierBuilder(URI ids_ID) {
    this.ids_ID = ids_ID;
  }

  public BusinessIdentifierBuilder() {
  }

  public BusinessIdentifierBuilder ids_ID(URI ids_ID) {
    this.ids_ID = ids_ID;
    return this;
  }
  public BusinessIdentifierBuilder ids_ID(String ids_ID) {
    this.ids_ID = URI.create(ids_ID);
    return this;
  }

  public BusinessIdentifierBuilder identifierSystem(String identifierSystem) {
    this.identifierSystem = identifierSystem;
    return this;
  }

  public BusinessIdentifierBuilder identifierNumber(String identifierNumber) {
    this.identifierNumber = identifierNumber;
    return this;
  }

  public BusinessIdentifierBuilder participant(Participant1 participant) {
    this.participant = participant;
    return this;
  }

  public BusinessIdentifier build() {
    return new BusinessIdentifier(ids_ID, identifierSystem, identifierNumber, participant);
  }

}