package inesctec.inestec.paris.json;

import inesctec.inestec.paris.service.EntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.nio.file.FileSystems;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class CertificateTest {

  @Mock
  Environment env;
  final String directoryPass=".\\docker\\local\\certificate-authority\\cert";

  @Autowired
  EntityService service;
  Certificate certificate;
  X509Certificate cert;

  @BeforeEach
  void setUp() {
    env = mock(Environment.class);
    when(env.getProperty("certificates.path"))
            .thenReturn( // For the different Operative Systems
                    directoryPass.replaceAll("[/\\\\]",
                            Matcher.quoteReplacement(FileSystems.getDefault().getSeparator())));

    service = new EntityService(env);
    service.loadFiles();
    certificate=new Certificate();

    // Uses the STV Producer certificate
    String id="b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff";
    cert = service.openCertificate(id, String.valueOf(
            service.findCertificate(id).get()));

  }

  @Test
  void addAlternativeName() throws ParseException {
    String noSAN="{\n" +
            "  \"subject\": {\n" +
            "    \"CN\": \"b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff\"\n" +
            "  },\n" +
            "  \"lastValidDate\": \"Jul 03, 2025, 15:49:30 PM\",\n" +
            "  \"issuer\": {\n" +
            "    \"CN\": \"IDS INESCTEC STV Test SubCA 2022\"\n" +
            "  },\n" +
            "  \"firstValidDate\": \"Jul 04, 2022, 15:49:30 PM\",\n" +
            "  \"alternativeNames\": []\n" +
            "}";

    String withSAN="{\n" +
            "  \"subject\": {\n" +
            "    \"CN\": \"b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff\"\n" +
            "  },\n" +
            "  \"lastValidDate\": \"Jul 03, 2025, 15:49:30 PM\",\n" +
            "  \"issuer\": {\n" +
            "    \"CN\": \"IDS INESCTEC STV Test SubCA 2022\"\n" +
            "  },\n" +
            "  \"firstValidDate\": \"Jul 04, 2022, 15:49:30 PM\",\n" +
            "  \"alternativeNames\": [\n" +
            "    \"vcese11.inesctec.pt\"\n" +
            "  ]\n" +
            "}";
    // Create a new Certificate
    Certificate newCertificate=new Certificate();

    Map<String, String> subjects=new HashMap<>();
    subjects.put("CN", "b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff");
    Map<String, String> issuer=new HashMap<>();
    issuer.put("CN", "IDS INESCTEC STV Test SubCA 2022");
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    newCertificate.setSubject(subjects);
    newCertificate.setIssuer(issuer);
    newCertificate.setFirstValidDate(formatter.parse("2022-07-04 15:49:30 UTC"));
    newCertificate.setLastValidDate(formatter.parse("2025-07-03 15:49:30 UTC"));


    assertEquals(newCertificate.toJson(), noSAN);

    newCertificate.addAlternativeName("vcese11.inesctec.pt");
    assertEquals(newCertificate.toJson(), withSAN);
  }

  @Test
  void createCertificateEntity() throws InvalidNameException, ParseException {
    Certificate newCertificate=new Certificate();

    Map<String, String> subjects=new HashMap<>();
    subjects.put("C", "PT");
    subjects.put("O", "PRODUCER");
    subjects.put("OU", "PRODUCER");
    subjects.put("CN", "b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff");

    Map<String, String> issuer=new HashMap<>();
    issuer.put("CN", "IDS INESCTEC STV Test SubCA 2022");
    issuer.put("O", "INESC TEC");
    issuer.put("C", "PT");

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    newCertificate.setSubject(subjects);
    newCertificate.setIssuer(issuer);
    newCertificate.setFirstValidDate(formatter.parse("2022-07-04 15:49:30 BST"));
    newCertificate.setLastValidDate(formatter.parse("2025-07-03 15:49:30 BST"));
    newCertificate.addAlternativeName("vcese11.inesctec.pt");

    assertEquals(certificate.createCertificateEntity(cert).toJson(), newCertificate.toJson());
  }

  @Test
  void getOrganizationFromCertificate() {
    assertEquals(certificate.getOrganizationFromCertificate(cert), "PRODUCER");
  }

  @Test
  void getCertificatesSubjects() {
    System.out.println(certificate.getCertificatesSubjects(cert).toString());
    assertEquals(certificate.getCertificatesSubjects(cert).toString(), "[C=PT, O=PRODUCER, OU=PRODUCER, CN=b5790c63-d2dc-4c98-9d62-a9ba56d5f8ff]");
  }

  @Test
  void getCertificatesIssuer() {
    System.out.println(certificate.getCertificatesIssuer(cert).toString());
    assertEquals(certificate.getCertificatesIssuer(cert).toString(), "[C=PT, O=INESC TEC, CN=IDS INESCTEC STV Test SubCA 2022]");
  }
}