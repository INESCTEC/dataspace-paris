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
package inesctec.inestec.paris.service;

import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.util.RdfResource;
import inesctec.inestec.paris.converter.BinaryOperatorConverter;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.interfaces.IConverter;
import inesctec.inestec.paris.model.Constraint;
import inesctec.inestec.paris.repository.ConstraintRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConstraintService implements IConverter<de.fraunhofer.iais.eis.Constraint, Constraint> {

    private final ConstraintRepository repository;

    public ConstraintService(ConstraintRepository repository) {
        this.repository = repository;
    }

    public Constraint save(Constraint constraint){
        return repository.save(constraint);
    }

    public void save(List<Constraint> constraintList){
        constraintList.stream().forEach(ev -> repository.save(ev));
    }

    public Optional<Constraint> findByID(Long id){
        return repository.findById(id);
    }

    public Constraint findConstraintByConstraintIdsID(URI id){
        // Only return the latest version
        return repository.findConstraintByConstraintIdsID(id).stream().findFirst().orElseThrow(() ->
                new ResourceNotFoundException("Constraint"));
    }

    public Optional<Constraint> findConstraintByConstraintIdsID_returnOptional(URI id){
        // Only return the latest version
        return repository.findConstraintByConstraintIdsID(id).stream().findFirst();
    }

    public List<Constraint> findAll(){
        List cons = new ArrayList<>();
        repository.findAll().forEach(cons::add);
        return cons;
    }

    public void remove(Constraint constraint){
        repository.delete(constraint);
    }

    public void remove(List<Constraint> constraint){
        constraint.stream().forEach(v -> repository.delete(v));
    }

    public void remove(){
        repository.deleteAll();
    }

    public Integer size(){
        return ((List<Constraint>) this.findAll()).size();
    }

    /**
     * Converts a list of database Constraints into a list of Infomodel Constraints
     * @param constraintList list of database Constraints
     * @return list of Infomodel Constraints
     * */
    public List<de.fraunhofer.iais.eis.Constraint> convertToInfomodel(List<inesctec.inestec.paris.model.Constraint> constraintList){
        List<de.fraunhofer.iais.eis.Constraint> constraintsFraun=new ArrayList<>();
        constraintList.forEach(c->{
            constraintsFraun.add(this.convertToInfomodel(c));
        });
        return constraintsFraun;
    }

    /**
     * Converts a database Constraint into a Infomodel Constraint
     * @param databaseObject database Constraints
     * @return Infomodel Constraint
     * */
    @Override
    public de.fraunhofer.iais.eis.Constraint convertToInfomodel(Constraint databaseObject){
        BinaryOperatorConverter binaryOperatorConverter=new BinaryOperatorConverter();


        // Creates a Constraint with an auto-generated ID
        if(databaseObject.getConstraintIdsID()==null) {
            return new ConstraintBuilder()
                    ._leftOperand_(databaseObject.getLeftOperand())
                    ._operator_(BinaryOperator.valueOf(binaryOperatorConverter.convert2(databaseObject.getOperator())))
                    ._rightOperand_(databaseObject.getRightOperand() == null ?
                            null :
                            new RdfResource(databaseObject.getRightOperand()))
                    ._unit_(databaseObject.getUnit() == null ? null : databaseObject.getUnit())
                    ._pipEndpoint_(databaseObject.getPipEndpoint() == null ? null : URI.create(databaseObject.getPipEndpoint()))
                    ._rightOperandReference_(databaseObject.getRightOperandReference() == null ? null : URI.create(databaseObject.getRightOperandReference()))
                    .build();
        }
        // Creates a Constraint with a specific ID
        return new ConstraintBuilder(databaseObject.getConstraintIdsID())
                ._leftOperand_(databaseObject.getLeftOperand())
                ._operator_(BinaryOperator.valueOf(binaryOperatorConverter.convert2(databaseObject.getOperator())))
                ._rightOperand_(databaseObject.getRightOperand() == null ?
                        null :
                        new RdfResource(databaseObject.getRightOperand()))
                ._unit_(databaseObject.getUnit() == null ? null : databaseObject.getUnit())
                ._pipEndpoint_(databaseObject.getPipEndpoint() == null ? null : URI.create(databaseObject.getPipEndpoint()))
                ._rightOperandReference_(databaseObject.getRightOperandReference() == null ? null : URI.create(databaseObject.getRightOperandReference()))
                .build();
    }

    /**
     * Converts a database Constraint into a Infomodel Constraint
     * @param constraint database Constraints
     * @param binaryOperatorConverter converter
     * @return Infomodel Constraint
     * */
    public de.fraunhofer.iais.eis.Constraint convertToInfomodel(inesctec.inestec.paris.model.Constraint constraint,
                                                                    BinaryOperatorConverter binaryOperatorConverter, URI id){

        // Creates a Constraint with a specific ID
        return new ConstraintBuilder(id)
                ._leftOperand_(constraint.getLeftOperand())
                ._operator_(BinaryOperator.valueOf(binaryOperatorConverter.convert2(constraint.getOperator())))
                ._rightOperand_(constraint.getRightOperand() == null ?
                        null :
                        new RdfResource(constraint.getRightOperand()))
                ._unit_(constraint.getUnit() == null ? null : constraint.getUnit())
                ._pipEndpoint_(constraint.getPipEndpoint() == null ? null : URI.create(constraint.getPipEndpoint()))
                ._rightOperandReference_(constraint.getRightOperandReference() == null ? null : URI.create(constraint.getRightOperandReference()))
                .build();
    }


    /**
     * Converts a List of database Constraint into a List of Infomodel Constraint
     * @param constraints list of infomodel Constraints
     * @return List of Database Constraint
     * */
    public List<Constraint> convertToDatabaseModel(List<de.fraunhofer.iais.eis.Constraint> constraints){
        List<Constraint> constraintsFraun=new ArrayList<>();
        constraints.forEach(c->{
            constraintsFraun.add(this.convertToDatabaseModel(c));
        });
        return constraintsFraun;
    }

    /**
     * Converts a database Constraint into a Infomodel Constraint
     * @param infomodelObject infomodel Constraints
     * @return Database Constraint
     * */
    @Override
    public Constraint convertToDatabaseModel(de.fraunhofer.iais.eis.Constraint infomodelObject) {
        return new inesctec.inestec.paris.builder.ConstraintBuilder()
                .constraint_ids_ID(infomodelObject.getId().toString())
                .unit(infomodelObject.getUnit() == null ? null : infomodelObject.getUnit())
                .leftOperand(infomodelObject.getLeftOperand())
                .operator(infomodelObject.getOperator().toString())
                .rightOperand(infomodelObject.getRightOperand() == null ?
                        null : infomodelObject.getRightOperand().getValue())
                .rightOperandReference(infomodelObject.getRightOperandReference() == null ? null : infomodelObject.getRightOperandReference())
                .pipEndpoint(infomodelObject.getPipEndpoint() == null ? null : infomodelObject.getPipEndpoint())
                .build();
    }



}
