package inesctec.inestec.paris.messaging;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.*;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import ids.messaging.util.IdsMessageUtils;
import inesctec.inestec.paris.DataLoader;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.util.TestUtil;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql({"/remove_data.sql"})
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties",
        properties = {
                "application.runner.enabled=true"})
class DescriptionRequestMessageHandlerTest {

  Map<String, Object> map;
  Serializer serializer;
  TestUtil testUtil;

  @Autowired private ParticipantService service;
  @Autowired private ParticipantCertificationService certificationService;
  @Autowired private LogicalConstraintService logicalConstraintService;
  @Autowired private PersonService personService;
  @Autowired private BusinessIdentifierService businessIdentifierService;
  private ParticipantConverter participantConverter;

  @Autowired
  DataLoader dataLoader;
  @Autowired
  private DescriptionRequestMessageHandler handler;

  @Mock
  private ConfigContainer connectorConfig;

  @Mock
  DapsTokenProvider provider;

  @MockBean
  private ConfigurationModel configurationModel;

  @MockBean
  private DapsValidator dapsValidator;

  DescriptionRequestMessage message;

  URI participantID;

  @BeforeEach
  void setUp() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
    testUtil=new TestUtil();
    Connector connector=testUtil.createConnectorToTest();
    participantID=URI.create("https://stvgodigital.pt/ids/participant/id/producer-01");
    participantConverter=new ParticipantConverter(service, certificationService, logicalConstraintService, personService, businessIdentifierService);

    DynamicAttributeToken dat=new DynamicAttributeTokenBuilder()._tokenValue_("Fake token")._tokenFormat_(TokenFormat.JWT).build();

    Mockito.when(connectorConfig.getConnector()).thenReturn(connector);
    Mockito.when(connectorConfig.getConfigurationModel()).thenReturn(configurationModel);
    Mockito.when(connectorConfig.getConfigurationModel().getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
    Mockito.when(provider.provideDapsToken()).thenReturn("Mock Token");
    Mockito.when(dapsValidator.checkDat(dat)).thenReturn(true);
    Mockito.when(provider.getDAT()).thenReturn(dat);

    handler=new DescriptionRequestMessageHandler(connectorConfig, provider, service, certificationService,
            logicalConstraintService, personService, businessIdentifierService);

    serializer=new Serializer();
    dataLoader.loadDatabase();

    try {


      // Create the Header to send on the handleMessage()
      message=new DescriptionRequestMessageBuilder()
              ._securityToken_(provider.getDAT())
              ._issued_(IdsMessageUtils.getGregorianNow())
              ._issuerConnector_(connectorConfig.getConnector().getId())
              ._modelVersion_(this.connectorConfig.getConnector().getOutboundModelVersion())
              ._senderAgent_(connectorConfig.getConnector().getId())
              ._recipientConnector_(connector.getHasDefaultEndpoint().getAccessURL())
              ._requestedElement_(participantID)
              .build();

      //IdsMessageUtils.buildSelfDeclaration()

    } catch (ConnectorMissingCertExtensionException | DapsConnectionException | DapsEmptyResponseException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(1)
  void handleMessage() {

    try {
      /*System.out.println(service.size());
      service.findAll().forEach(p->System.out.println(p.getParticipantIdsID()));*/
      // Send the Message
      MessageResponse messageResponse = handler.handleMessage((DescriptionRequestMessageImpl) message, null);

      // Get the Response
      map = messageResponse.createMultipartMap(serializer);
      //System.out.println(map.toString());
      try {
        System.out.println(map.get("header").getClass());
        System.out.println(map.get("header").toString());
        final var requestHeader = serializer.deserialize((String) map.get("header"), ResultMessage.class);
        assertInstanceOf(ResultMessage.class, requestHeader);
        Assertions.assertTrue(ResultMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return ResultMessageBuilder. It returns " + requestHeader.getClass());
      } catch (IOException e) {
       throw new RuntimeException(e);
      }

      try {
        System.out.println("Payload: "+map.get("payload").toString());
        //Participant requestPayload= CommunicationUtil.deserializeParticipant(map.get("payload").toString());
        Participant requestPayload = serializer.deserialize(map.get("payload").toString(), Participant.class);
        assertInstanceOf(Participant.class, requestPayload);
        Assertions.assertTrue(Participant.class.isAssignableFrom(requestPayload.getClass()),
                "Method should return Participant. It returns " + requestPayload.getClass());
        System.out.println(requestPayload);

        assertTrue(CompareUtil.compareFraunhParticipants(requestPayload,
                participantConverter.convert(service.findParticipantByParticipantIDSID(participantID).get())));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    } catch (SerializeException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(2)
  void handleMessage_ParticipantIsInvalid() {
    try {
      // Change the Participat to null
      message.setRequestedElement(null);
      // Send the Message
      MessageResponse messageResponse = handler.handleMessage((DescriptionRequestMessageImpl) message, null);

      // Get the Response
      map = messageResponse.createMultipartMap(serializer);
      System.out.println(map.toString());
      try {
        final var requestHeader = serializer.deserialize((String) map.get("header"), RejectionMessage.class);
        assertInstanceOf(RejectionMessage.class, requestHeader);
        Assertions.assertTrue(RejectionMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return RejectionMessage. It returns " + requestHeader.getClass());

        assertEquals(((RejectionMessage) requestHeader).getRejectionReason(), RejectionReason.BAD_PARAMETERS);
      } catch (IOException e) {
        // Assertation made to fail
        Assertions.assertEquals(ErrorResponse.class, "Error while deserializing", "Method should return a ErrorResponse.");
      }

      assertEquals(map.get("payload").toString(), "Invalid Participant");

    } catch (SerializeException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(3)
  void handleMessage_ParticipantDoesNotExist() {
    try {
      // Change the Participate to one that does not exist
      message.setRequestedElement(URI.create("https://stvgodigital.pt/ids/participant/id/test/invalid/participant"));
      // Send the Message
      MessageResponse messageResponse = handler.handleMessage((DescriptionRequestMessageImpl) message, null);

      // Get the Response
      map = messageResponse.createMultipartMap(serializer);
      System.out.println(map.toString());
      try {
        final var requestHeader = serializer.deserialize((String) map.get("header"), RejectionMessage.class);
        assertInstanceOf(RejectionMessage.class, requestHeader);
        Assertions.assertTrue(RejectionMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return RejectionMessage. It returns " + requestHeader.getClass());

        assertEquals(((RejectionMessage) requestHeader).getRejectionReason(), RejectionReason.NOT_FOUND);
      } catch (IOException e) {
        // Assertation made to fail
        Assertions.assertEquals(ErrorResponse.class, "Error while deserializing", "Method should return a ErrorResponse.");
      }

      assertEquals(map.get("payload").toString(), "Participant Not Found");

    } catch (SerializeException e) {
      throw new RuntimeException(e);
    }

  }

}