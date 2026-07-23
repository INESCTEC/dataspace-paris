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
package inesctec.inestec.paris;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import inesctec.inestec.paris.util.ExceptionUtil;

/**
 * Entry point to the CertIS
 */
@Log4j2
@SpringBootApplication//(exclude = SecurityAutoConfiguration.class)
@ComponentScan({
		"inesctec.inestec.paris",
		"ids.*",
		"ids.messaging.*"
})
@Configuration
public class ParISApplication implements CommandLineRunner {

  /*@Qualifier("ActivePrf")
  @Autowired
  private String activeProfile;*/
	public static void main(String[] args) {
    try {
      SpringApplication application = new SpringApplication(ParISApplication.class);
      //application.setWebApplicationType(WebApplicationType.NONE);
      application.run(args);
    }
    catch (Throwable e) {
      // https://stackoverflow.com/questions/32770884/breakpoint-at-throw-new-silentexitexception-in-eclipse-spring-boot
      if (e.getClass().getName().contains("SilentExitException"))
        log.debug("Spring is restarting the main thread - See spring-boot-devtools");
      else
        log.error("Application crashed! : {}", ExceptionUtil.getFullStackTrace(e));
    }

	}
	
  @Override
  public void run(String... args) throws Exception {
  }

}

