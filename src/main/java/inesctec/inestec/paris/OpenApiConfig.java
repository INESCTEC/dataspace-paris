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

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;

@Configuration
public class OpenApiConfig {

    private final ServletContext servletContext;

    @Autowired
    public OpenApiConfig(ServletContext servletContext) {
        super();
        this.servletContext = servletContext;
    }

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${CertIS.title}") String title, @Value("${CertIS.description}") String descr, @Value("${CertIS.version}") String version) {
        return new OpenAPI()
            .components(new Components().addSecuritySchemes("basicAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")))
            .info(new Info()
                        .title(title)
                        .description(descr)
                        .version(version)
                        .termsOfService(servletContext.getContextPath() + "/LICENSE.txt")
                        .license(new License()
                          .name("Copyright © INESC TEC http://www.inesctec.pt 2023. All Rights Reserved.")
                          .url(servletContext.getContextPath() + "/LICENSE.txt"))
                        .contact(new Contact()
                          .name("Ana Chaves")
                          .email("ana.c.chaves@inesctec.pt"))
                )
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
