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
package inesctec.inestec.paris.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import inesctec.inestec.paris.dto.*;
import inesctec.inestec.paris.util.CompareUtil;
import inesctec.inestec.paris.util.LocalDateTimeTypeAdapter;
import inesctec.inestec.paris.converter.UriConverter;
import inesctec.inestec.paris.util.ZonedDateTimeTypeAdapter;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.jena.atlas.lib.SetUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.annotations.CascadeType;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static inesctec.inestec.paris.config.DatabaseConstants.URI_COLUMN_LENGTH;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "participant", // TODO: Make legalName unique again
    uniqueConstraints = {
    @UniqueConstraint(name = "UniqueIDSID_Version", columnNames = { "participantIdsID", "version" })/*,
        @UniqueConstraint(name = "UniqueIDSID_Version", columnNames = { "legalName", "version" }) */})
public class Participant1  implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Participant1.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id_participant;

    /* Participant-ID */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI participantIdsID;

    /* The complete legal name of the IDS Participant, */
    @Column(/*unique = true, */nullable = false)
    @ElementCollection @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name = "participant_legal_name")
    private List<String> legalName;

    /* The commonly used name or term for the IDS Participant
    * Also known as title */
    @Column
    @ElementCollection @LazyCollection(LazyCollectionOption.FALSE) // It should only have one value due to the DataspaceApp
    @CollectionTable(name = "participant_name")
    private List<String> name=new ArrayList<>();


    @Column @ElementCollection @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name = "participant_description")
    private List<String> description;


    /* Email address for contacting the participant on a general level. */
    @Column @ElementCollection @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name = "participant_email")
    private List<String> corporateEmailAddress;

    /* General official homepage of the participant. */
    @Convert(converter = UriConverter.class)
    @Column(length = URI_COLUMN_LENGTH)
    private URI corporateHomepage;
    
    /* Conditions which needs to be satisfied that a single Participant can be seen as a member of the
    subject Participant. For instance, all Participants with their headquarter in 
    Europe might be a potential consumer of GDPR-releated data.*/
    @OneToOne//(cascade = CascadeType.ALL)
    @Cascade({ CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "part_refinement",
            joinColumns =
                    { @JoinColumn(name = "id_participant", referencedColumnName = "id_participant") },
            inverseJoinColumns =
                    { @JoinColumn(name = "id_logical_constraint", referencedColumnName = "id_logical_constraint") })
    private LogicalConstraint participantRefinement;

    /* Indicates membership of a persons to an organization. */
    @ManyToMany(fetch=FetchType.EAGER)
    //@Cascade({ CascadeType.ALL})
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.PERSIST})
    @JoinTable(
            name = "participant_person",
            joinColumns = @JoinColumn(name = "id_participant"),
            inverseJoinColumns = @JoinColumn(name = "id_person"))
    private Set<Person> memberPerson;

    /* Certification issued for the given Participant. */
    @OneToOne(mappedBy = "participant")
    @PrimaryKeyJoinColumn @Cascade({CascadeType.ALL})
    private ParticipantCertification participantCertification;
    
    /* Indicates a primary site for the Organization, this is the default means by which an Organization
    can be contacted and is not necessarily the formal headquarters.  - Site
    */
    @Column
    private String primarySite;
    
    /* The Value-added Tax ID of the IDS Participant.
    * identifierNumber
    * e.g. the NIF number
    * unique Validation is working */
    @Column
    private String vatID;
    
    /* Used to identify the activity sector */
    @OneToMany(mappedBy="participant",fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade({ CascadeType.ALL})
    private Set<BusinessIdentifier> businessIdentifier;
    
    /* The responsible court for the organization. Usually a city and country.
    * Same as jurisdiction */
    @Column
    private String registryCourt;
    
    /* The legal form of the IDS Participant. */
    @Column(nullable = false)
    private String legalForm;

    @Column
    private String version;

    /* Indicates the Connectors that belong to a Participant. */
    @ManyToMany(mappedBy = "participantSet")
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Connector> connectorSet;

    public Participant1(URI participantIdsID, List<String> legalName, List<String> name, List<String> description,
                        List<String> corporateEmailAddress, URI corporateHomepage, /*List<URI> memberParticipant,*/
                        LogicalConstraint participantRefinement,
                        Set<Person> memberPerson, ParticipantCertification participantCertification,
                        String primarySite, String vatID, Set<BusinessIdentifier> businessIdentifier, String registryCourt,
                        String legalForm, Set<Connector> connectorSet, String version) {
        this.participantIdsID = participantIdsID;
        this.legalName = legalName;
        this.name = name;
        this.description = description;
        this.corporateEmailAddress = corporateEmailAddress;
        this.corporateHomepage = corporateHomepage;
        //this.memberParticipant = memberParticipant;
        this.participantRefinement = participantRefinement;
        this.memberPerson = memberPerson;
        this.participantCertification = participantCertification;
        this.primarySite = primarySite;
        this.vatID = vatID;
        this.businessIdentifier = businessIdentifier;
        this.registryCourt = registryCourt;
        this.legalForm = legalForm;
        this.connectorSet=connectorSet;
        this.version=version;
    }

    @Transactional
    public void update(Participant1 participant1){
        this.participantIdsID = participant1.participantIdsID;
        this.legalName = participant1.legalName;
        this.name = participant1.name;
        this.corporateEmailAddress = participant1.corporateEmailAddress;
        this.corporateHomepage = participant1.corporateHomepage;
        //this.memberParticipant = participant1.memberParticipant;
        this.participantRefinement = participant1.participantRefinement;
        this.memberPerson = participant1.memberPerson;
        this.participantCertification = participant1.participantCertification;
        this.primarySite = participant1.primarySite;
        this.vatID = participant1.vatID;
        this.registryCourt = participant1.registryCourt;
        this.legalForm = participant1.legalForm;
        this.description=participant1.description;
        this.connectorSet=participant1.connectorSet;
        this.businessIdentifier=participant1.businessIdentifier;
        this.version=participant1.version;
    }


    @Transactional
    public void updateBasicParticipant(Participant1 participant1){
        this.participantIdsID = participant1.participantIdsID;
        this.legalName = participant1.legalName;
        this.name = participant1.name;
        this.corporateEmailAddress = participant1.corporateEmailAddress;
        this.corporateHomepage = participant1.corporateHomepage;
        this.primarySite = participant1.primarySite;
        this.vatID = participant1.vatID;
        this.registryCourt = participant1.registryCourt;
        this.legalForm = participant1.legalForm;
        this.description=participant1.description;
        this.version=participant1.version;
    }


    public Participant1Dto convertToParticipantDto(){

        LOGGER.debug("Converting Participant to DTO");

        Set<Connector> connectorSetFromParticipant=this.getConnectorSet();
        PersonDto personDto=new PersonDto();
        LogicalConstraint logicalConstraint=this.getParticipantRefinement();
        Set<Person> personSet=this.getMemberPerson();

        Participant1Dto participant1DTO=new Participant1Dto();
        /*if(participant1.getMemberParticipant()!=null){
            participant1.getMemberParticipant().forEach(p -> {
                memberPart.add(participant1.convertToParticipantDto(p));
            });
        }
        participant1DTO.setMemberParticipant(memberPart);*/

        participant1DTO.setParticipantIdsID(this.getParticipantIdsID());
        participant1DTO.setLegalName(this.getLegalName());
        participant1DTO.setName(this.getName());
        participant1DTO.setCorporateEmailAddress(this.getCorporateEmailAddress());
        participant1DTO.setCorporateHomepage(this.getCorporateHomepage());
        participant1DTO.setVersion(this.getVersion());

        if(this.getMemberPerson()!=null){
            participant1DTO.setMemberPerson(personSet.stream().map(Person::convertToPersonDto).collect(Collectors.toSet()));
        }

        if(logicalConstraint!=null){
            participant1DTO.setParticipantRefinement(ObjectUtils.defaultIfNull(
                logicalConstraint.convertToLogicalConstDto(),
                null));
        }

        if(this.getBusinessIdentifier()!=null){
            List<BusinessIdentifierDto> businessIdentifierDtos=new ArrayList<>();
            this.getBusinessIdentifier().forEach(bi->{
                businessIdentifierDtos.add(bi.convertToBusinessIdentifierDto());
            });
            participant1DTO.setBusinessIdentifier(new HashSet<>(businessIdentifierDtos));
        }

        participant1DTO.setParticipantCertification(this.getParticipantCertification() == null ?
                null : this.getParticipantCertification().convertToCertificationDto());
        participant1DTO.setPrimarySite(this.getPrimarySite());
        participant1DTO.setVatID( this.getVatID());
        participant1DTO.setRegistryCourt(this.getRegistryCourt());
        participant1DTO.setLegalForm(this.getLegalForm());
        participant1DTO.setDescription(this.getDescription());

        if(connectorSetFromParticipant!=null){
            Set<ConnectorDto> connectorDtoSet=new HashSet<>();
            connectorSetFromParticipant.forEach(connector -> connectorDtoSet.add(connector.convertToConnectorDto()));
            participant1DTO.setConnectorSet(connectorDtoSet);
        }

        /*if(participant1.getChildren()!=null){
            participant1.getChildren().forEach(child -> {
                children_1.add(participant1.convertToParticipantDto(child));
            });
        }
        participant1DTO.setChildren(ObjectUtils.defaultIfNull(children_1,null));*/

        return participant1DTO;
    }

    public Participant1(URI participantIdsID, List<String> legalName, ParticipantCertification participantCertification) {
        this.participantIdsID = participantIdsID;
        this.legalName = legalName;
        this.participantCertification = participantCertification;
    }

    // Only adds the first name (Due to the App behavior)
    public void setName(List<String> name) {
        this.name = name.stream().findFirst().stream().toList();
    }

    // Overrides the name. It can only have 1 name (Due to the App behavior)
    public void setName(String newName) {
        if(this.name==null){
            this.name=new ArrayList<>();
        }
        this.name.clear();
        this.name.add(newName);
    }

    public void setConnectorSet(Set<Connector> connectorSet) {
        this.connectorSet = connectorSet;
    }

    public void setConnectorSet(Connector connector) {
        if(this.connectorSet==null){
            this.connectorSet=new HashSet<>();
        }
        this.connectorSet.add(connector);
    }

   /* public void setMemberParticipant(List<URI> memberParticipant) {
        this.memberParticipant = memberParticipant;
    }*/

    /*public void setMemberParticipant(URI memberParticipant) {
        if(this.memberParticipant==null){
            this.memberParticipant=new ArrayList<>();
        }
        this.memberParticipant.add(memberParticipant);
    }

    public void setMemberParticipant(List<Participant1> memberParticipant) {
        this.memberParticipant = memberParticipant.stream().map(v->v.getParticipantIdsID()).toList();
    }

    public void setMemberParticipant(Participant1 memberParticipant) {
        if(this.memberParticipant==null){
            this.memberParticipant=new ArrayList<>();
        }
        this.memberParticipant.add(memberParticipant.getParticipantIdsID());
    }*/

    public void setCorporateEmailAddress(List<String> corporateEmailAddress) {
        this.corporateEmailAddress = corporateEmailAddress;
    }

    public void setCorporateEmailAddress(String corporateEmailAddress) {
        if(this.corporateEmailAddress==null){
            this.corporateEmailAddress=new ArrayList<>();
        }
        this.corporateEmailAddress.add(corporateEmailAddress);
    }

    public void setLegalName(List<String> legalName) {
        this.legalName = legalName;
    }

    public void setLegalName(String legalName) {
        if(this.legalName==null){
            this.legalName=new ArrayList<>();
        }
        this.legalName.add(legalName);
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public void setDescription(String description) {
        if(this.description==null){
            this.description=new ArrayList<>();
        }
        this.description.add(description);
    }

    public void setMemberPerson(Person person) {
        if(this.memberPerson==null){
            this.memberPerson=new HashSet<>();
        }
        this.memberPerson.add(person);

        if(person.getParticipant()==null){
            person.setParticipant(new HashSet<>());
        }
        person.getParticipant().add(this);
    }

    public void setMemberPerson(Set<Person> persons) {
        this.memberPerson=persons;
    }

    public void setBusinessIdentifier(Set<BusinessIdentifier> businessIdentifierSet) {
        this.businessIdentifier = businessIdentifierSet;
    }

    public void setBusinessIdentifier(BusinessIdentifier businessIdentifier) {
        if(this.businessIdentifier==null){
            this.businessIdentifier=new HashSet<>();
        }
        this.businessIdentifier.add(businessIdentifier);
    }

    public void removePerson(Person person){
        this.memberPerson.remove(person);
        person.getParticipant().remove(this);
    }

    public void addParticipantToPerson(Person person){
        this.memberPerson.remove(person);
        person.getParticipant().add(this);
        this.memberPerson.add(person);
    }

    @Override
    public String toString() {
        return "Participant1{" +
            "participantID='" + participantIdsID + '\'' +
            ", legalName='" + legalName + '\'' +
            ", name='" + name + '\'' +
            ", corporateEmailAddress='" + corporateEmailAddress + '\'' +
            ", corporateHomepage=" + corporateHomepage +
            //", memberParticipant=" + memberParticipant +
            ", participantRefinement='" + participantRefinement + '\'' +
            ", primarySite='" + primarySite + '\'' +
            ", vatID='" + vatID + '\'' +
            ", businessIdentifier='" + businessIdentifier + '\'' +
            ", registryCourt='" + registryCourt + '\'' +
            ", legalForm='" + legalForm + '\'' +
            ", version='" + version + '\'' +
            '}';
    }

    public String toJson(){

        return convertToParticipantDto().toJson();
        /*Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(this);
        //System.out.println(json.toString());
        return json;*/
    }

    public String toJson2(){
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(this);
        return json;
    }

    @Override
    public boolean equals(Object o) {
        LOGGER.info("Checking if the Participants are equal...");
        if (this == o) {
            LOGGER.info("... They are.");
            return true;
        }
        if (!(o instanceof Participant1 that)) {
            LOGGER.info("... They are different.");
            return false;
        }

        /*System.err.println("ParticipantIdsID: "+Objects.equals(getParticipantIdsID(), that.getParticipantIdsID()));
        System.err.println("LegalName: "+CompareUtil.compareStrings(getLegalName(),that.getLegalName()));
        System.err.println("Name: "+CompareUtil.compareStrings(getName(), that.getName()));
        System.err.println("Description: "+CompareUtil.compareStrings(getDescription(), that.getDescription()));
        System.err.println("CorporateEmailAddress: "+CompareUtil.compareStrings(getCorporateEmailAddress() , that.getCorporateEmailAddress()));
        System.err.println("CorporateHomepage: "+Objects.equals(getCorporateHomepage(), that.getCorporateHomepage()));
        //System.err.println("MemberParticipant: "+ Objects.equals(getMemberParticipant(), that.getMemberParticipant()));
        //System.err.println("Children: "+CompareUtil.compareDBParticipants(getChildren(), that.getChildren()));
        System.err.println("ParticipantRefinement: "+ Objects.equals(getParticipantRefinement(), that.getParticipantRefinement()));
        System.err.println("MemberPerson: "+ CompareUtil.compareDBPersons(getMemberPerson(), that.getMemberPerson()));
        System.err.println("ParticipantCertification: "+Objects.equals(getParticipantCertification(), that.getParticipantCertification()));
        System.err.println("PrimarySite: "+Objects.equals(getPrimarySite(), that.getPrimarySite()));
        System.err.println("VatID: "+Objects.equals(getVatID(), that.getVatID()));
        System.err.println("BusinessIdentifier: "+CompareUtil.compareSet(getBusinessIdentifier(), that.getBusinessIdentifier()));
        System.err.println("RegistryCourt: "+Objects.equals(getRegistryCourt(), that.getRegistryCourt()));
        System.err.println("LegalForm: "+Objects.equals(getLegalForm(), that.getLegalForm()));
        System.err.println("LegalForm: "+Objects.equals(getVersion(), that.getVersion()));
        System.err.println("ConnectorSet: "+CompareUtil.compareSet(getConnectorSet(), that.getConnectorSet()));*/

        return Objects.equals(getParticipantIdsID(), that.getParticipantIdsID())
            && CompareUtil.compareStrings(getLegalName(),that.getLegalName())
            && CompareUtil.compareStrings(getName(), that.getName())
            && CompareUtil.compareStrings(getDescription(), that.getDescription())
            && CompareUtil.compareStrings(getCorporateEmailAddress(),that.getCorporateEmailAddress())
            && Objects.equals(getCorporateHomepage(), that.getCorporateHomepage())
            //&& Objects.equals(getMemberParticipant(), that.getMemberParticipant())
            /*&& CompareUtil.compareDBParticipants(getChildren(), that.getChildren())*/
            && Objects.equals(getParticipantRefinement(), that.getParticipantRefinement())
            && CompareUtil.compareDBPersons(getMemberPerson(), that.getMemberPerson())
            && Objects.equals(getParticipantCertification(),that.getParticipantCertification())
            && Objects.equals(getPrimarySite(),that.getPrimarySite())
            && Objects.equals(getVatID(),that.getVatID())
            && CompareUtil.compareDBBusinessIdentifiers(getBusinessIdentifier() , that.getBusinessIdentifier())
            && Objects.equals(getRegistryCourt(), that.getRegistryCourt())
            && Objects.equals(getLegalForm(), that.getLegalForm())
            && Objects.equals(getVersion(), that.getVersion())
            && CompareUtil.compareSet(getConnectorSet(), that.getConnectorSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId_participant(), getParticipantIdsID(), getLegalName(), getName(), getDescription(), getCorporateEmailAddress(), getCorporateHomepage(), getParticipantRefinement(), getMemberPerson(),
            getParticipantCertification(), getPrimarySite(), getVatID(), getBusinessIdentifier(), getRegistryCourt(), getLegalForm(), getVersion(), getConnectorSet());
    }
}
