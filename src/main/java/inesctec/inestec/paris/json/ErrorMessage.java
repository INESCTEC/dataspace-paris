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
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class ErrorMessage implements java.io.Serializable {

    private String message;
    private String reason;

    public ErrorMessage(String message) {
        this.message = message;
    }

    public String toJson(){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(this);
        System.out.println(json.toString());
        return json;
    }
}
