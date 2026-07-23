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


import de.fraunhofer.iais.eis.*;
import inesctec.inestec.paris.converter.BinaryOperatorConverter;
import inesctec.inestec.paris.enums.LogicalConstraintOperator;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import inesctec.inestec.paris.interfaces.IConverter;
import inesctec.inestec.paris.model.LogicalConstraint;
import inesctec.inestec.paris.model.Participant1;
import inesctec.inestec.paris.repository.LogicalConstraintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LogicalConstraintService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogicalConstraintService.class);
    private final LogicalConstraintRepository repository;
    private final ConstraintService constraintService;

    public LogicalConstraintService(LogicalConstraintRepository repository, ConstraintService constraintService) {
        this.repository = repository;
        this.constraintService = constraintService;
    }

    public LogicalConstraint save(LogicalConstraint logicalConstraint){
        return repository.save(logicalConstraint);
    }

    public void save(List<LogicalConstraint> constraintList){
        constraintList.stream().forEach(ev -> repository.save(ev));
    }

    public Optional<LogicalConstraint> findByID(Long id){
        return repository.findById(id);
    }

    public List<LogicalConstraint> findAll(){
        List ev = new ArrayList<>();
        repository.findAll().forEach(ev::add);
        return ev;
    }

    public void remove(LogicalConstraint logicalConstraint){
        repository.delete(logicalConstraint);
    }

    public void remove(List<LogicalConstraint> logicalConstraint){
        logicalConstraint.stream().forEach(v -> repository.delete(v));
    }

    public void remove(){
        repository.deleteAll();
    }

    public Integer size(){
        return ((List<LogicalConstraint>) this.findAll()).size();
    }

    @Transactional
    public LogicalConstraint findLogicalConstraintByIds_ID(URI id){
        return repository.findFirstByIdsIDOrderByVersion(id).orElseThrow(() ->
                new ResourceNotFoundException("Logical Constraint"));
    }

    @Transactional
    public Optional<LogicalConstraint> findLogicalConstraintBy_IDS_ID(URI id){
        return repository.findFirstByIdsIDOrderByVersion(id);
    }


    /**
     * Converts a Fraunhofer Constraint and Logical Constraint (i.e., Participant Refiement) into a LogicalConstraint
     * @param fraun_refinement infomodel refinement
     * @param p database participant
     * @return a Logical Constraint with all the values
     * */
    public LogicalConstraint convertToDatabaseModel(AbstractConstraint fraun_refinement, Participant1 p) {
        LogicalConstraint logicalConstraint;
        List<inesctec.inestec.paris.model.Constraint> constraints=new ArrayList<>();

        if (fraun_refinement instanceof Constraint){
            inesctec.inestec.paris.model.Constraint newConstraint=constraintService.convertToDatabaseModel((Constraint) fraun_refinement);
            newConstraint.setVersion(p.getVersion());

            // They have the same ID
            logicalConstraint =
                    new inesctec.inestec.paris.builder.LogicalConstraintBuilder(
                            fraun_refinement.getId())
                            .participant(p)
                            .version(p.getVersion())
                            .build();

            constraints.add(newConstraint);

            LogicalConstraint finalLogicalConstraint1 = logicalConstraint;
            // For each constraint, set the Logical Constraint
            constraints.forEach(v-> v.setLogicalConstraint(finalLogicalConstraint1));
            // Set the Constraints on the Logical Constraint
            logicalConstraint.addConstraint(newConstraint);


            //p.setParticipantRefinement(logicalConstraint);
            return logicalConstraint;
        }

        if (fraun_refinement instanceof de.fraunhofer.iais.eis.LogicalConstraint){
            // List of Constraints defined on Participant
            List<de.fraunhofer.iais.eis.Constraint> originalConstraintList= new ArrayList<>();

            LogicalConstraintOperator logicalConstraintOperator;
            if(!((de.fraunhofer.iais.eis.LogicalConstraint) fraun_refinement).getAnd().isEmpty()){
                // If Logical Operator is AND
                originalConstraintList=((de.fraunhofer.iais.eis.LogicalConstraint) fraun_refinement).getAnd();
                logicalConstraintOperator=LogicalConstraintOperator.valueOf("AND");
            }
            else if (!((de.fraunhofer.iais.eis.LogicalConstraint) fraun_refinement).getXone().isEmpty()){
                // If Logical Operator is XONE
                originalConstraintList=((de.fraunhofer.iais.eis.LogicalConstraint) fraun_refinement).getXone();
                logicalConstraintOperator=LogicalConstraintOperator.valueOf("XONE");
            }else {
                // If Logical Operator is OR
                originalConstraintList=((de.fraunhofer.iais.eis.LogicalConstraint) fraun_refinement).getOr();
                logicalConstraintOperator=LogicalConstraintOperator.valueOf("OR");
            }

            constraints=constraintService.convertToDatabaseModel(originalConstraintList);
            constraints.forEach(v-> v.setVersion(p.getVersion()));

            logicalConstraint =
                    new inesctec.inestec.paris.builder.LogicalConstraintBuilder(
                            fraun_refinement.getId(),
                            logicalConstraintOperator,
                            constraints)
                            .version(p.getVersion())
                            .participant(p)
                            .build();

            LogicalConstraint finalLogicalConstraint = logicalConstraint;
            constraints.forEach(v-> {
                v.setLogicalConstraint(finalLogicalConstraint);
            });
            return logicalConstraint;
        }
        return null;
    }


    public de.fraunhofer.iais.eis.AbstractConstraint convertToInfomodel(LogicalConstraint refinement){
        switch (refinement.getLogicalOperator()){
            //case NONE
            default -> {
                inesctec.inestec.paris.model.Constraint c=refinement.getConstraints().stream()
                        .findFirst().orElseThrow(() -> new ResourceNotFoundException("Constraint"));

                BinaryOperatorConverter binaryOperatorConverter=new BinaryOperatorConverter();

                return constraintService.convertToInfomodel(c, binaryOperatorConverter, refinement.getIdsID());
            }
            case AND -> {
                List<Constraint> constraintsFraun=constraintService.convertToInfomodel(
                        refinement.getConstraints().stream().toList()
                );
                return new LogicalConstraintBuilder(refinement
                        .getIdsID())
                        ._and_(constraintsFraun).build();
            }
            case OR -> {
                List<Constraint> constraintsFraun=constraintService.convertToInfomodel(
                        refinement.getConstraints().stream().toList()
                );
                return new LogicalConstraintBuilder(refinement
                                .getIdsID())
                                ._or_(constraintsFraun).build();
            }
            case XONE -> {
                List<Constraint> constraintsFraun=constraintService.convertToInfomodel(
                        refinement.getConstraints().stream().toList()
                );
                return new LogicalConstraintBuilder(refinement
                                .getIdsID())
                                ._xone_(constraintsFraun).build();
            }
        }
    }
}
