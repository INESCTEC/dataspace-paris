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
 */package inesctec.inestec.paris.converter;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.model.ParticipantCertification;
import inesctec.inestec.paris.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A converter for converting Model Participant1s to Infomodel Participant
 * and Infomodel Participants to Model Participant1s.
 * Used for responding a message.
 */

public class ParticipantConverter implements Converter<Participant1, Participant>{

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantConverter.class);

    public final ParticipantService participantService;
    public final ParticipantCertificationService certificationService;
    public final LogicalConstraintService logicalConstraintService;
    public final PersonService personService;
    public final BusinessIdentifierService businessIdentifierService;


    public ParticipantConverter(ParticipantService participantService,
                                ParticipantCertificationService certificationService, LogicalConstraintService logicalConstraintService, PersonService personService, BusinessIdentifierService businessIdentifierService) {
        this.participantService = participantService;
        this.certificationService = certificationService;
        this.logicalConstraintService = logicalConstraintService;
        this.personService = personService;
        this.businessIdentifierService = businessIdentifierService;
    }

    /**
     * Converts a Database Participant to a Infomodel Participant.
     *
     * @param participantDB the Participant1.
     * @return Participant representation of the Participant1.
     */
    @Override
    public Participant convert(Participant1 participantDB) {
        return convertToFraunhofer(participantDB);
    }

    /**
     * Converts a Database Participant to a ParticipantFraunDto.
     *
     * @return ParticipantFraunDto representation of the Participant.
     */
    public ParticipantFraunDto convertToParticipantFraunDto(Participant1 part) {

        Participant participant = convertToFraunhofer(part);
        ParticipantFraunDto newParticipantFraunDto;
        if(part.getConnectorSet()!=null){
            newParticipantFraunDto=new ParticipantFraunDto(participant, part.getConnectorSet());
        }
        else newParticipantFraunDto=new ParticipantFraunDto(participant);

        return newParticipantFraunDto;
    }

    private Participant convertToFraunhofer(Participant1 participantDB) {
        if(participantDB==null) return null;

        LOGGER.debug("Certification is being created...",ParticipantConverter.class);

        // Get Participant Certification from Database
        ParticipantCertification db_certification=participantDB.getParticipantCertification();
        de.fraunhofer.iais.eis.ParticipantCertification infModel_certification=null;

        // db_certification should never be null, since I made mandatory to have a Certification to create a Participant. However, it can happen if using a setParticipantCertification()
        if(db_certification!=null){
            infModel_certification=certificationService.convertToInfomodel(db_certification);
        }


        LOGGER.debug("Person is being created...",ParticipantConverter.class);
        // Get List of MemberPersons. It is null because otherwise it writes an empty array on the Participant
        List<de.fraunhofer.iais.eis.Person> persons=null;

        try {
            persons=personService.convertToInfomodel(participantDB.getMemberPerson()).stream().toList();
        }catch (NullPointerException ex){
            LOGGER.debug("Participant does not have a Person.");
        }

        // Guarantees that the TaxID (i.e., BusinessIdentifier) is not null
        LOGGER.debug("BusinessIdentifier is being created...",ParticipantConverter.class);
        // Get List of MemberPersons. It is null because otherwise it writes an empty array on the Participant
        List<de.fraunhofer.iais.eis.BusinessIdentifier> businessIdentifier=null;
        try {
            businessIdentifier=businessIdentifierService.convertToInfomodel(participantDB.getBusinessIdentifier()).stream().toList();
        }catch (NullPointerException ex){
            LOGGER.debug("Participant does not have a Business Identifier.");
        }


        LOGGER.debug("Participant is being created...",ParticipantConverter.class);
        Participant participant =  new ParticipantBuilder(participantDB.getParticipantIdsID())
                ._legalName_(participantDB.getLegalName())
                ._primarySite_(participantDB.getPrimarySite()==null ?
                        null :
                        new SiteBuilder()
                                ._siteAddress_(participantDB.getPrimarySite()).build())
                ._corporateEmailAddress_(participantDB.getCorporateEmailAddress())
                ._corporateHomepage_(participantDB.getCorporateHomepage())
                ._participantCertification_(infModel_certification)
                ._vatID_(participantDB.getVatID())
                ._jurisdiction_(participantDB.getRegistryCourt())
                ._memberPerson_(persons)
                ._legalForm_(participantDB.getLegalForm())
                ._businessIdentifier_(businessIdentifier)
                ._title_(participantDB.getName() ==null ?
                        null : participantDB.getName().stream().map(v->new TypedLiteral(v)).toList())
                ._version_(participantDB.getVersion())
                .build();


        // Guarantees that if the Description does not exist, the description list is empty and does not have a null value inside
        if(participantDB.getDescription()!=null){
            participant.setDescription(participantDB.getDescription().stream().map(TypedLiteral::new).toList());
        }


        if((participantDB.getParticipantRefinement()!=null)){
            try {
                AbstractConstraint partRefinement= logicalConstraintService.convertToInfomodel(participantDB.getParticipantRefinement());
                participant.setParticipantRefinement(partRefinement);
            }catch (ResourceNotFoundException exception){
                // This exception means that the Participant has a LogicalConstraint without Constraints.
                // In this case, the Participant stays without Refinement
                LOGGER.debug("..." + exception.getMessage() + "...");
                LOGGER.debug("... Participant won't have Refinement");
            }

        }

        // If Participant has memberParticipants (children), Then fill values
        //TODO 4/10
        /*if(participantDB.getMemberParticipant()!=null){
            List<Participant> children=new ArrayList<>();
            participantDB.getMemberParticipant().forEach(child -> {
                        children.add(children);
                    });
            participant.setMemberParticipant(children);
        }*/

        //System.out.println(participant.toRdf());

        return participant;

    }


    /**
     * Converts a Infomodel Participant to a Database Participant.
     *
     * @param fran_part the infomodel Participant1.
     * @return databse representation of a infomodel Participant.
     */
    public Participant1 convertToDatabaseParticipant(Participant fran_part) {

        LOGGER.debug("In convertToDatabaseParticipant!!!!!");
        /*LOGGER.info("Participant:");
        LOGGER.info(fran_part.toRdf());*/

        ParticipantCertification certification=null;
        if(fran_part.getParticipantCertification()!=null){
            certification=certificationService.convertToDatabaseModel(fran_part.getParticipantCertification());
        }


        LOGGER.info("Creating a Person ...");
        // Get List of MemberPersons
        Set<inesctec.inestec.paris.model.Person> persons=personService.convertToDatabaseModel(fran_part.getMemberPerson());
        //persons.forEach(System.out::println);
        LOGGER.debug("... Person created");


        LOGGER.debug("Creating a BusinessIdentifier ...");
        // Get List of BusinessIdentifiers
        Set<inesctec.inestec.paris.model.BusinessIdentifier> businessIdentifiers=businessIdentifierService
                .convertToDatabaseModel(fran_part.getBusinessIdentifier());
        LOGGER.debug("... BusinessIdentifier created");

        Participant1 participant=participantService.convertToDatabaseModel(fran_part, certification);

        if (fran_part.getParticipantRefinement()!=null){
            LOGGER.debug("Creating Refinement ...");


            inesctec.inestec.paris.model.LogicalConstraint logicalConstraint=
                    logicalConstraintService.convertToDatabaseModel(fran_part.getParticipantRefinement(), participant);


            participant.setParticipantRefinement(logicalConstraint);
            LOGGER.debug("... Refinement Created.");
        }

        // The Database Participant does not have the MemberParticipant
        /*if(!fran_part.getMemberParticipant().isEmpty()){
            LOGGER.info("Creating the Children Hierarchy ...");
            List<Participant1> listParticipantsChildren=new ArrayList<>();

            fran_part.getMemberParticipant().stream().forEach(
                    v-> {
                        listParticipantsChildren.add(convertToDatabaseParticipant(v));
                    }
            );

            LOGGER.info("... Hierarchy finished");
        }*/

        // Associate Certification and Participant
        if(certification!=null){
            certification.setParticipant(participant);
            participant.setParticipantCertification(certification);
        }

        //System.err.println(certification.toString());

        if(!persons.isEmpty()) {
            persons.forEach(v->v.setParticipant(participant));
            participant.setMemberPerson(persons);
        }

        if(!businessIdentifiers.isEmpty()) {
            businessIdentifiers.forEach(v->v.setParticipant(participant));
            participant.setBusinessIdentifier(businessIdentifiers);
        }

        return participant;
    }


    /**
     * Converts a ParticipantFraunDto Participant to a Database Participant.
     *
     * @param dtoPart the ParticipantFraunDto.
     * @return databse representation of a infomodel Participant.
     */
    public Participant1 convertToDatabaseParticipant(ParticipantFraunDto dtoPart) {
        Participant1 returnedPart=this.convertToDatabaseParticipant(dtoPart.getParticipant());
        if(!dtoPart.getConnectors().isEmpty()){
            returnedPart.setConnectorSet(dtoPart.getConnectors());
        }
        return returnedPart;
    }

    public Participant buildDummyParticipant() throws URISyntaxException, DatatypeConfigurationException {
        Constraint constraint= new ConstraintBuilder()
                ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
                ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                ._operator_(BinaryOperator.EQUALS)
                ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        Constraint constraint2= new ConstraintBuilder()
                ._unit_(URI.create("http://dbpedia.org/resource/Euro"))
                ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                ._operator_(BinaryOperator.EQUALS)
                ._rightOperand_(new RdfResource("PARTICIPANT_MEMBER_LEVEL_CONTROL_FRAMEWORK")).build();

        Constraint constraint3= new ConstraintBuilder()
                ._leftOperand_(LeftOperand.SECURITY_LEVEL)
                ._operator_(BinaryOperator.EQUALS)
                ._rightOperandReference_(new URI("http://right/operand/reference/test")).build();

        List<Constraint> constraintList=new ArrayList<>();
        constraintList.add(constraint2);
        constraintList.add(constraint);
        constraintList.add(constraint3);
        
        AbstractConstraint constraintLogical= new LogicalConstraintBuilder()
                ._and_(constraintList).build();

        Participant participant;
        participant = new ParticipantBuilder()
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
            ._participantRefinement_(constraintLogical)
            .build();
        return participant;
    }

   /* public String toJson(){}*/


}
