package inesctec.inestec.paris.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.*;
import ids.messaging.handler.message.MessageHandlerException;
import ids.messaging.handler.message.MessagePayload;
import ids.messaging.handler.message.MessagePayloadInputstream;
import ids.messaging.response.MessageResponse;
import ids.messaging.util.IdsMessageUtils;
import inesctec.inestec.paris.DataLoader;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.CommunicationUtil;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.converter.ParticipantConverter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QueryMessageHandlerTest {

  MessageProcessedNotificationMessage messageProcessedNotification;

  Map<String, Object> map;
  Serializer serializer;

  @Autowired private ParticipantService service;
  @Autowired private ParticipantCertificationService certificationService;
  @Autowired private LogicalConstraintService logicalConstraintService;
  @Autowired private ConnectorService connectorService;
  @Autowired private PersonService personService;
  @Autowired private BusinessIdentifierService businessIdentifierService;
  private ParticipantConverter participantConverter;

  @Autowired
  DataLoader dataLoader;
  @Autowired
  private QueryMessageHandler handler;

  @Mock
  private ConfigContainer connectorConfig;

  @Mock
  DapsTokenProvider provider;

  @MockBean
  private ConfigurationModel configurationModel;

  @MockBean
  private DapsValidator dapsValidator;

  private QueryMessage message;

  @BeforeEach
  void setUp() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {

    Connector connector=new BaseConnectorBuilder(URI.create("https://test-paris.com/id/test/connector"))
            ._hasDefaultEndpoint_(
                    new ConnectorEndpointBuilder(URI.create("https://test-paris.com/id/test/connectorEndpoint/dhiasdcna"))
                            ._accessURL_(URI.create("https://host.docker.internal:8080/circthread-01/api/ids/data")).build())
            ._inboundModelVersion_("4.2.6")
            ._outboundModelVersion_("4.2.6")
            ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
            ._maintainer_(URI.create("https://www.inesctec.pt/"))
            .build();

    DynamicAttributeToken dat=new DynamicAttributeTokenBuilder()._tokenValue_("Fake token")._tokenFormat_(TokenFormat.JWT).build();

    Mockito.when(connectorConfig.getConnector()).thenReturn(connector);
    Mockito.when(connectorConfig.getConfigurationModel()).thenReturn(configurationModel);
    Mockito.when(connectorConfig.getConfigurationModel().getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
    Mockito.when(provider.provideDapsToken()).thenReturn("Mock Token");
    Mockito.when(dapsValidator.checkDat(dat)).thenReturn(true);
    Mockito.when(provider.getDAT()).thenReturn(dat);


    handler=new QueryMessageHandler(connectorConfig, provider, service, certificationService,
            connectorService, logicalConstraintService, personService, businessIdentifierService);
    
    serializer=new Serializer();

    participantConverter=new ParticipantConverter(service, certificationService, logicalConstraintService, personService, businessIdentifierService);

    // Create the Header to send on the handleMessage()
    message=new QueryMessageBuilder()
            ._securityToken_(provider.getDAT())
            //._correlationMessage_(message.getId())
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._issuerConnector_(connectorConfig.getConnector().getId())
            ._modelVersion_(this.connectorConfig.getConnector().getOutboundModelVersion())
            ._senderAgent_(connectorConfig.getConnector().getId())
            ._recipientAgent_(connector.getHasDefaultEndpoint().getAccessURL()) // http://host.docker.internal:8081/api/ids/data
            //._recipientScope_(new QueryTar)
            .build();

    messageProcessedNotification= new MessageProcessedNotificationMessageBuilder(URI.create("https://test-paris.com/messageProcessedNotification/id/122454578777"))
            ._securityToken_(provider.getDAT())
            ._correlationMessage_(message.getId())
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._issuerConnector_(connectorConfig.getConnector().getId())
            ._modelVersion_(connectorConfig.getConnector().getOutboundModelVersion())
            ._senderAgent_(connectorConfig.getConnector().getId())
            .build();
  }

  @Test
  void handleMessage_QueryParticipants() {

    try {
      String query="select * from participant";

      MessagePayload messagePayload=new MessagePayloadInputstream(IOUtils.toInputStream(query), new ObjectMapper());

      MessageResponse messageResponse=handler.handleMessage((QueryMessageImpl) message,messagePayload);

      map=messageResponse.createMultipartMap(serializer);
      //System.out.println(map.toString());
      try {
        final var requestHeader = serializer.deserialize((String) map.get("header"), ResultMessage.class);
        assertInstanceOf(ResultMessage.class, requestHeader);
        Assertions.assertTrue(ResultMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return MessageProcessedNotificationMessage. It returns "+ requestHeader.getClass());
      }
      catch (IOException e){
        // Assertation made to fail
        Assertions.assertEquals(ResultMessage.class, "Error while deserializing", "Method should return a ResultMessage.");
      }

      assertEquals(map.get("payload").getClass(), String.class);

      List<?> receivedParticipants = CommunicationUtil.deserializeList(map.get("payload").toString(), ParticipantFraunDto.class);
      receivedParticipants.forEach(ob->{
        if (ob instanceof ParticipantFraunDto){
          ParticipantFraunDto p=(ParticipantFraunDto) ob;
          System.err.println(p.toJson());
          Optional<Participant1> part_Op=service.findParticipantByParticipantIDSID(p.getParticipant().getId());
          assertTrue(part_Op.isPresent());

          Participant1 receivedPart=participantConverter.convertToDatabaseParticipant(p);
          System.out.println(receivedPart.toJson());

          assertTrue(Objects.equals(receivedPart.getParticipantIdsID(), part_Op.get().getParticipantIdsID()));
          assertTrue(CompareUtil.compareStrings(receivedPart.getLegalName(),part_Op.get().getLegalName()));
          assertTrue(CompareUtil.compareStrings(receivedPart.getName(), part_Op.get().getName()));
          assertTrue(Objects.equals(receivedPart.getDescription(), part_Op.get().getDescription()));
          assertTrue(CompareUtil.compareStrings(receivedPart.getCorporateEmailAddress() , part_Op.get().getCorporateEmailAddress()));
          assertTrue(Objects.equals(receivedPart.getCorporateHomepage(), part_Op.get().getCorporateHomepage()));
          //assertTrue(Objects.equals(receivedPart.getParticipantRefinement(), part_Op.get().getParticipantRefinement()));
          assertTrue(CompareUtil.comparePersonsDTO(receivedPart.getMemberPerson(), part_Op.get().getMemberPerson()));
          assertTrue(Objects.equals(receivedPart.getParticipantCertification(), part_Op.get().getParticipantCertification()));
          assertTrue(Objects.equals(receivedPart.getPrimarySite(), part_Op.get().getPrimarySite()));
          assertTrue(Objects.equals(receivedPart.getVatID(), part_Op.get().getVatID()));
          assertEquals( //It needs to be a DTO because of the databaseIDs
              receivedPart.getBusinessIdentifier().toString(),
              part_Op.get().getBusinessIdentifier().toString());
          assertTrue(Objects.equals(receivedPart.getRegistryCourt(), part_Op.get().getRegistryCourt()));
          assertTrue(Objects.equals(receivedPart.getLegalForm(), part_Op.get().getLegalForm()));
          assertEquals(receivedPart.getVersion(), part_Op.get().getVersion());
        }
      });

      assertFalse(receivedParticipants.isEmpty());
      assertEquals(receivedParticipants.size(), 2);

    } catch (SerializeException | MessageHandlerException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void handleMessage_QueryParticipant_withID() {

    try {
      String query="select * from participant where participantID='https://texpact.pt/ids/participant/id/subcontracted-01'";

      MessagePayload messagePayload=new MessagePayloadInputstream(IOUtils.toInputStream(query), new ObjectMapper());

      MessageResponse messageResponse=handler.handleMessage((QueryMessageImpl) message,messagePayload);

      map=messageResponse.createMultipartMap(serializer);
      System.out.println(map.toString());
      try {
        final var requestHeader = serializer.deserialize((String) map.get("header"), ResultMessage.class);
        assertInstanceOf(ResultMessage.class, requestHeader);
        Assertions.assertTrue(ResultMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return MessageProcessedNotificationMessage. It returns "+ requestHeader.getClass());
      }
      catch (IOException e){
        throw new Exception(e);
      }

      ParticipantFraunDto receivedParticipant;
      try {
        System.out.println(map.get("payload").getClass());
        receivedParticipant = CommunicationUtil
                .deserialize( map.get("payload").toString(), ParticipantFraunDto.class);
      }
      catch (Exception e){
        throw new Exception(e);
      }

      Optional<Participant1> part_Op=service.findParticipantByParticipantIDSID(receivedParticipant.getParticipant().getId());
      assertTrue(part_Op.isPresent());

      Participant1 receivedPart=participantConverter.convertToDatabaseParticipant(receivedParticipant);


      assertTrue(Objects.equals(receivedPart.getParticipantIdsID(), part_Op.get().getParticipantIdsID()));
      assertTrue(CompareUtil.compareStrings(receivedPart.getLegalName(),part_Op.get().getLegalName()));
      assertTrue(CompareUtil.compareStrings(receivedPart.getName(), part_Op.get().getName()));
      assertTrue(Objects.equals(receivedPart.getDescription(), part_Op.get().getDescription()));
      assertTrue(CompareUtil.compareStrings(receivedPart.getCorporateEmailAddress() , part_Op.get().getCorporateEmailAddress()));
      assertTrue(Objects.equals(receivedPart.getCorporateHomepage(), part_Op.get().getCorporateHomepage()));
      //assertTrue(Objects.equals(receivedPart.getParticipantRefinement(), part_Op.get().getParticipantRefinement()));
      assertTrue(CompareUtil.compareDBPersons(receivedPart.getMemberPerson(), part_Op.get().getMemberPerson()));
      assertTrue(Objects.equals(receivedPart.getParticipantCertification(), part_Op.get().getParticipantCertification()));
      assertTrue(Objects.equals(receivedPart.getPrimarySite(), part_Op.get().getPrimarySite()));
      assertTrue(Objects.equals(receivedPart.getVatID(), part_Op.get().getVatID()));
      assertEquals(
          receivedPart.getBusinessIdentifier().toString(),
          part_Op.get().getBusinessIdentifier().toString());
      assertTrue(Objects.equals(receivedPart.getRegistryCourt(), part_Op.get().getRegistryCourt()));
      assertTrue(Objects.equals(receivedPart.getLegalForm(), part_Op.get().getLegalForm()));

      System.out.println(receivedPart.getConnectorSet());
      System.out.println(part_Op.get().getConnectorSet());
      //assertTrue(CompareUtil.compareSet(receivedPart.getConnectorSet(), part_Op.get().getConnectorSet()));
      assertEquals(receivedPart.getConnectorSet().toString(), part_Op.get().getConnectorSet().toString());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void handleMessage_QueryConnectors() {

    try {
      String query="select * from connector";

      MessagePayload messagePayload=new MessagePayloadInputstream(IOUtils.toInputStream(query), new ObjectMapper());

      MessageResponse messageResponse=handler.handleMessage((QueryMessageImpl) message,messagePayload);

      map=messageResponse.createMultipartMap(serializer);
      //System.out.println(map.toString());
      try {
        final var requestHeader = serializer.deserialize((String) map.get("header"), ResultMessage.class);
        assertInstanceOf(ResultMessage.class, requestHeader);
        Assertions.assertTrue(ResultMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return MessageProcessedNotificationMessage. It returns "+ requestHeader.getClass());
      }
      catch (IOException e){
        throw new RuntimeException(e);
      }


      Set<inesctec.inestec.paris.model.Connector> receivedConnectors = (Set<inesctec.inestec.paris.model.Connector>) map.get("payload");
      System.err.println(receivedConnectors.size());
      receivedConnectors.forEach(conn->{
        System.err.println(conn);
        Optional<inesctec.inestec.paris.model.Connector> conn_Op=connectorService.findConnectorByConnectorIdsID(conn.getConnectorID().toString());
        assertTrue(conn_Op.isPresent());
        assertEquals(conn_Op.get(), conn);
      });

      assertFalse(receivedConnectors.isEmpty());
      assertEquals(receivedConnectors.size(), 2);

    } catch (SerializeException | MessageHandlerException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void handleMessage_QueryConnector_withID() {

    try {
      String query="select * from connector where connectorID='https://texpact.pt/ids/connector/id/producer-01'";

      MessagePayload messagePayload=new MessagePayloadInputstream(IOUtils.toInputStream(query), new ObjectMapper());

      MessageResponse messageResponse=handler.handleMessage((QueryMessageImpl) message,messagePayload);

      map=messageResponse.createMultipartMap(serializer);
      //System.out.println(map.toString());
      try {
        final var requestHeader = serializer.deserialize((String) map.get("header"), ResultMessage.class);
        assertInstanceOf(ResultMessage.class, requestHeader);
        Assertions.assertTrue(ResultMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return MessageProcessedNotificationMessage. It returns "+ requestHeader.getClass());
      }
      catch (IOException e){
        throw new RuntimeException(e);
      }


      inesctec.inestec.paris.model.Connector receivedConnector = (inesctec.inestec.paris.model.Connector) map.get("payload");
      System.out.println(receivedConnector);
      Optional<inesctec.inestec.paris.model.Connector> conn_Op=connectorService.findConnectorByConnectorIdsID(receivedConnector.getConnectorID().toString());
      assertTrue(conn_Op.isPresent());

      //assertEquals(participantConverter.convertToDatabaseParticipant(receivedParticipant), part_Op.get());

      System.out.println(receivedConnector);
      System.out.println(conn_Op.get().toJson());
      assertEquals(receivedConnector, conn_Op.get());



    } catch (SerializeException | MessageHandlerException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void handleMessage_Fail_WrongTableName() {

    try {
      String query = "select * from participants";

      MessagePayload messagePayload = new MessagePayloadInputstream(IOUtils.toInputStream(query), new ObjectMapper());

      MessageResponse messageResponse = handler.handleMessage((QueryMessageImpl) message, messagePayload);

      map = messageResponse.createMultipartMap(serializer);
      System.out.println(map.toString());
      try {
        final var requestHeader = serializer.deserialize((String) map.get("header"), RejectionMessage.class);
        assertInstanceOf(RejectionMessage.class, requestHeader);
        Assertions.assertTrue(RejectionMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return RejectionMessage. It returns " + requestHeader.getClass());

        assertEquals(((RejectionMessage) requestHeader).getRejectionReason(), RejectionReason.BAD_PARAMETERS);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      assertEquals(map.get("payload").toString(), "Invalid Value");

      } catch (SerializeException | MessageHandlerException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void handleMessage_Fail_NotInsert() {

    try {
      String query="describe participants";

      MessagePayload messagePayload=new MessagePayloadInputstream(IOUtils.toInputStream(query), new ObjectMapper());

      MessageResponse messageResponse=handler.handleMessage((QueryMessageImpl) message,messagePayload);

      map=messageResponse.createMultipartMap(serializer);
      System.out.println(map.toString());
      try {
        final var requestHeader = serializer.deserialize((String) map.get("header"), RejectionMessage.class);
        assertInstanceOf(RejectionMessage.class, requestHeader);
        Assertions.assertTrue(RejectionMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return RejectionMessage. It returns " + requestHeader.getClass());

        assertEquals(((RejectionMessage) requestHeader).getRejectionReason(), RejectionReason.BAD_PARAMETERS);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      assertEquals(map.get("payload").toString(), "Invalid Query");
      //IdsMessageUtils.buildSelfDeclaration()

    } catch (SerializeException | MessageHandlerException e) {
      throw new RuntimeException(e);
    }
  }

}