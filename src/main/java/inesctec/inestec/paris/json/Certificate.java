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

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.*;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class Certificate implements java.io.Serializable {

    private Map<String, String> subject;
    private Date lastValidDate;
    private Map<String, String> issuer;
    private Date firstValidDate;
    private final List<String> alternativeNames=new ArrayList<>();

    public void addAlternativeName(String altName){
        this.alternativeNames.add(altName);
    }

    // TODO from Cesar to Ana: I had to set the date format "MMM dd, yyyy, HH:mm:ss aa"
    //   without it, the method return a strange character before 'aa'
    //   Are there side effects ?
    public String toJson(){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("MMM dd, yyyy, HH:mm:ss aa")
                .create();
        String json = gson.toJson(this);
        System.out.println(json.toString());
        return json;
    }

    /**
     * Transform a X509Certificate into a JSON Certificate
     * */
    public Certificate createCertificateEntity(X509Certificate cert) {

        Map<String, String> subject=new HashMap<>();
        getCertificatesSubjects(cert).forEach((v) -> subject.put(v.getType(),v.getValue().toString()));

        Map<String, String> issuer=new HashMap<>();
        getCertificatesIssuer(cert).forEach((v) -> issuer.put(v.getType(),v.getValue().toString()));
        //System.out.println(issuer);

        this.setSubject(subject);
        this.setIssuer(issuer);
        this.setFirstValidDate(cert.getNotBefore());
        this.setLastValidDate(cert.getNotAfter());
        try {
            cert.getSubjectAlternativeNames().stream()
                    .forEach((v)-> this.addAlternativeName((String) v.get(1)));
        } catch (CertificateParsingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public String getOrganizationFromCertificate(X509Certificate cert)   {
        String organization;
        try {
            organization = new LdapName(cert.getSubjectX500Principal().getName()).getRdns().stream()
                    .filter(i -> i.getType().equalsIgnoreCase("O")).findFirst().get().getValue().toString();
        } catch (InvalidNameException e) {
            throw new ResourceNotFoundException("Organization", "Certificate", "Connector", "ID");
        }

        //System.out.println("organization: " + organization);
        return organization;
    }

    public List<Rdn> getCertificatesSubjects(X509Certificate cert)   {
        // List all values of Subject
        LdapName ldapDN = null;
        try {
            ldapDN = new LdapName(cert.getSubjectX500Principal().getName());
        } catch (InvalidNameException e) {
            throw new RuntimeException(e);
        }
        /*for(Rdn rdn: ldapDN.getRdns()) {
            System.out.println(rdn.getType() + " -> " + rdn.getValue());
        }*/
        return ldapDN.getRdns();
    }

    public List<Rdn> getCertificatesIssuer(X509Certificate cert)   {
        // List all values of Subject
        LdapName ldapDN = null;
        try {
            ldapDN = new LdapName(cert.getIssuerX500Principal().getName());
        } catch (InvalidNameException e) {
            throw new RuntimeException(e);
        }
        /*for(Rdn rdn: ldapDN.getRdns()) {
            System.out.println(rdn.getType() + " -> " + rdn.getValue());
        }*/
        return ldapDN.getRdns();
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "subject='" + subject + '\'' +
                ", lastValidDate='" + lastValidDate + '\'' +
                ", issuer='" + issuer + '\'' +
                ", firstValidDate='" + firstValidDate + '\'' +
                ", alternativeNames=" + alternativeNames +
                '}';
    }
}
