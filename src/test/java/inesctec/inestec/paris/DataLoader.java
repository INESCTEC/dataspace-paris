package inesctec.inestec.paris;

import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.CertificationLevel;
import de.fraunhofer.iais.eis.LeftOperand;
import inesctec.inestec.paris.builder.*;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.model.*;
import inesctec.inestec.paris.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.*;
import java.util.*;

@Component
public class DataLoader implements ApplicationRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(DataLoader.class);


    EvaluationFacilityService evaluationFacilityService;
    PersonService personService;
    ParticipantService participantService;
    ParticipantCertificationService participantCertificationService;
    LogicalConstraintService logicalConstraintService;
    ConstraintService constraintService;
    ConnectorService connectorService;
    BusinessIdentifierService businessIdentifierService;


    @Autowired
    public DataLoader(EvaluationFacilityService evaluationFacilityService, PersonService personService,
                      ParticipantService participantService, ConstraintService constraintService,
                      ParticipantCertificationService participantCertificationService,
                      LogicalConstraintService logicalConstraintService, ConnectorService connectorService,
                      BusinessIdentifierService businessIdentifierService) {
        this.evaluationFacilityService = evaluationFacilityService;
        this.personService = personService;
        this.participantService = participantService;
        this.participantCertificationService = participantCertificationService;
        this.constraintService = constraintService;
        this.logicalConstraintService = logicalConstraintService;
        this.connectorService=connectorService;
        this.businessIdentifierService=businessIdentifierService;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("EV: "+evaluationFacilityService.size());
        System.out.println("personService: "+personService.size());
        System.out.println("participantService: "+participantService.size());
        System.out.println("certificationService: "+participantCertificationService.size());
        System.out.println("logicalConstraintService: "+logicalConstraintService.size());
        System.out.println("constraintService: "+constraintService.size());
        System.out.println("connectorService: "+connectorService.size());
        System.out.println("businessIdentifierService: "+businessIdentifierService.size());
        if(evaluationFacilityService.size()==0 && personService.size()==0
        && participantService.size()==0 && participantCertificationService.size()==0
        && logicalConstraintService.size()==0 && constraintService.size()==0 && connectorService.size()==0
        && businessIdentifierService.size()==0){
            loadDatabase();
        }
    }

    @Transactional
    public void loadDatabase(){
        LOGGER.info("Populating Database...");
        // Create EvaluationFacility Object

        List<EvaluationFacility> evaluationFacilityList = evaluationFacility();

        // Create ParticipantCertification Object, it takes the evaluationFacilityList as argument
        List<ParticipantCertification> participantCertification = participantCertification(evaluationFacilityList);

        /*
         *   Save EvaluationFacility to Database
         * */
        LOGGER.debug("Saving Evaluation Facility on Database ");
        evaluationFacilityService.save(evaluationFacilityList);


        /*
         * Set 'Certification' on EvaluationFacility
         * ParticipantCertification 1 was issued by evaluationFacilityList "Facility 1"
         * ParticipantCertification 2 was issued by evaluationFacilityList "Facility 2"
         * */
        Set<ParticipantCertification> certificationSet=new HashSet<>();
        certificationSet.add(participantCertification.get(0));
        Set<ParticipantCertification> certificationSet2=new HashSet<>();
        certificationSet2.add(participantCertification.get(1));

        evaluationFacilityList.get(0).setCertifications(certificationSet);
        evaluationFacilityList.get(1).setCertifications(certificationSet2);
        /*evaluationFacilityService
                .findByEvaluationFacilityID(URI.create("https://evaluation/facility/ids/id/facility-01"))
                .get().setCertifications(certificationSet);
        evaluationFacilityService
                .findByEvaluationFacilityID(URI.create("https://evaluation/facility/ids/id/facility-01"))
                .get().setCertifications(certificationSet2);*/


        /*
         * Create Person Object
         * Create Connector Object
         * Create Business Identifier Object
         * Create Participants Object
         * */
        List<Person> personList = person();
        List<Connector> connectorList = connector();
        List<BusinessIdentifier> businessIdentifierList=businessIdentifier();
        List<Participant1> participants = participant(participantCertification, personList);

        URI producerURI=URI.create("https://stvgodigital.pt/ids/participant/id/producer-01");
        URI subcontractedURI=URI.create("https://stvgodigital.pt/ids/participant/id/subcontracted-01");

        Participant1 producer=participants.stream()
                .filter(v -> v.getParticipantIdsID()
                        .equals(producerURI))
                .findFirst()
                .orElseThrow(()->new ResourceNotFoundException("Participant", "ID", String.valueOf(producerURI)));

        Participant1 subcontracted=participants.stream()
                .filter(v -> v.getParticipantIdsID()
                        .equals(subcontractedURI))
                .findFirst()
                .orElseThrow(()->new ResourceNotFoundException("Participant", "ID", String.valueOf(subcontractedURI)));


        /*
        Add Participant "Provider" for Participant Certification 2
        Add Participant "Subcontracted" for Participant Certification 1
        */
        participantCertification.get(1).setParticipant(producer);
        participantCertification.get(0).setParticipant(subcontracted);


        /*
        Add Participant Certification 1 on Participant "Provider"
        Add Participant Certification 0 on Participant "Subcontracted"
        */
        participants.stream().filter(v -> v.getParticipantIdsID()
            .equals(producerURI))
            .findFirst().get().setParticipantCertification(participantCertification.get(1));

        participants.stream().filter(v -> v.getParticipantIdsID()
                .equals(subcontractedURI))
            .findFirst().get().setParticipantCertification(participantCertification.get(0));

        //***************************************************************************

        Set<Person> memberPersonSet_Participant = new HashSet<>();
        memberPersonSet_Participant.add(personList.get(0));
        memberPersonSet_Participant.add(personList.get(2));

        Set<Person> memberPersonSet_Participant2 = new HashSet<>();
        memberPersonSet_Participant2.add(personList.get(1));


        /*
        Add Participant "Provider" on Person 1
        Add Participant "Subcontracted" on Person 2
        */
        personList.get(0).setParticipant(producer);
        personList.get(2).setParticipant(producer);
        personList.get(1).setParticipant(subcontracted);


        /*
        Add Person 1 on Participant "Provider"
        Add Person 2 on Participant "Subcontracted"
        */
        /*participants.stream().filter(v -> v.getParticipantIdsID()
                .equals(producerURI))
            .findFirst().get().setMemberPerson(memberPersonSet_Participant);

        participants.stream().filter(v -> v.getParticipantIdsID()
                .equals(subcontractedURI))
            .findFirst().get().setMemberPerson(memberPersonSet_Participant2);*/

        /*
        Add Participant "Provider" on Connector "Provider"
        Add Participant "Subcontracted" on Connector "Subcontracted"
        */

        connectorList.stream().filter(
                        v->v.getConnectorID().toString().equals("https://stvgodigital.pt/ids/connector/id/producer-01"))
                .forEach(v->
                        v.setParticipantSet(producer));
        connectorList.stream().filter(
                        v->v.getConnectorID().toString().equals("https://stvgodigital.pt/ids/connector/id/subcontracted-01"))
                .forEach(v->
                        v.setParticipantSet(subcontracted));
        /*
        Add Connector "Provider" on Participant "Provider"
        Add Connector "Subcontracted" on Participant "Subcontracted"
        */
        participants.stream().filter(v -> v.getParticipantIdsID()
                        .equals(producerURI)).findFirst().get()
                .setConnectorSet(Set.of(connectorList.stream()
                        .filter(v -> v.getConnectorID().toString()
                                .equals("https://stvgodigital.pt/ids/connector/id/producer-01"))
                        .findFirst().get()));

        participants.stream().filter(v -> v.getParticipantIdsID()
                        .equals(subcontractedURI))
                .findFirst().get().setConnectorSet(Set.of(connectorList.stream()
                        .filter(v -> v.getConnectorID().toString().equals("https://stvgodigital.pt/ids/connector/id/subcontracted-01"))
                        .findFirst().get()));
        //connectorList.stream().forEach(System.out::println);

        /*
        Add BusinessIdentifier 1 on Participant "Provider"
        Add BusinessIdentifier 2 on Participant "Subcontracted"
        */
        Set<BusinessIdentifier> auxBusinessIdentifierSet=new HashSet<>();
        auxBusinessIdentifierSet.add(businessIdentifierList.get(0));
        participants.stream().filter(v -> v.getParticipantIdsID()
                        .equals(producerURI))
                .findFirst().get().setBusinessIdentifier(auxBusinessIdentifierSet);

        auxBusinessIdentifierSet.clear();
        auxBusinessIdentifierSet.add(businessIdentifierList.get(1));
        participants.stream().filter(v -> v.getParticipantIdsID()
                        .equals(subcontractedURI))
                .findFirst().get().setBusinessIdentifier(auxBusinessIdentifierSet);

        Constraint constraint=new ConstraintBuilder()
                .constraint_ids_ID("http://inesctec/constraint/id/01")
                .unit("http://dbpedia.org/resource/Euro")
                .leftOperand(LeftOperand.SECURITY_LEVEL)
                .operator(String.valueOf(BinaryOperator.EQUALS))
                .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK").build();
        Constraint constraint2=new ConstraintBuilder()
                .constraint_ids_ID("http://inesctec/constraint/id/02")
                .unit("http://dbpedia.org/resource/Euro")
                .leftOperand(LeftOperand.SECURITY_LEVEL)
                .operator(String.valueOf(BinaryOperator.EQUALS))
                .rightOperand("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK").build();
        Constraint constraint3=new ConstraintBuilder()
                .constraint_ids_ID("http://inesctec/constraint/id/03")
                .leftOperand(LeftOperand.SECURITY_LEVEL)
                .operator(String.valueOf(BinaryOperator.EQUALS))
                .rightOperandReference("http://right/operand/reference/test").build();

        LogicalConstraint logicalConstraint =
                new LogicalConstraintBuilder(URI.create("https://stvgodigital.pt/ids/participant/refinement/id/constraint-01"),
                        constraint)
                        .participant(subcontracted)
                        .build();

                /*new LogicalConstraint(
                URI.create("https://stvgodigital.pt/ids/participant/refinement/id/constraint-01"),constraint);*/


        //logicalConstraint.setParticipantSet(subcontracted);

        participants.stream().filter(v -> v.getParticipantIdsID()
                .equals(subcontractedURI)).findFirst().get()
                .setParticipantRefinement(logicalConstraint);

        /*participants.stream().filter(v -> v.getParticipantIdsID()
                        .equals(subcontracted))
                .findFirst().get().setParticipantLogicalConstraint(logicalConstraint);*/

        constraint.setLogicalConstraint(logicalConstraint);
        /*System.err.println(constraint);
        System.err.println(logicalConstraint);*/

        //participants.stream().forEach(v-> {System.out.println(v);});



        /*
        Insert Participant on Database
        Insert Participant Certification on Database -> Order needs to be this
        */
        participantService.save(participants);

        //logicalConstraintService.save(logicalConstraint);
        //constraintService.save(constraint);

        //participantCertificationService.save(participantCertification);


        /*
        Insert Person on Database
        */
        //personService.save(personList);

        /*
        Insert Business Identifier on Database
        */
        businessIdentifierService.save(businessIdentifierList);

        /*
        Insert Connector on Database
        */
        connectorService.save(connectorList);

        LOGGER.info("... Database Populated.");


    }

    private List<Connector> connector() {
        URI producerURI=URI.create("https://stvgodigital.pt/ids/connector/id/producer-01");
        URI subcontractedURI=URI.create("https://stvgodigital.pt/ids/connector/id/subcontracted-01");

        List<Connector> connectorList=new ArrayList<>();
        connectorList.add(new Connector(producerURI, "b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff",
                URI.create("https://vcese11.inesctec.pt:8080/producer/api/ids/data")));
        connectorList.add(new Connector(subcontractedURI, "20bcebe2-694e-49c5-b2d9-8070e5b09c69",
                URI.create("https://vcese12.inesctec.pt:8080/subcontracted/api/ids/data")));
        return connectorList;
    }

    private List<EvaluationFacility> evaluationFacility() {
        List<EvaluationFacility> evaluationFacility = new ArrayList<>();
        evaluationFacility.add(new EvaluationFacility(
            URI.create("https://evaluation/facility/ids/id/facility-01")));
        evaluationFacility.add(new EvaluationFacility(
            URI.create("https://evaluation/facility/ids/id/facility-02")));

        //evaluationFacility.stream().forEach(v->System.out.println(v.toString()));

        return evaluationFacility;
    }

    private List<ParticipantCertification> participantCertification(List<EvaluationFacility> evaluationFacilityList) {
        ZonedDateTime certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
        ZonedDateTime certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

        List<ParticipantCertification> participantCertification = new ArrayList<>();

        /*
        * ParticipantCertification 1 was issued by evaluationFacilityList "Facility 1"
        * */
        participantCertification.add(new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .evaluationFacility(evaluationFacilityList.get(0))
            .membershipEnd(certificationEndDate)
                .ids_id("https://stvgodigital.pt/ids/participantCertification/id/01")
            .build());

        /*
         * ParticipantCertification 2 was issued by evaluationFacilityList "Facility 2"
         * */
        participantCertification.add(new ParticipantCertificationBuilder()
            .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .lastValidDate(certificationDate)
            .evaluationFacility(evaluationFacilityList.get(1))
            .membershipEnd(certificationEndDate)
                .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                .title("Participant Certification 2")
                .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
            .build());

        //participantCertification.stream().forEach(v->System.out.println(v.toString()));
        return participantCertification;
    }

    private List<Person> person() {
        List<String> phoneNumberList_Person=new ArrayList<>();
        phoneNumberList_Person.add("923452876");

        List<String> emailList_Person=new ArrayList<>();
        emailList_Person.add("john.doe@gmail.com");
        emailList_Person.add("john.doe@hotmail.com");

        List<Person> person = new ArrayList<>();
        person.add(new PersonBuilder()
            .familyName("Doe")
            .givenName("John")
            .phoneNumber(phoneNumberList_Person)
            .email(emailList_Person)
            .homepage("www.john-doe.com")
                .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                .build());

        person.add(new PersonBuilder("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6642").familyName("Doe2").givenName("John2").phoneNumber(phoneNumberList_Person).email(emailList_Person).homepage("www.test.com").build());

        person.add(new PersonBuilder().ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddegdhaksdjhw8").familyName("Doe3").givenName("John3").phoneNumber(phoneNumberList_Person).email(emailList_Person).homepage("www.test-3.com").build());

        //persons.stream().forEach(v->System.out.println(v.toString()));
        return person;
    }

    private List<Participant1> participant(List<ParticipantCertification> participantCertification,
                                           List<Person> person) {

        List<Participant1> participants = new ArrayList<>();

        /*
            Create Participant 1 - Producer
        * */
        // If a mandatory field of a Participant is not instantiates, a error is thorwn when saving in DB
        Participant1 participant= new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"), participantCertification.get(0))
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            //participant.setParticipantRefinement("");
            .primarySite("Street X")
            .vatID("123456789")
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .build();

        /*participant.setMemberParticipant("Producer");
        participant.setChildren("Producer");*/

        //System.err.println("Participant 1: "+participant.toString());

        /*
            Create Participant 2 - Subcontracted
        */
        Participant1 participant2= new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/subcontracted-01"),
            List.of("Subcontracted"), participantCertification.get(1)
        )
            .name(List.of("Subcontracted"))
            .corporateEmailAddress(List.of("subcontracted@inesctec.pt"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            .primarySite("Street X")
            .vatID("123456788")
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .description(List.of("Participant called Subcontracted, with id https://stvgodigital.pt/ids/participant/id/subcontracted-01"))
            //.memberParticipant(participant)
            .build();

        //participant2.setMemberParticipant(participant);


        // Add Participants to List
        participants.add(participant);
        participants.add(participant2);


        return participants;
    }
    private List<BusinessIdentifier> businessIdentifier() {
        List<BusinessIdentifier> businessIdentifierList=new ArrayList<>();
        businessIdentifierList.add(new BusinessIdentifierBuilder()
                .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                .identifierSystem("businessIdentifierSystem")
                .identifierNumber("businessIdentifierNumber")
                .build());

        businessIdentifierList.add(new BusinessIdentifierBuilder()
                .ids_ID("https://texpact.pt/ids/businessIdentifier/id/subcontracted-01")
                .identifierSystem("businessIdentifierSystem")
                .identifierNumber("businessIdentifierNumber")
                .build());
        return businessIdentifierList;
    }
}
