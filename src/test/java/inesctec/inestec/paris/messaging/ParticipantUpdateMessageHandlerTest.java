package inesctec.inestec.paris.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.*;
import ids.messaging.handler.message.MessagePayload;
import ids.messaging.handler.message.MessagePayloadInputstream;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import ids.messaging.util.IdsMessageUtils;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
class ParticipantUpdateMessageHandlerTest {


  MessageProcessedNotificationMessage messageProcessedNotification;

  Map<String, Object> map;
  Serializer serializer;

  TestUtil testUtil=new TestUtil();
  @Autowired private ParticipantUtil participantUtil;
  @Autowired private EvaluationFacilityService evaluationFacilityService;
  @Autowired private PersonService personService;
  @Autowired private ParticipantService participantService;
  @Autowired private ParticipantCertificationService certificationService;
  @Autowired private ConstraintService constraintService;
  @Autowired private LogicalConstraintService logicalConstraintService;
  @Autowired private BusinessIdentifierService businessIdentifierService;
  @Autowired private ConnectorService connectorService;

  @Autowired
  private ParticipantUpdateMessageHandler handler;

  @Mock
  private ConfigContainer connectorConfig;

  @Mock
  DapsTokenProvider provider;

  @MockBean
  private ConfigurationModel configurationModel;

  @MockBean
  private DapsValidator dapsValidator;

  private ParticipantUpdateMessage message;
  private ParticipantConverter converter;
  MessagePayload messagePayload;

  @BeforeEach
  void setUp() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException, DatatypeConfigurationException, URISyntaxException {

    converter=new ParticipantConverter(participantService, certificationService, logicalConstraintService,personService, businessIdentifierService);
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

    handler=new ParticipantUpdateMessageHandler(connectorConfig, provider, new JsonConverter(),
            evaluationFacilityService, personService, participantService, certificationService,
            constraintService, logicalConstraintService, businessIdentifierService, connectorService, participantUtil);

    serializer=new Serializer();
    

    // Create the Header to send on the handleMessage()
    message=new ParticipantUpdateMessageBuilder()
            ._securityToken_(provider.getDAT())
            //._correlationMessage_(message.getId())
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._issuerConnector_(connectorConfig.getConnector().getId())
            ._modelVersion_(this.connectorConfig.getConnector().getOutboundModelVersion())
            ._senderAgent_(connectorConfig.getConnector().getId())
            ._affectedParticipant_(URI.create("https://participant/id/test/connector/fraunhofer"))
            //._recipientAgent_(message.getRecipientAgent())
            .build();

    messageProcessedNotification= new MessageProcessedNotificationMessageBuilder(URI.create("https://test-paris.com/messageProcessedNotification/id/122454578777"))
            ._securityToken_(provider.getDAT())
            ._correlationMessage_(message.getId())
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._issuerConnector_(connectorConfig.getConnector().getId())
            ._modelVersion_(connectorConfig.getConnector().getOutboundModelVersion())
            ._senderAgent_(connectorConfig.getConnector().getId())
            .build();

    Participant participant= new ParticipantBuilder(URI.create("https://test"))
            ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
            ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
            ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
            ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
            ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
            ._legalName_(Util.asList("Fraunhofer IAIS"))
            ._memberParticipant_(Util.asList(new ParticipantBuilder(
                    new URI("https://www.fraunhofer.de/"))
                    ._legalForm_("e.V.").build()))
            ._participantCertification_(new ParticipantCertificationBuilder()
                    ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    ._lastValidDate_(
                            DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0)).build())
            ._memberPerson_(Util.asList(new PersonBuilder()
                    ._familyName_("Mueller")
                    ._givenName_("Peter")
                    ._emailAddress_(Util.asList("mueller.peter@emailcontact.de"))
                    ._homepage_("https://MemberPerson.de/mueller_peter")
                    ._phoneNumber_(Util.asList("017894561237"))
                    .build()))
            ._version_("3.0")
            ._legalForm_("e.V.")
            .build();

    // Create the Payload to send on the handleMessage()
    messagePayload=new MessagePayloadInputstream(IOUtils.toInputStream(participant.toString()), new ObjectMapper());
  }

  @Test
  @Order(1)
  void handleMessage() throws DatatypeConfigurationException, URISyntaxException, SerializeException {

    // Send the Message
    MessageResponse messageResponse=handler.handleMessage((ParticipantUpdateMessageImpl) message,messagePayload);

    // Get the Response
    map=messageResponse.createMultipartMap(serializer);
    System.out.println(map.toString());
    try {
      final var requestHeader = serializer.deserialize((String) map.get("header"), MessageProcessedNotificationMessage.class);
      assertInstanceOf(MessageProcessedNotificationMessage.class, requestHeader);
      Assertions.assertTrue(MessageProcessedNotificationMessage.class.isAssignableFrom(requestHeader.getClass()),
              "Method should return MessageProcessedNotificationMessage. It returns "+ requestHeader.getClass());
    }
    catch (IOException e){
      throw new RuntimeException(e);
    }

    try {
      Participant payload = serializer.deserialize(map.get("payload").toString(), Participant.class);

      // Check if Connector was inserted
      // Get Connector Model
      System.out.println(payload.toRdf());
      assertNotNull(payload);

      Optional<inesctec.inestec.paris.model.Participant1> insertedParticipant_OP=participantService.findParticipantByParticipantIDSID(payload.getId());
      assertTrue(insertedParticipant_OP.isPresent());

    }catch (IOException e){
      // Assertation made to fail
      Assertions.assertEquals(Connector.class, "Error while deserializing payload", "Method should return a Connector.");
    }

    //IdsMessageUtils.buildSelfDeclaration()

  }

  @Test
  @Order(2)
  void handleMessage_AlreadyInserted() throws DatatypeConfigurationException, URISyntaxException, SerializeException {

    // Send the Message
    MessageResponse messageResponse=handler.handleMessage((ParticipantUpdateMessageImpl) message,messagePayload);

    // Get the Response
    map=messageResponse.createMultipartMap(serializer);
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

    assertEquals(map.get("payload").toString(), "Participant with that ID already exists");

    //IdsMessageUtils.buildSelfDeclaration()

  }

}