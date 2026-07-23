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
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import ids.messaging.core.daps.DapsConnectionException;
import ids.messaging.core.daps.DapsEmptyResponseException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.handler.message.MessageHandler;
import ids.messaging.handler.message.MessageHandlerException;
import ids.messaging.handler.message.MessagePayload;
import ids.messaging.handler.message.SupportedMessageType;
import ids.messaging.response.BodyResponse;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import ids.messaging.util.IdsMessageUtils;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.CommunicationUtil;
import inesctec.inestec.paris.converter.ParticipantConverter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Component
@SupportedMessageType(QueryMessageImpl.class)
public class QueryMessageHandler implements MessageHandler<QueryMessageImpl> {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryMessageHandler.class);

  private final Connector connector;
  private final DapsTokenProvider provider;
  private ParticipantService service;

  private ConnectorService connectorService;
  private ParticipantCertificationService certificationService;
  private LogicalConstraintService logicalConstraintService;
  private PersonService personService;
  private BusinessIdentifierService businessIdentifierService;

  public QueryMessageHandler(final ConfigContainer container,
                             final DapsTokenProvider provider,
                             ParticipantService service,
                             ParticipantCertificationService certificationService,
                             ConnectorService connectorService,
                             LogicalConstraintService logicalConstraintService,
                             PersonService personService, BusinessIdentifierService businessIdentifierService) {
    this.connector = container.getConnector();
    this.provider = provider;
    this.service=service;
    this.certificationService=certificationService;
    this.connectorService=connectorService;
    this.logicalConstraintService=logicalConstraintService;
    this.personService=personService;
    this.businessIdentifierService = businessIdentifierService;
  }

  /**
   * This message implements the logic that is needed to handle the message. As it just returns the input as string the messagePayload-InputStream is converted to a String.
   *
   * @param queryMessage The queryMessageImpl (part one of the multipart message) containing interesting meta data.
   * @param messagePayload The received payload that has to be handled here
   * @return received payload as string, wrapped inside a @{@link BodyResponse}
   */
  @Override

  public MessageResponse handleMessage(final QueryMessageImpl queryMessage, final MessagePayload messagePayload) throws MessageHandlerException {
    LOGGER.info("EUREKA Received a QueryMessage!");

    try {

      final var receivedPayload = IOUtils.toString(messagePayload.getUnderlyingInputStream(),
              StandardCharsets.UTF_8.name()) ;

      //System.out.println("receivedPayload: "+receivedPayload);

      Statement statement= (Statement) CCJSqlParserUtil.parse(receivedPayload);

      if (statement instanceof Select) {
        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect)  select.getSelectBody();

        Table table = (Table) plainSelect.getFromItem();

        EqualsTo whereCondition = (EqualsTo) plainSelect.getWhere();
        //Column b = (Column) whereCondition.getRightExpression();

        LOGGER.info("Creating message...");
        final var message = createReturnMessage(queryMessage);
        LOGGER.info(String.valueOf(message));

        switch (table.getName().toLowerCase()){
          case "participant", "participante"-> {
            LOGGER.debug("Is Querying Participants...");
            if (plainSelect.getWhere()==null){
              LOGGER.debug("Returning all participants");
              // There is no Where Clause. This means that it is lo list all Participants
              List<ParticipantFraunDto> participants=new ArrayList();

              ParticipantConverter converter=new ParticipantConverter(service, certificationService, logicalConstraintService, personService, businessIdentifierService);
              service.findAll().forEach(p->{
                ParticipantFraunDto participantFraunDto=converter.convertToParticipantFraunDto(p);
                participants.add(participantFraunDto);
              });
              //JsonConverter jsonConverter=new JsonConverter();
              // I return this because of the encoding. Otherwise, chatracters like 'ç' won't print correctly
              LOGGER.debug("Returning participants:");
              LOGGER.debug(CommunicationUtil.serialize(participants));
              return BodyResponse.create(message, CommunicationUtil.serialize(participants));
            }
            else {
              String id=extractIDFromWhere(whereCondition);
              Optional<Participant1> participant=service
                      .findParticipantByParticipantIDSID(URI.create(id));

              if(participant.isEmpty()){
                return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                        "Participant Not Found", connector.getId(),
                        connector.getOutboundModelVersion());
              }
              LOGGER.debug("Returning participant with ID: "+ participant.get().getParticipantIdsID());
              ParticipantFraunDto participantFraunDto=new ParticipantConverter(service, certificationService, logicalConstraintService, personService, businessIdentifierService)
                      .convertToParticipantFraunDto(participant.get());
              //LOGGER.debug(participantFraunDto.toJson3());

              return BodyResponse.create(message,
                      CommunicationUtil.serialize(participantFraunDto));
            }
          }
          case "connector", "conector" -> {
            LOGGER.debug("Is Querying Connectors...");
            if(whereCondition==null){
              // There is no Where Clause. This means that it is lo list all Connectors
              Set<inesctec.inestec.paris.model.Connector> connectors= StreamSupport
                      .stream(connectorService.findAll().spliterator(), false)
                      .collect(Collectors.toSet());
              if (connectors.isEmpty()){
                return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                        "Connector Not Found", connector.getId(),
                        connector.getOutboundModelVersion());
              }
              LOGGER.debug("Returning all connectors");
              return BodyResponse.create(message, connectors);
            }
            else {
              String id=extractIDFromWhere(whereCondition);
              Optional<inesctec.inestec.paris.model.Connector> connectorDB=connectorService
                      .findConnectorByConnectorIdsID((id.toString()));
              if(connectorDB.isEmpty()){
                return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                        "Connector Not Found", connector.getId(),
                        connector.getOutboundModelVersion());
              }
              LOGGER.debug("Returning connector with ID: "+ connectorDB.get().getConnectorID());
              return BodyResponse.create(message,connectorDB.get());
            }
          }
          default -> {
            return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS,
                    "Invalid Value", connector.getId(), connector.getOutboundModelVersion());
          }
        }

      }
      else {
        return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS, "Invalid Query"
                , connector.getId(), connector.getOutboundModelVersion());
      }
    }
    catch (Exception e) {
      return ErrorResponse.withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR, e.getMessage(), connector.getId(), connector.getOutboundModelVersion());
    }
  }

  private String extractIDFromWhere(EqualsTo whereCondition) {
    String id = whereCondition.getRightExpression().toString().substring(1, whereCondition.getRightExpression().toString().length()-1);
    System.out.println("ID from the Where: "+ id);
    return id;
  }

  public ResultMessage createReturnMessage(QueryMessageImpl queryMessage) throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
    return new ResultMessageBuilder()
            ._securityToken_(provider.getDAT())
            ._correlationMessage_(queryMessage.getId())
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._issuerConnector_(connector.getId())
            ._modelVersion_(this.connector.getOutboundModelVersion())
            ._senderAgent_(connector.getId())
            .build();

  }
}