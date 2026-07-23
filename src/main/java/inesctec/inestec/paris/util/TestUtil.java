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
package inesctec.inestec.paris.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.builder.ParticipantBuilder;
import inesctec.inestec.paris.builder.ParticipantCertificationBuilder;
import inesctec.inestec.paris.builder.PersonBuilder;
import inesctec.inestec.paris.dto.BusinessIdentifierDto;
import inesctec.inestec.paris.dto.Participant1Dto;
import inesctec.inestec.paris.dto.ParticipantCertificationDto;
import inesctec.inestec.paris.enums.IdentifierSystem;
import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import inesctec.inestec.paris.model.*;
import inesctec.inestec.paris.model.BusinessIdentifier;
import inesctec.inestec.paris.model.Connector;
import inesctec.inestec.paris.model.Constraint;
import inesctec.inestec.paris.model.LogicalConstraint;
import inesctec.inestec.paris.model.Person;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Class used to run the Mock tests
 * */
@Getter
@Setter
public class TestUtil {

    public TestUtil() {
    }

    public String toJson(){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .create();
        String json = gson.toJson(this);
        //System.out.println(json.toString());
        return json;
    }

    public Participant1 addParticipant2(){
        ZonedDateTime certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
        ZonedDateTime certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

        Participant1 producer= new ParticipantBuilder(
                URI.create("https://test.com/ids/participant/id/test-01"),
                List.of("Test-01"),
                new ParticipantCertificationBuilder()
                        .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        .lastValidDate(certificationDate)
                        .membershipEnd(certificationEndDate)
                        .ids_id("https://test.com/ids/participantCertification/id/02")
                        .title("test.com")
                        .build())
                .name( List.of("Test01"))
                .corporateEmailAddress(List.of("test@inesctec.pt"))
                .corporateHomepage(URI.create("http://test.com"))
                .primarySite("Street X")
                .vatID("123456789")
                .registryCourt("Portugal")
                .legalForm("e.V.")
                .version("1")
                .build();
        return producer;
    }

