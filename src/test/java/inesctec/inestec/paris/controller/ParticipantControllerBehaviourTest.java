package inesctec.inestec.paris.controller;

import inesctec.inestec.paris.DataLoader;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.service.*;
import inesctec.inestec.paris.converter.ParticipantConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
/*@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")*/
class ParticipantControllerBehaviourTest {

    @Autowired
    ParticipantService service;
    @Autowired
    ParticipantCertificationService certificationService;
    @Autowired
    LogicalConstraintService logicalConstraintService;
    @Autowired
    PersonService personService;
    @Autowired
    BusinessIdentifierService businessIdentifierService;

    @Autowired
    DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        System.out.println(service.findAll().toString());
    }

    @Test
    void getListOfParticipantsID() {
        List participantsIDs=new ArrayList();
        service.findAll().forEach(p -> participantsIDs.add(p.getParticipantIdsID()));
        System.out.println("Size: "+participantsIDs.size());
        assertNotEquals(participantsIDs.size(), 0);
    }

    @Test
    void getParticipant() {
        List<ParticipantFraunDto> participants=new ArrayList();
        service.findAll().forEach(p->{
            participants.add(new ParticipantConverter(service, certificationService, logicalConstraintService, personService, businessIdentifierService)
                    .convertToParticipantFraunDto(p));
        });

        assertNotEquals(participants.size(), 0);
    }

    @Test
    void getParticipant_checkValues() {
        List<ParticipantFraunDto> participantsFraunf=new ArrayList();
        List<Participant1> participants=new ArrayList();
        service.findAll().forEach(p->{
            participantsFraunf.add(new ParticipantConverter(service, certificationService, logicalConstraintService, personService, businessIdentifierService)
                    .convertToParticipantFraunDto(p));
            participants.add(p);
        });
        //participant1s.forEach(System.out::println);

        assertNotEquals(participants.size(), 0);

        participantsFraunf.forEach(p -> {
            System.out.println(p);
            //equals subcontracted

        });
    }

    /**
     * Class ParticipantInsertationTest already tests the AppParticipant
     *
    * */
}