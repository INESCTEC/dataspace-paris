package inesctec.inestec.paris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.fraunhofer.iais.eis.CertificationLevel;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.SiteBuilder;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.DataLoader;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.converter.ParticipantConverter;
import inesctec.inestec.paris.converter.XMLGregorianCalendarConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.util.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ParticipantController.class)
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
class ParticipantControllerTestMock {

    String uri="/api";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipantService service;
    @Autowired
    JsonConverter jsonConverter;
    @MockBean
    DataLoader dataLoader;
    @MockBean EvaluationFacilityService evaluationFacilityService;
    @MockBean PersonService personService;
    @MockBean ConnectorService connectorService;
    @MockBean
    private ParticipantCertificationService participantCertificationService;
    @MockBean
    private LogicalConstraintService logicalConstraintService;
    @MockBean
    ConstraintService constraintService;
    @MockBean
    BusinessIdentifierService businessIdentifierService;

    @MockBean
    private TestUtil testUtil;

    @MockBean
    private ParticipantConverter converter;
    @MockBean
    private ParticipantUtil participantUtil;
    @MockBean
    JdbcTemplate jdbcTemplate;

    /**
     * Tests Endpoint GET /api/participants
     * Test creates a list of Participants and calls the endpoint to see if they are the same
     *
     * @return a list of Infomodel Participants
    * */
    @Test @WithMockUser(value = "testUser")
    public void getListOfParticipants_shouldReturnListFromService() throws Exception {

        List<Participant1> participantList=new ArrayList<>();
        testUtil=new TestUtil();
        participantList.add(testUtil.addProducer());
        participantList.add(testUtil.addSubcontracted());

        List<ParticipantFraunDto> participantFraunDtoList=convertToFraunfDto(participantList);

        AtomicReference<String> response=testUtil.formatMockTestsResponse(participantFraunDtoList);

        when(service.findAll()).thenReturn(participantList);
        this.mockMvc.perform(get(uri+"/participants")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(response.get()));
    }

    /**
    * Tests Endpoint GET /api/participant
    *
     * This Participant does not have any Connector associated
    * @return an Infomodel Participant
    * */
    @Test @WithMockUser(value = "testUser")
    public void getParticipant_UsingID_shouldReturnInfomodelParticipant() throws Exception {
        // Add Producer Participant and convert it to Infomodel Participant
        testUtil = new TestUtil();
        Participant1 producer = testUtil.addProducer();
        ParticipantFraunDto participantFraunDto = convertToFraunfDto(producer);

        System.out.println("PARTICIPANT: \n"+participantFraunDto);

        // Make the test
        when(service.findParticipantByParticipantIDSID(producer.getParticipantIdsID()))
                .thenReturn(Optional.of(producer));
        this.mockMvc.perform(get(uri + "/participants")
                        .param("participantId", String.valueOf(producer.getParticipantIdsID())))
                .andDo(print())
                .andExpect(status().isOk())
                //.andExpect(content().string(participantFraunDto.toSimpleJson()))
        ;
    }

    @Test @WithMockUser(value = "testUser")
    public void shouldReturnDefaultMessage() throws Exception {
        JSONObject json = new JSONObject();
        json.put("status", true);
        this.mockMvc.perform(get("/health")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(json.toString()));
    }

    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ParticipantFraunDto> convertToFraunfDto(List<Participant1> participantList) {
        converter=new ParticipantConverter(service, participantCertificationService, logicalConstraintService, personService, businessIdentifierService);
        List<ParticipantFraunDto> participantFraunDtoList=new ArrayList<>();
        participantList.stream()
                .forEach(v-> participantFraunDtoList.add(converter.convertToParticipantFraunDto(v)));

        return participantFraunDtoList;
    }

    public ParticipantFraunDto convertToFraunfDto(Participant1 participant) {
        return new ParticipantConverter(service, participantCertificationService, logicalConstraintService, personService, businessIdentifierService)
                .convertToParticipantFraunDto(participant);
    }


    /**
     * Tests Endpoint POST /api/participants
     *
     * @return an Infomodel Participant
     * */
   // @Test @WithMockUser(value = "testUser")
    public void addParticipant() throws Exception {
        // TODO
        // Add Producer Participant and convert it to Infomodel Participant
        testUtil = new TestUtil();
        XMLGregorianCalendarConverter calendarConverter=new XMLGregorianCalendarConverter();
        ZonedDateTime certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
        ZonedDateTime certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));


        Participant participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer"))
                ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
                ._legalName_("Fraunhofer-Institut")
                ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
                ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
                ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
                ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/02"))
                        ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        ._lastValidDate_(calendarConverter.convertToXMLGregorianCalendar(certificationDate))
                        ._membershipEnd_(calendarConverter.convertToXMLGregorianCalendar(certificationEndDate))
                        ._title_(new TypedLiteral("Participant Certification 3"))
                        ._description_(new TypedLiteral("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK))
                        ._evaluationFacility_(new de.fraunhofer.iais.eis.EvaluationFacilityBuilder(URI.create("https://ev/02")).build())
                        .build())
                ._version_("3.0")
                ._legalForm_("e.V.")
                .build();
        Participant1 convertedPart=converter.convertToDatabaseParticipant(participant);


        System.out.println(participant);
        // Make the test
        when(participantUtil.addParticipant(participant))
                .thenReturn(convertedPart);
        this.mockMvc.perform(
                post(uri + "/participants")
                        .content(asJsonString(participant.toRdf()))
                        //.content(IOUtils.toInputStream(participant.toRdf()).readAllBytes())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isCreated())
        //.andExpect(content().string(participantFraunDto.toSimpleJson()))
        ;

    }



}