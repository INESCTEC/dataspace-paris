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

import inesctec.inestec.paris.exceptions.EmptyDirectoryException;
import inesctec.inestec.paris.exceptions.ResourceNotFoundException;
//import org.apache.commons.codec.binary.Base64;
import inesctec.inestec.paris.json.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class EntityService {

    public List<Path> files;

    private Path path;

    private final Environment environment;

    String separator = FileSystems.getDefault().getSeparator();

    private static final Logger LOG = LoggerFactory.getLogger(EntityService.class);

    public EntityService(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void loadFiles(){
        this.path = Paths.get(environment.getProperty("certificates.path")
                .replaceAll("[/\\\\]",
                        Matcher.quoteReplacement(separator)));

        LOG.info("Path: "+this.path);
        try{
            files = listFiles();
        }
        catch (IOException exception){
            System.err.println(exception);
        }
    }

    // list all .crt files from this path that have read permission
    public List<Path> listFiles() throws IOException {

        // Tested!!
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path is not an existing directory!");
        }
        List files;
        files = Files.walk(Paths.get(String.valueOf(this.path)))
                .filter(Files::isReadable)      // read permission
                .filter(Files::isRegularFile)   // is a file
            .filter(file -> file.getFileName().toString().endsWith(".crt"))
                .collect(Collectors.toList());

        // Tested!!
        if(files.isEmpty())
            throw new EmptyDirectoryException(path.toString());

        return files;
    }

    public Optional<Path> findCertificate(String id) {
        //LOG.info("Path: "+this.path);

        String filename=id+".crt";

        /*try (Stream<Path> walk = Files.walk(path)) {
            List result;
            result = walk
                    .filter(Files::isReadable)      // read permission
                    .filter(Files::isRegularFile)   // is a file
                    .filter(p -> p.getFileName().toString().equals(filename))
                    .collect(Collectors.toList());
            System.out.println("RESULT"+result);
        } catch (IOException exception) {
            exception.printStackTrace();
        }*/

        Optional<Path> certificate= files.stream()
                .filter(p -> p.getFileName().toString().equals(filename))
                .findFirst();

        return certificate;
    }


    public String getJsonCertificateAttributes(String id)  {

        Optional<Path> certificate=this.findCertificate(id);

        if(!certificate.isPresent()){
            //throw new ResourceNotFoundException("Connector", "ID", id);
            return null;
        }
        else {
            String jsonCertificate=createJson(openCertificate(id, String.valueOf(certificate.get())));
            /*String organization=
                    getOrganizationFromCertificate(openCertificate(id, String.valueOf(certificate.get())));
            System.out.println(organization);*/
            if(jsonCertificate == null || jsonCertificate.isEmpty()){
                return null;
            }
            else
                return jsonCertificate;
        }
    }

    public String getJsonCertificateAttributes(String id, String publicKey)  {
        Optional<Path> certificate=this.findCertificate(id);

        if(!certificate.isPresent()){
            LOG.info("Certificate does not exist");
            //throw new ResourceNotFoundException("Connector", "ID", id);
            return null;
        }

        //LOG.info("Certificate Exists");
        X509Certificate cert = openCertificate(id, String.valueOf(certificate.get()));


        String pem = getPem(cert);
        //LOG.info("endoded "+encoded);
        if(pem == null || pem.isEmpty()){
            return null;
        }

        String selfDescriptionPublicKeyDecoded=this.decode(publicKey);

        try {
            // IF they are equal, get and return organization
            //LOG.info(selfDescriptionPublicKeyDecoded);
            if(selfDescriptionPublicKeyDecoded.equals(pem)){
                LOG.info("Certificate and public key are equal");

                String jsonCertificate=createJson(cert);
                //Nunca deve acontecer
                if(jsonCertificate == null || jsonCertificate.isEmpty()){
                    return null;
                }
                else
                    return jsonCertificate;
            }
            // IF public key and pem are not equal:
            else {
                LOG.info("Certificate and public key are NOT equal");
                //throw new PemNotEqualException(id);
                return publicKey;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private String createJson(X509Certificate cert) {
        Certificate jsonCert=new Certificate();
        jsonCert.createCertificateEntity(cert);
        return jsonCert.toJson();
    }

    /**
    * Extracts the PEM format public key from the X509Certificate certificate
    * */
    public String getPem(X509Certificate cert){

        //encode(cert.getPublicKey())
        LOG.info("Get Pem format from certificate");
        String publicKey= DatatypeConverter.printBase64Binary(
                cert.getPublicKey().getEncoded())
                .replaceAll("(.{64})", "$1\n")
                .replaceAll("\n", "");

        //System.out.println(publicKey);
        LOG.info("... " + publicKey);
        return publicKey;
    }


    /**
     * Decodes the public key taken from the connector self-description
     *
     * @return decoded public key.
     */
    public String decode(String publicKey){

        LOG.info("Get decoded PublicKey from the self-description");
        //byte[] decoded = Base64.getDecoder().decode(publicKey);
        //String dec=new String(decoded);

        org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();
        String base64formated = DatatypeConverter.printBase64Binary(
                        base64.decode(publicKey.getBytes()))
                .replaceAll("(.{64})", "$1\n")
                .replaceAll("\n", "");

        String dec = new String(DatatypeConverter.parseBase64Binary(base64formated));
        LOG.info("... " + dec);
        return dec;
    }

    public String encode(X509Certificate cert){

        LOG.info("Get encoded PublicKey");
        byte[] publicKey = cert.getPublicKey().getEncoded();
        String encoded = Base64.getEncoder().encodeToString(publicKey);

        LOG.info("... " + encoded);
        return encoded;
    }

    /**
     * Opens the certificate
     *
     * @return certificate.
     */
    public X509Certificate openCertificate(String id, String certificate)   {
        try{
            CertificateFactory fac = CertificateFactory.getInstance("X509");
            FileInputStream is = new FileInputStream(certificate);
            X509Certificate cert = (X509Certificate) fac.generateCertificate(is);

            //System.out.println(cert);
            return cert;

        } catch (CertificateException  | FileNotFoundException e) {
            throw new ResourceNotFoundException("Certificate", "Connector", "ID", id);
        }
    }
}