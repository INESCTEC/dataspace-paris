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

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import de.fraunhofer.iais.eis.*;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.dto.PersonDto;
import inesctec.inestec.paris.model.BusinessIdentifier;
import inesctec.inestec.paris.model.Participant1;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

public class CompareUtil {

  /**
   * Compares two Infomodel Participants
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareFraunhParticipants(Participant part1, Participant part2){
    if(part1==null || part2==null) return false;

    // The Fraunh Site Address has a UUID. Mine does not
    System.out.println("LegalName: "+ compareListsEmptyAndNull(part1.getLegalName(),part2.getLegalName())
            + "\nPrimarySite: " + compareSiteAddress(part1.getPrimarySite(), part2.getPrimarySite())
            + "\nCorporateEmailAddress: " + compareListsEmptyAndNull(part1.getCorporateEmailAddress(),part2.getCorporateEmailAddress())
            + "\nCorporateHomepage: " + Objects.equals(part1.getCorporateHomepage(),part2.getCorporateHomepage())
            + "\nMemberParticipant: " + compareListsEmptyAndNull(part1.getMemberParticipant(),part2.getMemberParticipant())
            + "\nParticipantCertification: " + Objects.equals(part1.getParticipantCertification(),part2.getParticipantCertification())
            + "\nParticipantRefinement: " + Objects.equals(part1.getParticipantRefinement(),part2.getParticipantRefinement())
            + "\nBusinessIdentifier: " + compareListsEmptyAndNull(part1.getBusinessIdentifier(),part2.getBusinessIdentifier())
            + "\nVatID: " + Objects.equals(part1.getVatID(),part2.getVatID())
            + "\nLegalForm: " + Objects.equals(part1.getLegalForm(),part2.getLegalForm())
            + "\nJurisdiction: " + Objects.equals(part1.getJurisdiction(),part2.getJurisdiction())
            + "\nMemberPerson: " + CompareUtil.comparePersons(part1.getMemberPerson(),part2.getMemberPerson())
            + "\nVersion: " + Objects.equals(part1.getVersion(),part2.getVersion())
            + "\nTitle: " + compareListsEmptyAndNull(part1.getTitle(),part2.getTitle())
            + "\nId: " + Objects.equals(part1.getId(),part2.getId())
            + "\nComment: " + compareListsEmptyAndNull(part1.getComment(),part2.getComment())
            + "\nDescription: " + compareListsEmptyAndNull(part1.getDescription(),part2.getDescription())
            + "\nLabel: " + compareListsEmptyAndNull(part1.getLabel(),part2.getLabel()));

    return compareListsEmptyAndNull(part1.getLegalName(),part2.getLegalName())
            && compareSiteAddress(part1.getPrimarySite(), part2.getPrimarySite())
            && compareListsEmptyAndNull(part1.getCorporateEmailAddress(),part2.getCorporateEmailAddress())
            && Objects.equals(part1.getCorporateHomepage(), part2.getCorporateHomepage())
            && compareListsEmptyAndNull(part1.getMemberParticipant(),part2.getMemberParticipant())
            && (Objects.equals(part1.getParticipantCertification(), part2.getParticipantCertification()))
            && Objects.equals(part1.getParticipantRefinement() , part2.getParticipantRefinement())
            && compareListsEmptyAndNull(part1.getBusinessIdentifier() , part2.getBusinessIdentifier())
            && Objects.equals(part1.getVatID() , part2.getVatID())
            && Objects.equals(part1.getLegalForm() , part2.getLegalForm())
            && Objects.equals(part1.getJurisdiction() , part2.getJurisdiction())
            && CompareUtil.comparePersons(part1.getMemberPerson(),part2.getMemberPerson())
            && Objects.equals(part1.getVersion(), part2.getVersion())
            && compareListsEmptyAndNull(part1.getTitle() , part2.getTitle())
            && Objects.equals(part1.getId() , part2.getId())
            && compareListsEmptyAndNull(part1.getComment() , part2.getComment())
            && compareListsEmptyAndNull(part1.getDescription() , part2.getDescription())
            && compareListsEmptyAndNull(part1.getLabel() , part2.getLabel());
  }

  /**
   * Compares two Database Participants
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareDBParticipants(Participant1 part1, Participant1 part2){
    if(part1==null || part2==null) return false;

    System.out.println("LegalName: "+ Objects.equals(part1.getLegalName(),part2.getLegalName())
            + "\nPrimarySite: " + Objects.equals(part1.getPrimarySite(),part2.getPrimarySite())
            + "\nCorporateEmailAddress: " + Objects.equals(part1.getCorporateEmailAddress(),part2.getCorporateEmailAddress())
            + "\nCorporateHomepage: " + Objects.equals(part1.getCorporateHomepage(),part2.getCorporateHomepage())
           // + "\nMemberParticipant: " + Objects.equals(part1.getMemberParticipant(),part2.getMemberParticipant())
            + "\nParticipantCertification: " + Objects.equals(part1.getParticipantCertification(),part2.getParticipantCertification())
            + "\nParticipantRefinement: " + Objects.equals(part1.getParticipantRefinement(),part2.getParticipantRefinement())
            + "\nBusinessIdentifier: " + CompareUtil.compareSet(part1.getBusinessIdentifier(),part2.getBusinessIdentifier())
            + "\nVatID: " + Objects.equals(part1.getVatID(),part2.getVatID())
            + "\nLegalForm: " + Objects.equals(part1.getLegalForm(),part2.getLegalForm())
            + "\nJurisdiction: " + Objects.equals(part1.getRegistryCourt(),part2.getRegistryCourt())
            + "\nMemberPerson: " + CompareUtil.compareSet(part1.getMemberPerson(), part2.getMemberPerson())
            + "\nTitle: " + Objects.equals(part1.getName(),part2.getName())
            + "\nParticipantIdsID: " + Objects.equals(part1.getParticipantIdsID(),part2.getParticipantIdsID())
            + "\nId_participant_DB: " + Objects.equals(part1.getId_participant(),part2.getId_participant())
            + "\nDescription: " + Objects.equals(part1.getDescription(),part2.getDescription())
            + "\nVersion: " + Objects.equals(part1.getVersion(),part2.getVersion()));

    return part1.equals(part2);
    /*return Objects.equals(part1.getLegalName(),part2.getLegalName())
            && Objects.equals(part1.getPrimarySite(),part2.getPrimarySite())
            && Objects.equals(part1.getCorporateEmailAddress(),part2.getCorporateEmailAddress())
            && Objects.equals(part1.getCorporateHomepage(), part2.getCorporateHomepage())
            && Objects.equals(part1.getMemberParticipant(),part2.getMemberParticipant())
            && Objects.equals(part1.getParticipantCertification(),part2.getParticipantCertification())
            && Objects.equals(part1.getParticipantRefinement() , part2.getParticipantRefinement())
            && Objects.equals(part1.getBusinessIdentifier() , part2.getBusinessIdentifier())
            && Objects.equals(part1.getVatID() , part2.getVatID())
            && Objects.equals(part1.getLegalForm() , part2.getLegalForm())
            && Objects.equals(part1.getRegistryCourt() , part2.getRegistryCourt())
            && CompareUtil.compareSet(getMemberPerson(), that.getMemberPerson())
            && Objects.equals(part1.getName() , part2.getName())
            && Objects.equals(part1.getParticipantIdsID() , part2.getParticipantIdsID())
            && Objects.equals(part1.getId_participant() , part2.getId_participant())
            && Objects.equals(part1.getDescription() , part2.getDescription());*/
  }

  /**
   * Compares two ParticipantFraunDto Participants
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareFraunhDTOParticipants(ParticipantFraunDto part1, ParticipantFraunDto part2){
    if(part1==null || part2==null) return false;

    return compareFraunhParticipants(part1.getParticipant(), part2.getParticipant())
            && compareSet(part1.getConnectors(), part2.getConnectors());
  }

  /**
   * Compares two sets
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareSet(Set<?> set1, Set<?> set2){

    // Handles if both sets are null or empty
    if(Objects.equals(set1, set2)) return true;

    // Handles if one is empty and the other null
    try {
      if((set1==null && set2.isEmpty()) || set2==null && set1.isEmpty()) return true;
    }
    catch (NullPointerException ex){}

    // Handles if only one set is null
    if(set1==null) return false;
    if(set2==null) return false;

    // Sees the difference
    //System.err.println(Sets.difference(set1,set2));
    return Sets.difference(set1,set2).isEmpty();
  }

  /**
   * Compares two infomodel addresses
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareSiteAddress(Site site1, Site site2){

    if((site1==null && site2==null) ) return true;

    // Ideal condition. Already handles all nulls and all empty lists, as well as list content. For more specific cases it uses the other conditions
    try {
      if(Objects.equals(site1.getSiteAddress(), site2.getSiteAddress())) return true;
     }
    catch (NullPointerException ex){}

    return false;
  }

  /**
   * Checks if two lists are the same when one or both are empty or null
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareListsEmptyAndNull(List<?> list1, List<?> list2){

    // Ideal condition. Already handles all nulls and all empty lists, as well as list content. For more specific cases it uses the other conditions
    if(Objects.equals(list1, list2)) return true;


    //System.out.println("list1: "+ list1 + "\nlist2: " + list2);
    // If one list is null and other is empty, they are considered equal
    try {
      if((list1==null && list2.isEmpty()) || list2==null && list1.isEmpty()) return true;
    }
    catch (NullPointerException ex){}

    if(list1==null) return false;
    if(list2==null) return false;

    return false;
  }

  /**
   * Compares two Infomodel Persons
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean comparePersons(List<Person> list1, List<Person> list2){

    if(compareListsEmptyAndNull(list1, list2)) return true;

    List<Person> personList1= new ArrayList<>(list1);
    List<Person> personList2= new ArrayList<>(list2);

    personList1.sort(new SortByLastName());
    personList2.sort(new SortByLastName());

    /*System.out.println("compareListsByLastName: "+ CollectionUtils.isEqualCollection(personList1, personList2));
    System.out.println("compareListsByLastName: "+ personList1.containsAll(personList2));
    System.out.println("compareListsByLastName: "+ personList1.equals(personList2));
    System.out.println("compareListsByLastName: "+ personList1.toString().equals(personList2.toString()));
    System.out.println("compareListsByLastName: "+ ListUtils.isEqualList(personList1,personList2));*/

    if(CollectionUtils.isEqualCollection(personList1, personList2) ||
            ListUtils.isEqualList(personList1,personList2) || personList1.toString().equals(personList2.toString())){
      return true;
    }

    return false;
  }

  /**
   * Compares two DB Persons
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareDBPersons(Set<inesctec.inestec.paris.model.Person> list1, Set<inesctec.inestec.paris.model.Person> list2){

    if(compareSet(list1, list2)) return true;

    if(list1==null) return false;
    if(list2==null) return false;
    List<inesctec.inestec.paris.model.Person> personList1= new ArrayList<>(list1);
    List<inesctec.inestec.paris.model.Person> personList2= new ArrayList<>(list2);

    personList1.sort(new SortByLastName());
    personList2.sort(new SortByLastName());

    /*personList1.forEach(p->System.err.println(p.toJson2()));
    personList2.forEach(p->System.out.println(p.toJson2()));*/

    /*System.out.println("isEqualCollection: "+ CollectionUtils.isEqualCollection(personList1, personList2));
    System.out.println("containsAll: "+ personList1.containsAll(personList2));
    System.out.println("equals: "+ personList1.equals(personList2));
    System.out.println("toString: "+ personList1.toString().equals(personList2.toString()));
    System.out.println("isEqualList: "+ ListUtils.isEqualList(personList1,personList2));*/

    if(CollectionUtils.isEqualCollection(personList1, personList2) ||
            ListUtils.isEqualList(personList1,personList2) || personList1.toString().equals(personList2.toString())){
      return true;
    }

    return false;
  }

  /**
   * Compares two DB Persons
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean comparePersonsDTO(Set<inesctec.inestec.paris.model.Person> list1, Set<inesctec.inestec.paris.model.Person> list2){

    if(compareSet(list1, list2)) return true;

    if(list1==null) return false;
    if(list2==null) return false;
    List<PersonDto> personList1= new ArrayList<>(list1.stream().map(inesctec.inestec.paris.model.Person::convertToPersonDto).toList());
    List<PersonDto> personList2= new ArrayList<>(list2.stream().map(inesctec.inestec.paris.model.Person::convertToPersonDto).toList());

    personList1.sort(new SortByLastName());
    personList2.sort(new SortByLastName());

    /*personList1.forEach(p->System.err.println(p.toString()));
    personList2.forEach(p->System.out.println(p.toString()));*/

    /*System.out.println("isEqualCollection: "+ CollectionUtils.isEqualCollection(personList1, personList2));
    System.out.println("containsAll: "+ personList1.containsAll(personList2));
    System.out.println("equals: "+ personList1.equals(personList2));
    System.out.println("toString: "+ personList1.toString().equals(personList2.toString()));
    System.out.println("isEqualList: "+ ListUtils.isEqualList(personList1,personList2));*/

    if(CollectionUtils.isEqualCollection(personList1, personList2) ||
        ListUtils.isEqualList(personList1,personList2) || personList1.toString().equals(personList2.toString())){
      return true;
    }

    return false;
  }

  /**
   * Compares two DB Business Identifiers
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareDBBusinessIdentifiers(Set<inesctec.inestec.paris.model.BusinessIdentifier> list1,
                                                     Set<inesctec.inestec.paris.model.BusinessIdentifier> list2){

    if(compareSet(list1, list2)) return true;

    if(list1==null) return false;
    if(list2==null) return false;
    List<inesctec.inestec.paris.model.BusinessIdentifier> biList1= new ArrayList<>(list1);
    List<inesctec.inestec.paris.model.BusinessIdentifier> biList2= new ArrayList<>(list2);

    biList1.sort(new SortByIdsId());
    biList2.sort(new SortByIdsId());

    /*biList1.forEach(p->System.err.println(p.toJson()));
    biList2.forEach(p->System.out.println(p.toJson()));*/

    /*System.out.println("isEqualCollection: "+ CollectionUtils.isEqualCollection(biList1, biList2));
    System.out.println("containsAll: "+ biList1.containsAll(biList2));
    System.out.println("equals: "+ biList1.equals(biList2));
    System.out.println("toString: "+ biList1.toString().equals(biList2.toString()));
    System.out.println("isEqualList: "+ ListUtils.isEqualList(biList1,biList2));*/

    if(CollectionUtils.isEqualCollection(biList1, biList2) ||
            ListUtils.isEqualList(biList1,biList2) || biList1.toString().equals(biList2.toString())){
      return true;
    }

    return false;
  }


  /**
   * Compares two Lists of Infomodel Participants
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareParticipants(List<Participant> list1, List<Participant> list2){

    if(compareListsEmptyAndNull(list1, list2)) return true;

    List<Participant> parList1= new ArrayList<>(list1);
    List<Participant> parList2= new ArrayList<>(list2);

    parList1.sort(new SortByID());
    parList2.sort(new SortByID());

    /*System.out.println("compareListsByLastName: "+ CollectionUtils.isEqualCollection(personList1, personList2));
    System.out.println("compareListsByLastName: "+ personList1.containsAll(personList2));
    System.out.println("compareListsByLastName: "+ personList1.equals(personList2));
    System.out.println("compareListsByLastName: "+ personList1.toString().equals(personList2.toString()));
    System.out.println("compareListsByLastName: "+ ListUtils.isEqualList(personList1,personList2));*/

    if(CollectionUtils.isEqualCollection(parList1, parList2) ||
            ListUtils.isEqualList(parList1,parList2) || parList1.toString().equals(parList2.toString())){
      return true;
    }

    return false;
  }

  /**
   * Compares two Lists of Infomodel Participants
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareDBParticipants(List<Participant1> list1, List<Participant1> list2){

    if(compareListsEmptyAndNull(list1, list2)) return true;

    if(list1==null) return false;
    if(list2==null) return false;

    List<Participant1> parList1= new ArrayList<>(list1);
    List<Participant1> parList2= new ArrayList<>(list2);

    parList1.sort(new SortByID_DB());
    parList2.sort(new SortByID_DB());

    /*System.out.println("compareListsByLastName: "+ CollectionUtils.isEqualCollection(personList1, personList2));
    System.out.println("compareListsByLastName: "+ personList1.containsAll(personList2));
    System.out.println("compareListsByLastName: "+ personList1.equals(personList2));
    System.out.println("compareListsByLastName: "+ personList1.toString().equals(personList2.toString()));
    System.out.println("compareListsByLastName: "+ ListUtils.isEqualList(personList1,personList2));*/

    if(CollectionUtils.isEqualCollection(parList1, parList2) ||
            ListUtils.isEqualList(parList1,parList2) || parList1.toString().equals(parList2.toString())){
      return true;
    }

    return false;
  }

  /**
   * Compares two Infomodel Constraints
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareConstraints(List<de.fraunhofer.iais.eis.Constraint> list1, List<de.fraunhofer.iais.eis.Constraint> list2){

    if(compareListsEmptyAndNull(list1, list2)) return true;

    List<de.fraunhofer.iais.eis.Constraint> constraintList1= new ArrayList<>(list1);
    List<de.fraunhofer.iais.eis.Constraint> constraintList2= new ArrayList<>(list2);

    constraintList1.sort((Comparator<? super Constraint>) new SortByIdsId());
    constraintList2.sort((Comparator<? super Constraint>) new SortByIdsId());

    /*System.out.println("compareListsByLastName: "+ CollectionUtils.isEqualCollection(constraintList1, constraintList2));
    System.out.println("compareListsByLastName: "+ constraintList1.containsAll(constraintList2));
    System.out.println("compareListsByLastName: "+ constraintList1.equals(constraintList2));
    System.out.println("compareListsByLastName: "+ constraintList1.toString().equals(constraintList2.toString()));
    System.out.println("compareListsByLastName: "+ ListUtils.isEqualList(constraintList1,constraintList2));*/

    if(CollectionUtils.isEqualCollection(constraintList1, constraintList2) ||
            ListUtils.isEqualList(constraintList1,constraintList2) || constraintList1.toString().equals(constraintList2.toString())){
      return true;
    }

    return false;
  }

  public static boolean compareStrings(List<String> list1, List<String> list2){

    if(compareListsEmptyAndNull(list1, list2)) return true;

    if(list1==null) return false;
    if(list2==null) return false;

    List<String> constraintList1= new ArrayList<>(list1);
    List<String> constraintList2= new ArrayList<>(list2);

    constraintList1.sort(new SortByString());
    constraintList2.sort(new SortByString());

    /*System.out.println("compareListsByLastName: "+ CollectionUtils.isEqualCollection(constraintList1, constraintList2));
    System.out.println("compareListsByLastName: "+ constraintList1.containsAll(constraintList2));
    System.out.println("compareListsByLastName: "+ constraintList1.equals(constraintList2));
    System.out.println("compareListsByLastName: "+ constraintList1.toString().equals(constraintList2.toString()));
    System.out.println("compareListsByLastName: "+ ListUtils.isEqualList(constraintList1,constraintList2));*/

    if(CollectionUtils.isEqualCollection(constraintList1, constraintList2) ||
            ListUtils.isEqualList(constraintList1,constraintList2) || constraintList1.toString().equals(constraintList2.toString())){
      return true;
    }

    return false;
  }

  /*static class SortByLastName implements Comparator<Person>
  {
    @Override
    public int compare(Person o1, Person o2) {
      return o1.getFamilyName().compareTo(o2.getFamilyName());
    }
  }*/

  static class SortByLastName implements Comparator<Object>
  {
    @Override
    public int compare(Object o1, Object o2) {
      return o1.toString().compareTo(o2.toString());
    }

    public int compare(Person o1, Person o2) {
      return o1.getFamilyName().compareTo(o2.getFamilyName());
    }

    public int compare(inesctec.inestec.paris.model.Person o1, inesctec.inestec.paris.model.Person o2) {
      return o1.getFamilyName().compareTo(o2.getFamilyName());
    }
  }

  static class SortByLastNameDB implements Comparator<inesctec.inestec.paris.model.Person>
  {
    @Override
    public int compare(inesctec.inestec.paris.model.Person o1, inesctec.inestec.paris.model.Person o2) {
      return o1.getFamilyName().compareTo(o2.getFamilyName());
    }
  }

  public static class SortByIdsId implements Comparator<Object>
  {
    @Override
    public int compare(Object o1, Object o2) {
      if (o1 instanceof de.fraunhofer.iais.eis.Constraint && o2 instanceof de.fraunhofer.iais.eis.Constraint){
        return ((de.fraunhofer.iais.eis.Constraint)o1).getId()
                .compareTo(((de.fraunhofer.iais.eis.Constraint)o2).getId());
      }
      if (o1 instanceof BusinessIdentifier && o2 instanceof BusinessIdentifier){
        return ((BusinessIdentifier)o1).getBusinessIdentifierIdsID().compareTo(((BusinessIdentifier)o2).getBusinessIdentifierIdsID());
      }
      return o1.toString().compareTo(o2.toString());
    }
  }

  static class SortByID implements Comparator<Participant>
  {

    @Override
    public int compare(Participant o1, Participant o2) {
      return o1.getId().compareTo(o2.getId());
    }
  }

  static class SortByID_DB implements Comparator<Participant1>
  {
    @Override
    public int compare(Participant1 o1, Participant1 o2) {
      return o1.getParticipantIdsID().compareTo(o2.getParticipantIdsID());
    }
  }

  static class SortByString implements Comparator<String>
  { @Override
    public int compare(String o1, String o2) {
      return o1.compareTo(o2);
    }
  }

}
