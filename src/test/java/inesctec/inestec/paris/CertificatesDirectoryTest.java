package inesctec.inestec.paris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.FileSystems;
import java.util.regex.Matcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import inesctec.inestec.paris.exceptions.EmptyDirectoryException;
import inesctec.inestec.paris.service.EntityService;

class CertificatesDirectoryTest {

  final String directoryPass = "src/main/resources/certificates";

  final String directoryNotExist = "//wsl$/Ubuntu-20.04/home/ana/certificate-authority/ids-certificate-authority/data/cert1";

  final String directoryEmpty = "//wsl$/Ubuntu-20.04/home/ana/certificate-authority/ids-certificate-authority/data/cert2";

  @Mock
  Environment env;

  @BeforeEach
  //@BeforeClass
  void setUp() {
    env = mock(Environment.class);
  }

  @Test
	void listFiles_notADirectoryTest() {
		when(env.getProperty("certificates.path"))
				.thenReturn(directoryNotExist);
		//System.out.println("listFiles_notADirectoryTest: "+env.getProperty("certificates.path"));

		Exception exception = assertThrows(IllegalArgumentException.class, () ->
				execute(env));
		assertEquals("Path is not an existing directory!", exception.getMessage());
	}

  @Test
	void listFiles_correctScenario() {
		when(env.getProperty("certificates.path"))
				.thenReturn(directoryPass);
		//System.out.println("listFiles_correctScenario: "+env.getProperty("certificates.path"));
		execute(env);
	}

  public void execute(Environment env) {
    EntityService service = new EntityService(env);
    service.loadFiles();
    service.getJsonCertificateAttributes("0f550ce6-cb93-4cce-b55e-6c9376e870ff");
  }

  public String osPath(String path) {
    return path.replaceAll("[/\\\\]", Matcher.quoteReplacement(FileSystems.getDefault().getSeparator()));
  }

}
