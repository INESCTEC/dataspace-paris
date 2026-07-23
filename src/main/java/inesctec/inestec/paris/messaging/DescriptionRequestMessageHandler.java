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
package inesctec.inestec.paris.messaging;

import java.util.Optional;

import de.fraunhofer.iais.eis.*;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.CommunicationUtil;
import inesctec.inestec.paris.converter.ParticipantConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.handler.message.MessageHandler;
import ids.messaging.handler.message.MessagePayload;
import ids.messaging.handler.message.SupportedMessageType;
import ids.messaging.response.BodyResponse;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import ids.messaging.util.IdsMessageUtils;

/**
 * Used to Querying a specific Participant Entry
 * It was replaced by the QueryMessage
 *
 * recipient: http://host.docker.internal:8081/api/ids/data
 * elementId: https://stvgodigital.pt/ids/participant/id/producer-01"
 * */
@Component
@SupportedMessageType(DescriptionRequestMessageImpl.class)
public class DescriptionRequestMessageHandler implements MessageHandler<DescriptionRequestMessageImpl> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionRequestMessageHandler.class);

  private final Connector connector;
  private final DapsTokenProvider provider;
  private ParticipantService service;
  private ParticipantCertificationService certificationService;
  private LogicalConstraintService logicalConstraintService;
  private PersonService personService;
  private BusinessIdentifierService businessIdentifierService;

  public DescriptionRequestMessageHandler(final ConfigContainer container,
                                          final DapsTokenProvider provider,
                                          ParticipantService service,
                                          ParticipantCertificationService certificationService,
                                          LogicalConstraintService logicalConstraintService,
                                          PersonService personService, BusinessIdentifierService businessIdentifierService) {
    this.connector = container.getConnector();
    this.provider = provider;
    this.service=service;
    this.certificationService=certificationService;
    this.logicalConstraintService=logicalConstraintService;
    this.personService=personService;
    this.businessIdentifierService = businessIdentifierService;
  }

  /**
   * This message implements the logic that is needed to handle the message. As it just returns the input as string the messagePayload-InputStream is converted to a String.
   *
   * @param requestMessage The RequestMessageImpl (part one of the multipart message) containing interesting meta data.
   * @param messagePayload The received payload that has to be handled here
   * @return received payload as string, wrapped inside a @{@link BodyResponse}
   */
  @Override
  public MessageResponse handleMessage(final DescriptionRequestMessageImpl requestMessage, final MessagePayload messagePayload) {
    LOGGER.info("Received a DescriptionRequestMessage!");

    try {

      if(requestMessage.getRequestedElement()==null)
      {
        return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS,
            "Invalid Participant", connector.getId(), connector.getOutboundModelVersion());
      }
      Optional<Participant1> participant=service
          .findParticipantByParticipantIDSID(requestMessage.getRequestedElement());

      //System.out.println("Participant is empty: "+participant.isEmpty());
      if(participant.isEmpty()){
        return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
            "Participant Not Found", connector.getId(),
            connector.getOutboundModelVersion());
      }


      LOGGER.info("Creating message...");
      final var message = new ResultMessageBuilder()
          ._securityToken_(provider.getDAT())
          ._correlationMessage_(requestMessage.getId())
          ._issued_(IdsMessageUtils.getGregorianNow())
          ._issuerConnector_(connector.getId())
          ._modelVersion_(this.connector.getOutboundModelVersion())
          ._senderAgent_(connector.getId())
          .build();

      //LOGGER.info(String.valueOf(message));

      ParticipantConverter participantConverter=new ParticipantConverter(service, certificationService, logicalConstraintService, personService, businessIdentifierService);
      // Maybe it should be a Fraun DTO (To have the Connectors)
      // It is returning a string because of the Portuguese characters

      Participant participant1=participantConverter.convert(participant.get());
      LOGGER.debug(participant1.toRdf());

      return BodyResponse.create(message, CommunicationUtil.serialize(participant1));
    }
    catch (Exception e) {
      return ErrorResponse.withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR, e.getMessage(), connector.getId(), connector.getOutboundModelVersion());
    }
  }
}