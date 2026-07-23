package inesctec.inestec.paris.controller.restTemplate;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthControllerTest {

    //This makes the code run without having the project running
    @LocalServerPort
    private String port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void shouldReturnHeathMessage() throws Exception {

        ResponseEntity<String> response = this.restTemplate
                .getForEntity("http://localhost:" + port + "/health",
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response.getBody());
        assertThat(response.getBody()).isEqualTo("{\"status\":true}");
    }

}