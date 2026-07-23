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
package inesctec.inestec.paris.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

import java.util.List;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class ListOfParticipantsInsertedOrNotMessage implements java.io.Serializable {

    private String successMessage;
    private String failMessage;
    private List<String> participantsSuccessfullyInserted;
    private List<String> participantsNotInserted;

    public ListOfParticipantsInsertedOrNotMessage(String successMessage, List<String> successList, String failMessage, List<String> failList) {
        this.successMessage = successMessage;
        this.participantsNotInserted=failList;
        this.participantsSuccessfullyInserted=successList;
        this.failMessage=failMessage;
    }

    public ListOfParticipantsInsertedOrNotMessage(List<String> successList, List<String> failList) {
        this.participantsNotInserted=failList;
        this.participantsSuccessfullyInserted=successList;
    }

    public String toJson(){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("Participants successfully inserted:", participantsSuccessfullyInserted);
        jsonObject.put("Participants not inserted since they were already on the Database:", participantsNotInserted);
        return jsonObject.toString();
    }
}
