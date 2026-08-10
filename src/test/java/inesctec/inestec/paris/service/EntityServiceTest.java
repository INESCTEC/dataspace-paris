package inesctec.inestec.paris.service;

import inesctec.inestec.paris.exceptions.ResourceNotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.bind.DatatypeConverter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EntityServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(EntityServiceTest.class);

    @Autowired EntityService service;
    
    ObjectMapper mapper = new ObjectMapper();

    // Used LASA
    String id = "6015bbf1-edb9-47b9-820d-f88774f64585";
    String certificateFilePublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm/OshFB2agd+s+r+ifW6s39I0dD8fT+vf2FT5T3Cm6OsIDZMXS4yZonBmaDMxDihg5Y6K9h4fsNRmCZG7WcXfz+2FFbMmtInXDdqpyItrVDHGZ3RX8MnJ5vKgJ6YLvVVbsogixkAWZNiaDsCP/srTPBUx4IdU/5LV9PpvEIYhJWK6/hBrZD2SPmUpaKOLuY/FAL1R/76bHx7lOI0mHZodMRdWAs4ddJGXdvqMoBRyzzRx2U3LiFc1NdV72422QVVis95PRszee350SFmm5wrEy0A7DSR6uWXaupH0QUYUuaq9DWIdUP2hoPiMCypYkRxRzkt+RrqJm0lwneUszEfvQIDAQAB";
    String selfDescriptionPublicKey = "TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFtL09zaEZCMmFnZCtzK3IraWZXNnMzOUkwZEQ4ZlQrdmYyRlQ1VDNDbTZPc0lEWk1YUzR5Wm9uQm1hRE14RGloZzVZNks5aDRmc05SbUNaRzdXY1hmeisyRkZiTW10SW5YRGRxcHlJdHJWREhHWjNSWDhNbko1dktnSjZZTHZWVmJzb2dpeGtBV1pOaWFEc0NQL3NyVFBCVXg0SWRVLzVMVjlQcHZFSVloSldLNi9oQnJaRDJTUG1VcGFLT0x1WS9GQUwxUi83NmJIeDdsT0kwbUhab2RNUmRXQXM0ZGRKR1hkdnFNb0JSeXp6UngyVTNMaUZjMU5kVjcyNDIyUVZWaXM5NVBSc3plZTM1MFNGbW01d3JFeTBBN0RTUjZ1V1hhdXBIMFFVWVV1YXE5RFdJZFVQMmhvUGlNQ3lwWWtSeFJ6a3QrUnJxSm0wbHduZVVzekVmdlFJREFRQUI=";

    String jsonResponse="{\n" +
            "  \"subject\": {\n" +
            "    \"C\": \"PT\",\n" +
            "    \"OU\": \"LASA\",\n" +
            "    \"CN\": \"6015bbf1-edb9-47b9-820d-f88774f64585\",\n" +
            "    \"O\": \"LASA\"\n" +
            "  },\n" +
            "  \"lastValidDate\": \"Mar 15, 2026, 09:53:49 AM\",\n" +
            "  \"issuer\": {\n" +
            "    \"C\": \"PT\",\n" +
            "    \"CN\": \"IDS INESCTEC STV Test SubCA 2022\",\n" +
            "    \"O\": \"INESC TEC\"\n" +
            "  },\n" +
            "  \"firstValidDate\": \"Mar 16, 2023, 09:53:49 AM\",\n" +
            "  \"alternativeNames\": [\n" +
            "    \"vcese11.inesctec.pt\"\n" +
            "  ]\n" +
            "}";


    String idCni40Producer = "22a50b0d-7f7f-4f04-8f42-f1277579aeb3";
    String cni40ProducerPEMWrong = "MIIBIjAFDLKSMLJFLSDMaçodkA,XLMºEDKLAisTAMBzpsrDQHBotHP5cGOd3fUkU3pTp0fd5JMEoNzGzo44bmBOmDF4d5NAFGAjUjMGtGk9dnMP2EmybypycigLAby8YjFtqGEvBIV1X+TTZmQf8amTYSmTe+IZseW174frjWr/8bQA7se4DAkrJtRX7EDESH7q82Q+2cXs84eCtXQw5bFb/ruAEAdLl7ia09A3qEBQeh6OXKeEH5GxapfXE9M4IjbNVyRv3OtEAAQmrrZVBY1ixthCxx6mK+fyFEJxXWoJa2UzZis5pSMsYfzEgscgmAhoSZZCXTNSNuT8wwIDAQAB";
    String cni40ProducerPEM = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkxzpO1EHs9/LmXnjBsAMaK/ilEHmP+awD2XvZV75L5sEvoiGQ1KGegxao7SaVnrENJDsHCHKLXWNc41VyX32HgG/qMLZ8My/bT6DgRYed1LpF+2dmr+54dcSuRhEzyVALqnd7jqyoAA3GlyydCIKJ2y5qGegQzP0D5hENStPdHgVfwiGLPCb/vEb7zeLuw5ZvtGhLZWJTymYRjNUI+KKw2mx62c2r3iCTwu0vRflSg4bLDMmJFrbJD1x8jsATSgn71h+P9QUtPUBSlGvzPYgP20PDhPj3JUhlCkPvAQryB0JyxqePhxEa+3Y8bAqF4jCdoCilPhkZiPazfVFgsj6NQIDAQAB";
    String cni40ProducerPublicKey = "TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUFreHpwTzFFSHM5L0xtWG5qQnNBTWFLL2lsRUhtUCthd0QyWHZaVjc1TDVzRXZvaUdRMUtHZWd4YW83U2FWbnJFTkpEc0hDSEtMWFdOYzQxVnlYMzJIZ0cvcU1MWjhNeS9iVDZEZ1JZZWQxTHBGKzJkbXIrNTRkY1N1UmhFenlWQUxxbmQ3anF5b0FBM0dseXlkQ0lLSjJ5NXFHZWdRelAwRDVoRU5TdFBkSGdWZndpR0xQQ2IvdkViN3plTHV3NVp2dEdoTFpXSlR5bVlSak5VSStLS3cybXg2MmMycjNpQ1R3dTB2UmZsU2c0YkxETW1KRnJiSkQxeDhqc0FUU2duNzFoK1A5UVV0UFVCU2xHdnpQWWdQMjBQRGhQajNKVWhsQ2tQdkFRcnlCMEp5eHFlUGh4RWErM1k4YkFxRjRqQ2RvQ2lsUGhrWmlQYXpmVkZnc2o2TlFJREFRQUI=";
    final String cni40producerJson="{\n" +
            "  \"subject\": {\n" +
            "    \"C\": \"PT\",\n" +
            "    \"OU\": \"CNI40-PRODUCER\",\n" +
            "    \"CN\": \"22a50b0d-7f7f-4f04-8f42-f1277579aeb3\",\n" +
            "    \"O\": \"CNI40-PRODUCER\"\n" +
            "  },\n" +
            "  \"lastValidDate\": \"Mar 15, 2026, 10:18:29 AM\",\n" +
            "  \"issuer\": {\n" +
            "    \"C\": \"PT\",\n" +
            "    \"CN\": \"IDS INESCTEC STV Test SubCA 2022\",\n" +
            "    \"O\": \"INESC TEC\"\n" +
            "  },\n" +
            "  \"firstValidDate\": \"Mar 16, 2023, 10:18:29 AM\",\n" +
            "  \"alternativeNames\": [\n" +
            "    \"ids-dp-01.inesctec.pt\"\n" +
            "  ]\n" +
            "}";
    String separator = FileSystems.getDefault().getSeparator();
    final String ca_path = "src/main/resources/certificates";


    @Test
    void findCertificate() {

        LOG.info("Check if Certificate with ID "+id +" has the correct path");
       /* Optional file;
        try {
            file = Files.walk(Paths.get(ca_path))
                    .filter(Files::isReadable)      // read permission
                    .filter(Files::isRegularFile)   // is a file
                    .filter(p -> p.getFileName().toString().equals(id+".crt"))
                    .findFirst();
            System.out.println(file.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        assertEquals(service.findCertificate(id).get().toString(),
                (ca_path+separator+id+".crt").replaceAll("[/\\\\]", Matcher.quoteReplacement(separator)));

       /* assertEquals(file.get().toString().replaceAll("[/\\\\]", Matcher.quoteReplacement(separator)),
                service.findCertificate(id).get());*/
    }

    @Test
    void findCertificate_FileDoesNotExist() {
        LOG.info("Tests various invalid IDs. So, it returns empty");

        assertEquals(service.findCertificate("b5790c63-d2dc-4c98-9d62-a9ba56d5f"), Optional.empty());

        assertEquals(service.findCertificate("b5790c63-d2dc-4c98-9d62-a9ba56d5f.crt"), Optional.empty());

        assertEquals(service.findCertificate("b5790c63-d2dc-4c98-9d62-a9ba56d5f.key"), Optional.empty());

        assertEquals(service.findCertificate("b5790c63"), Optional.empty());

        assertEquals(service.findCertificate("a9ba56d5f"), Optional.empty());
    }

    @Test
    void getJsonCertificateAttributes() {
      try {
        // Used the LASA
        assertEquals(mapper.readTree(service.getJsonCertificateAttributes(id, selfDescriptionPublicKey)), mapper.readTree(jsonResponse));
      }
      catch (Throwable t) {
        Assertions.fail(t.toString());
      }
    }

    @Test
    void getJsonCertificateAttributesWithPublicKey() {
      try {
        assertEquals(service.getJsonCertificateAttributes(idCni40Producer, cni40ProducerPEMWrong), cni40ProducerPEMWrong);

        assertEquals(mapper.readTree(service.getJsonCertificateAttributes(idCni40Producer, cni40ProducerPublicKey)), mapper.readTree(cni40producerJson));

        assertNotEquals(service.getJsonCertificateAttributes(idCni40Producer, cni40ProducerPEM), cni40producerJson);
      }
      catch (Throwable t) {
        Assertions.fail(t.toString());
      }
    }

    @Test
    void getJsonCertificateAttributesWithPublicKeyByte() {
        String idCni40Producer = "22a50b0d-7f7f-4f04-8f42-f1277579aeb3";

    /*X509Certificate cert=getCertificate(idCni40Producer);
    System.out.println("1: "+DatatypeConverter.parseBase64Binary(String.valueOf(cert.getPublicKey().getEncoded())));
    String encodedPem=service.getPem(cert);
    PublicKey builder= new PublicKeyBuilder()
    		._keyType_(KeyType.RSA)
    		._keyValue_(encodedPem.getBytes(StandardCharsets.UTF_8))
    		.build();
    System.out.println("Key: "+ new String(builder.getKeyValue(), StandardCharsets.UTF_8));*/

        assertEquals(service.getJsonCertificateAttributes(idCni40Producer, cni40ProducerPublicKey), cni40producerJson);
        assertNotEquals(service.getJsonCertificateAttributes(idCni40Producer, cni40ProducerPEM), cni40producerJson);

    }
    @Test
    void getJsonCertificateAttributes_Pass() {
      try {
        String expected = "{\n" +
          "  \"subject\": {\n" +
          "    \"C\": \"PT\",\n" +
          "    \"OU\": \"PRODUCER\",\n" +
          "    \"CN\": \"b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff\",\n" +
          "    \"O\": \"PRODUCER\"\n" +
          "  },\n" +
          "  \"lastValidDate\": \"Jul 03, 2025, 14:49:30 PM\",\n" +
          "  \"issuer\": {\n" +
          "    \"C\": \"PT\",\n" +
          "    \"CN\": \"IDS INESCTEC STV Test SubCA 2022\",\n" +
          "    \"O\": \"INESC TEC\"\n" +
          "  },\n" +
          "  \"firstValidDate\": \"Jul 04, 2022, 14:49:30 PM\",\n" +
          "  \"alternativeNames\": [\n" +
          "    \"vcese11.inesctec.pt\"\n" +
          "  ]\n" +
          "}";
        assertEquals(mapper.readTree(service.getJsonCertificateAttributes("b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff")),mapper.readTree(expected));
      }
      catch (Throwable t) {
        Assertions.fail(t.toString());
      }
    }

    @Test
    void getJsonCertificateAttributes_FileDoesNotExist() {
        service.getJsonCertificateAttributes("b5790c63-d2dc-4c98-9d62-a9ba56d5f");
        assertNull(service.getJsonCertificateAttributes("b5790c63-d2dc-4c98-9d62-a9ba56d5f"));
    }


    @Test
    void getPem() {
        LOG.info("Creating X509Certificate certificate...");
        X509Certificate cert = service.openCertificate(id, String.valueOf(
                service.findCertificate(id).get()));

        LOG.info("Test getPem() method from Entity Service");
        assertEquals(service.getPem(cert), certificateFilePublicKey);
    }

    @Test
    void testGetDecode() {
        String subcontractedID = "20bcebe2-694e-49c5-b2d9-8070e5b09c69";
        String subcontractedOriginalPublicKeyPem = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzhQJjzicy18EKcUhZ8hOCAqzpx2lglx6+eVS70oBEJbG2plTJmC8YAWX0oT4jSMaxm23cdGOMOyWtpkM7tH0SO0CN5grLlUdDgjgPATpFXszvR1u1xGo3dHtArm72D7frLacF/Zwgg80RuE2t+7zuLf/tQPdrScdZHv6Ti0lAmDLNLdFaaBf7Egvjg33CBJ8tQX+AE6tZq3YqU4npCsQS83VjGm4SLwI9Ft6j6J4NEBag0efrNa8eKXim4KgUCQX1j1/maomGUF6KJD7hYEGD1kubnUep1TXSbgYlshaFXOUw30A5kaPxxPifVgS435S0Oh5lSbaS9dBx8y8Oea82wIDAQAB";
        String subcontractedSelfDescriptionPublicKey = "TUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF6aFFKanppY3kxOEVLY1VoWjhoT0NBcXpweDJsZ2x4NitlVlM3MG9CRUpiRzJwbFRKbUM4WUFXWDBvVDRqU01heG0yM2NkR09NT3lXdHBrTTd0SDBTTzBDTjVnckxsVWREZ2pnUEFUcEZYc3p2UjF1MXhHbzNkSHRBcm03MkQ3ZnJMYWNGL1p3Z2c4MFJ1RTJ0Kzd6dUxmL3RRUGRyU2NkWkh2NlRpMGxBbURMTkxkRmFhQmY3RWd2amczM0NCSjh0UVgrQUU2dFpxM1lxVTRucENzUVM4M1ZqR200U0x3STlGdDZqNko0TkVCYWcwZWZyTmE4ZUtYaW00S2dVQ1FYMWoxL21hb21HVUY2S0pEN2hZRUdEMWt1Ym5VZXAxVFhTYmdZbHNoYUZYT1V3MzBBNWthUHh4UGlmVmdTNDM1UzBPaDVsU2JhUzlkQng4eThPZWE4MndJREFRQUI=";


        // Used the SUbcontracted
        Optional<Path> certificate = service.findCertificate(subcontractedID);
        X509Certificate cert = service.openCertificate(subcontractedID, String.valueOf(certificate.get()));

        String publicKeyBinary = service.decode(subcontractedSelfDescriptionPublicKey);

        assertEquals(publicKeyBinary, subcontractedOriginalPublicKeyPem);
    }

    @Test
    void decode() {
        LOG.info("Test decode() method from Entity Service");
        assertEquals(service.decode(selfDescriptionPublicKey), certificateFilePublicKey);
    }

    @Test
    void encode() {
        LOG.info("Creating X509Certificate certificate...");
        X509Certificate cert = service.openCertificate(id, String.valueOf(
                service.findCertificate(id).get()));

        LOG.info("Test encode() method from Entity Service");
        assertEquals(service.encode(cert), certificateFilePublicKey);
    }

    @Test
    void openCertificate_Fail() {
        String id = "b5790c63-d2dc";
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> service.openCertificate(id, id + ".crt"));
        assertEquals("Certificate not found for Connector with ID: '" + id + "'", exception.getMessage());

        //Did not test a certificate that does not have an Organization (method getJsonCertificateAttributesFromCertificate())
    }

    @Test
    void getOrganizationFromCertificate() {

        LOG.info("Creating X509Certificate certificate...");
        X509Certificate cert = service.openCertificate(id, String.valueOf(
                service.findCertificate(id).get()));

        //System.out.println(cert);

        LOG.info("Test getOrganizationFromCertificate() method from Entity Service");
        //assertEquals(service.encode(cert), certificateFilePublicKey);
        String response=service.getJsonCertificateAttributes(id, selfDescriptionPublicKey);
        System.out.println(response);
    }

    @Test
    void getPublicKey() {

        X509Certificate cert = service.openCertificate(idCni40Producer, String.valueOf(service.findCertificate(idCni40Producer).get()));

        String publicKey = DatatypeConverter.printBase64Binary(cert.getPublicKey().getEncoded()).replaceAll("(.{64})", "$1\n").replaceAll("\n", "");

        System.out.println(publicKey);

        assertEquals(publicKey, cni40ProducerPEM);
    }
}