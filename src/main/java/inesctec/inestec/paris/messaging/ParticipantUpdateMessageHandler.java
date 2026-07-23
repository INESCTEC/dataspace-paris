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

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.handler.message.MessageHandler;
import ids.messaging.handler.message.MessagePayload;
import ids.messaging.handler.message.SupportedMessageType;
import ids.messaging.response.BodyResponse;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import ids.messaging.util.IdsMessageUtils;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.util.ParticipantUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


/**
 * Create Participant Entry
 * This behaviour is done via the Controller
 * */
@Component
@SupportedMessageType(ParticipantUpdateMessageImpl.class)
public class ParticipantUpdateMessageHandler implements MessageHandler<ParticipantUpdateMessageImpl> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantUpdateMessageHandler.class);

  private final Connector connector;
  private final DapsTokenProvider provider;
  /*private ParticipantService service;
  private ParticipantCertificationService certificationService;
*/
  private final JsonConverter jsonConverter;

  private ParticipantUtil participantUtil;


  private EvaluationFacilityService evaluationFacilityService;
  private PersonService personService;
  private ParticipantService participantService;
  private ParticipantCertificationService certificationService;
  private ConstraintService constraintService;
  private LogicalConstraintService logicalConstraintService;
  private BusinessIdentifierService businessIdentifierService;
  private ConnectorService connectorService;

  public ParticipantUpdateMessageHandler(final ConfigContainer container,
                                         final DapsTokenProvider provider,
                                         JsonConverter jsonConverter, EvaluationFacilityService evaluationFacilityService,
                                         PersonService personService, ParticipantService participantService,
                                         ParticipantCertificationService certificationService,
                                         ConstraintService constraintService, LogicalConstraintService logicalConstraintService,
                                         BusinessIdentifierService businessIdentifierService, ConnectorService connectorService,
                                         ParticipantUtil participantUtil) {
    this.connector = container.getConnector();
    this.provider = provider;
    this.jsonConverter = jsonConverter;
    this.evaluationFacilityService = evaluationFacilityService;
    this.personService = personService;
    this.participantService = participantService;
    this.certificationService = certificationService;
    this.constraintService = constraintService;
    this.logicalConstraintService = logicalConstraintService;
    this.businessIdentifierService = businessIdentifierService;
    this.connectorService=connectorService;
    this.participantUtil=participantUtil;

    /*participantUtil =new ParticipantUtil(evaluationFacilityService, personService,participantService,
            certificationService, constraintService, logicalConstraintService, businessIdentifierService, connectorService);*/
  }

  /**
   * This message implements the logic that is needed to handle the message. As it just returns the input as string the messagePayload-InputStream is converted to a String.
   *
   * @param participantUpdateMessage The RequestMessageImpl (part one of the multipart message) containing interesting meta data.
   * @param messagePayload The received payload that has to be handled here
   * @return received payload as string, wrapped inside a @{@link BodyResponse}
   */


  @Override
  @Transactional
  public MessageResponse handleMessage(ParticipantUpdateMessageImpl participantUpdateMessage, MessagePayload messagePayload) {
    LOGGER.info("EUREKA Received a ParticipantUpdateMessage!");

    try {

      // This value is defined on the header
      if(participantUpdateMessage.getAffectedParticipant()==null)
      {
        return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS,
                "Invalid Participant", connector.getId(), connector.getOutboundModelVersion());
      }

      LOGGER.info("Creating message...");
      final var message = new MessageProcessedNotificationMessageBuilder()
              ._securityToken_(provider.getDAT())
              ._correlationMessage_(participantUpdateMessage.getId())
              ._issued_(IdsMessageUtils.getGregorianNow())
              ._issuerConnector_(connector.getId())
              ._modelVersion_(this.connector.getOutboundModelVersion())
              ._senderAgent_(connector.getId())
              .build();

      //LOGGER.info(String.valueOf(message));

      //Participant participantToAdd=messagePayload.readFromJSON(Participant.class);
      Serializer serializer=new Serializer();
      final var requestHeader = serializer.deserialize(IOUtils.toString(messagePayload.getUnderlyingInputStream(),
              StandardCharsets.UTF_8), Participant.class);


      //Check if participantID already exists
      Optional<Participant1> participant=participantService
              .findParticipantByParticipantIDSID(URI.create(requestHeader.getId().toString()));

      if(!participant.isEmpty()){
        System.out.println("Participant does not exist: "+participant.isEmpty());
        return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                "Participant with that ID already exists", connector.getId(),
                connector.getOutboundModelVersion());
      }
      LOGGER.info("Participant does not exist...");
      LOGGER.info("... one will be created.");

      // TODO: I can just send the requestHeader back, instead of reconverting to the Infomodel Participant
      Participant1 participant1=participantUtil.addParticipant(requestHeader);

      LOGGER.info("Converting Database Participant to the Infomodel Participant...");
      Participant convertedParticipant=new ParticipantConverter(participantService, certificationService, logicalConstraintService, personService, businessIdentifierService)
              .convert(participant1);
      LOGGER.info("... Participant converted.");

      LOGGER.info("... Participant successful added to DB");
      LOGGER.info("Sending Response Back to Connector");
      return BodyResponse.create(message, convertedParticipant);

    }
    catch (Exception e) {
      return ErrorResponse.withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR, e.getMessage(), connector.getId(), connector.getOutboundModelVersion());
    }
  }
}