    public Participant1 addProducer(){
        ZonedDateTime certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
        ZonedDateTime certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

        Participant1 producer= new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"),
            List.of("Producer"),
            new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/02")
                    .title("Participant Certification 2")
                    .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .build())
            .name( List.of("Producer"))
            .corporateEmailAddress(List.of("producer@inesctec.pt"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            //participant.setParticipantRefinement("");
            .primarySite("Street X")
            .version("1")
            .vatID("123456789")
            /*.businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                    .identifierSystem("businessIdentifierSystem")
                    .identifierNumber("businessIdentifierNumber")
                    .build())*/
            .registryCourt("Portugal")
            .legalForm("e.V.")
            //.hasRole(URI.create("http://www.stvgodigital.pt"))
            .build();
        //System.out.println(producer);
        return producer;
    }

    public Participant1 addSubcontracted(){
        ZonedDateTime certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
        ZonedDateTime certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

        Participant1 subcontracted= new ParticipantBuilder(
            URI.create("https://stvgodigital.pt/ids/participant/id/subcontracted-01"),
            List.of("Subcontracted"),
            new ParticipantCertificationBuilder()
                    .certificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .lastValidDate(certificationDate)
                    .membershipEnd(certificationEndDate)
                    .version("1")
                    .ids_id("https://stvgodigital.pt/ids/participantCertification/id/01")
                    .title("Participant Certification 2")
                    .description("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                    .build())
            .name( List.of("Subcontracted"))
            .corporateEmailAddress(List.of("subcontracted@inesctec.pt"))
            .corporateHomepage(URI.create("http://www.stvgodigital.pt"))
            //participant.setParticipantRefinement("");
            .primarySite("Street X")
            .vatID("123456789")
            /*.businessIdentifier(new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                    .ids_ID("https://texpact.pt/ids/businessIdentifier/id/subcontracted-01")
                    .identifierSystem("businessIdentifierSystem")
                    .identifierNumber("businessIdentifierNumber")
                    .build())*/
            .registryCourt("Portugal")
            .legalForm("e.V.")
            .version("1")
            .build();
        return subcontracted;
    }

    public BusinessIdentifier businessIdentifier_Producer() {
        return new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                .ids_ID("https://texpact.pt/ids/businessIdentifier/id/producer-01")
                .identifierSystem("businessIdentifierSystem")
                .identifierNumber("businessIdentifierNumber")
                .build();
    }
    public BusinessIdentifier businessIdentifier_Subcontracted() {
        return new inesctec.inestec.paris.builder.BusinessIdentifierBuilder()
                .ids_ID("https://texpact.pt/ids/businessIdentifier/id/subcontracted-01")
                .identifierSystem("businessIdentifierSystem")
                .identifierNumber("businessIdentifierNumber")
                .build();
    }

    public String toSimpleJson(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .create();
        String json = gson.toJson(this);
        //System.out.println(json.toString());
        return json;
    }

    public AtomicReference<String> formatMockTestsResponse(List list) {
        AtomicReference<String> response= new AtomicReference<>("[");
        list.forEach(v-> response.set(response.get()+this.toSimpleJson()+","));
        response.set(response.get().substring(0, response.get().length()-1));
        response.set(response.get()+"]");

        return response;
    }

    public Participant1Dto addProducerDto(){
        ZonedDateTime certificationDate = ZonedDateTime.of(2022, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));
        ZonedDateTime certificationEndDate = ZonedDateTime.of(2025, 03, 23, 12, 23, 22, 0, ZoneId.of("UTC"));

        ParticipantCertificationDto participantCertificationDto=new ParticipantCertificationDto();
        participantCertificationDto.setCertificationLevel(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK);
        participantCertificationDto.setLastValidDate(certificationDate);
        participantCertificationDto.setMembershipEnd(certificationEndDate);
        participantCertificationDto.setCertificationIdsID(URI.create("https://stvgodigital.pt/ids/participantCertification/id/02"));
        participantCertificationDto.setTitle("Participant Certification 2");
        participantCertificationDto.setDescription("Participant with Certification Level "+CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK);

        Participant1Dto producer=new Participant1Dto();
        producer.setParticipantIdsID(URI.create("https://stvgodigital.pt/ids/participant/id/producer-01"));
        producer.setLegalName(List.of("Producer"));
        producer.setName(List.of("Producer"));
        producer.setCorporateEmailAddress(List.of("producer@inesctec.pt"));
        producer.setCorporateHomepage(URI.create("http://www.stvgodigital.pt"));
        producer.setParticipantCertification(participantCertificationDto);
        producer.setPrimarySite("Street X");
        producer.setVatID("123456789");
        producer.setRegistryCourt("Portugal");
        producer.setLegalForm("e.V.");
        producer.setVersion("1");
        //producer.setChildren();
        return producer;
    }
    public Set<Person> persons() {
        List<String> phoneNumberList_Person=new ArrayList<>();
        phoneNumberList_Person.add("923452876");

        List<String> emailList_Person=new ArrayList<>();
        emailList_Person.add("john.doe@gmail.com");
        emailList_Person.add("john.doe@hotmail.com");

        Set<Person> persons = new HashSet<>();
        persons.add(new PersonBuilder("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                .familyName("Doe")
                .givenName("John")
                .phoneNumber(phoneNumberList_Person)
                .email(emailList_Person)
                .homepage("www.john-doe.com")
                .build());

        persons.add(new PersonBuilder("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6642")
                .familyName("Doe2")
                .givenName("John2")
                .phoneNumber(phoneNumberList_Person)
                .email(emailList_Person)
                .homepage("www.test.com").build());

        //persons.stream().forEach(v->System.out.println(v.toString()));
        return persons;
    }

    public Person person() {
        List<String> phoneNumberList_Person=new ArrayList<>();
        phoneNumberList_Person.add("923452876");

        List<String> emailList_Person=new ArrayList<>();
        emailList_Person.add("john.doe@gmail.com");
        emailList_Person.add("john.doe@hotmail.com");

        return new PersonBuilder()
                .familyName("Doe")
                .givenName("John")
                .phoneNumber(phoneNumberList_Person)
                .email(emailList_Person)
                .homepage("www.john-doe.com")
                .ids_id("https://stvgodigital.pt/ids/person/id/fa0e46cd-f9f8-4cc1-b144-76ddee1d6652")
                .build();
    }

    public LogicalConstraint refinement_none() {
        LogicalConstraint logicalConstraint=new LogicalConstraint();
        logicalConstraint.setIdsID(URI.create("https://test"));
        logicalConstraint.setLogicalOperator(LogicalConstraintOperator.NONE);
        logicalConstraint.addConstraint(new Constraint());

        return logicalConstraint;
    }

    public LogicalConstraint refinement_and() {
        LogicalConstraint logicalConstraint=new LogicalConstraint();
        logicalConstraint.setIdsID(URI.create("https://test/loginal/constraint/and"));
        logicalConstraint.setLogicalOperator(LogicalConstraintOperator.AND);
        logicalConstraint.addConstraint(new Constraint());
        logicalConstraint.addConstraint(new Constraint());

        return logicalConstraint;
    }

    public Connector connector_producer() {
        return new Connector(URI.create("https://connector/id/1"), "https://connector/ids/id/1",
                URI.create("https://connector/endpoint/1"));
    }

    public Participant fraunhoferPartWithOnlyOneConstraint() throws URISyntaxException, DatatypeConfigurationException {
        de.fraunhofer.iais.eis.Constraint constraint= new ConstraintBuilder()
                ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
                ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                ._operator_(BinaryOperator.EQUALS)
                ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        Participant participant;
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer"))
                ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
                ._legalName_("Fraunhofer-Institut")
                ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
                ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
                ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
                ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
                ._memberParticipant_(Util.asList(new de.fraunhofer.iais.eis.ParticipantBuilder(
                        new URI("https://www.fraunhofer.de/"))
                        ._legalForm_("e.V.").build()))
                ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/01"))
                        ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        ._lastValidDate_(
                                DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0)).build())
                ._memberPerson_(Util.asList(new de.fraunhofer.iais.eis.PersonBuilder()
                        ._familyName_("Mueller")
                        ._givenName_("Peter")
                        ._emailAddress_(Util.asList("mueller.peter@emailcontact.de"))
                        ._homepage_("https://MemberPerson.de/mueller_peter")
                        ._phoneNumber_(Util.asList("017894561237"))
                        .build()))
                ._version_("3.0")
                ._legalForm_("e.V.")
                ._participantRefinement_(constraint)
                .build();
        return participant;
    }

    public Participant fraunhoferPartComplete() throws URISyntaxException, DatatypeConfigurationException {
        de.fraunhofer.iais.eis.Constraint constraint= new ConstraintBuilder()
                ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
                ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                ._operator_(BinaryOperator.EQUALS)
                ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();


        de.fraunhofer.iais.eis.Constraint constraint2= new ConstraintBuilder()
                ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
                ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                ._operator_(BinaryOperator.EQUALS)
                ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        Participant participant;
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer"))
                ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
                ._legalName_("Fraunhofer-Institut")
                ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
                ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
                ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
                ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
                ._memberParticipant_(Util.asList(new de.fraunhofer.iais.eis.ParticipantBuilder(
                        new URI("https://www.fraunhofer.de/"))
                        ._legalForm_("e.V.").build()))
                ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/01"))
                        ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                        ._lastValidDate_(
                                DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0))
                        ._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                        .build())
                ._memberPerson_(Util.asList(new de.fraunhofer.iais.eis.PersonBuilder()
                        ._familyName_("Mueller")
                        ._givenName_("Peter")
                        ._emailAddress_(Util.asList("mueller.peter@emailcontact.de"))
                        ._homepage_("https://MemberPerson.de/mueller_peter")
                        ._phoneNumber_(Util.asList("017894561237"))
                        .build()))
                ._version_("3.0")
                ._legalForm_("e.V.")
                ._businessIdentifier_(new BusinessIdentifierBuilder()
                        ._identifierSystem_("CAE")
                        ._identifierNumber_("number")
                        .build())
                ._participantRefinement_(
                        new LogicalConstraintBuilder(URI.create("https://logical/contraint/id/1"))
                        ._and_(List.of(constraint, constraint2))
                        .build())
                .build();
        return participant;
    }

    public Participant fraunhoferPartComplete_2() throws URISyntaxException, DatatypeConfigurationException {
        de.fraunhofer.iais.eis.Constraint constraint= new ConstraintBuilder()
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        de.fraunhofer.iais.eis.Constraint constraint2= new ConstraintBuilder()
            ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
            ._leftOperand_(LeftOperand.SECURITY_LEVEL)
            ._operator_(BinaryOperator.EQUALS)
            ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        Participant participant;
        participant = new de.fraunhofer.iais.eis.ParticipantBuilder(URI.create("https://participant/id/test/connector/fraunhofer/2"))
            ._title_(Util.asList(new TypedLiteral("Fraunhofer IAIS")))
            ._legalName_("Fraunhofer-Institut/2")
            ._description_(Util.asList(new TypedLiteral("Fraunhofer Institute for Intelligent Analysis and Information Systems IAIS based in Sankt Augustin near Bonn is one of the leading scientific institutes in the fields of Artificial Intelligence, Machine Learning and Big Data in Germany and Europe.")))
            ._primarySite_(new SiteBuilder()._siteAddress_("Fraunhofer-Institut für Intelligente Analyse- und Informationssysteme IAIS, Schloss, 53757 Sankt Augustin").build())
            ._corporateEmailAddress_(Util.asList("contact@ids.fraunhofer.de"))
            ._corporateHomepage_(new URI("https://www.iais.fraunhofer.de/"))
            ._memberParticipant_(Util.asList(new de.fraunhofer.iais.eis.ParticipantBuilder(
                new URI("https://www.fraunhofer.de/"))
                ._legalForm_("e.V.").build()))
            ._participantCertification_(new de.fraunhofer.iais.eis.ParticipantCertificationBuilder(URI.create("https://cert/id/02"))
                ._certificationLevel_(CertificationLevel.PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK)
                ._lastValidDate_(
                    DatatypeFactory.newInstance().newXMLGregorianCalendarDate(2021, 6, 30, 0))
                ._evaluationFacility_(new EvaluationFacilityBuilder(URI.create("https://ev/01")).build())
                .build())
            ._memberPerson_(Util.asList(new de.fraunhofer.iais.eis.PersonBuilder()
                ._familyName_("Mueller")
                ._givenName_("Peter")
                ._emailAddress_(Util.asList("mueller.peter@emailcontact.de"))
                ._homepage_("https://MemberPerson.de/mueller_peter")
                ._phoneNumber_(Util.asList("017894561237"))
                .build()))
            ._version_("3.0")
            ._legalForm_("e.V.")
            ._businessIdentifier_(new BusinessIdentifierBuilder()
                ._identifierSystem_("CAE")
                ._identifierNumber_("number")
                .build())
            ._participantRefinement_(
                new LogicalConstraintBuilder(URI.create("https://logical/contraint/id/2"))
                    ._and_(List.of(constraint, constraint2))
                    .build())
            .build();
        return participant;
    }

    public de.fraunhofer.iais.eis.Connector createConnectorToTest(){
        return new BaseConnectorBuilder(URI.create("https://test-broker.com/id/test/connector-producer"))
                ._hasDefaultEndpoint_(
                        new ConnectorEndpointBuilder(URI.create("https://test-broker.com/id/test/connectorEndpoint/dhiasdcna"))
                                ._accessURL_(URI.create("https://host.docker.internal:8080/test/connector/api/ids/data")).build())
                ._inboundModelVersion_("4.2.6")
                ._outboundModelVersion_("4.2.6")
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._maintainer_(URI.create("https://www.inesctec.pt/"))
                ._curator_(URI.create("https://www.inesctec.pt/"))
                ._description_(new TypedLiteral("Description"))
                ._title_(new TypedLiteral("Title"))
                ._publicKey_(new PublicKeyBuilder(URI.create("https://test-broker.com/id/test/publicKey/dhiasdcna"))
                        ._keyValue_("KeyValueExample".getBytes())
                        ._keyType_(KeyType.RSA)
                        .build())
                ._resourceCatalog_(new ResourceCatalogBuilder(URI.create("https://test-broker.com/id/test/catalog/api/connector-producer"))
                        .build())
                .build();
    }
}
