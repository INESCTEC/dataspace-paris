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

import inesctec.inestec.paris.startup.ParticipantFolderImporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class DataLoader implements ApplicationRunner {

    private final ParticipantFolderImporter participantFolderImporter;
    private final Path participantImportDirectory;

    public DataLoader(
            ParticipantFolderImporter participantFolderImporter,
            @Value("${paris.data-loader.directory:./populatingDatabase}")
            String participantImportDirectory) {
        this.participantFolderImporter = participantFolderImporter;
        this.participantImportDirectory = Path.of(participantImportDirectory)
            .toAbsolutePath()
            .normalize();
    }

    @Override
    public void run(ApplicationArguments args) {
        participantFolderImporter.importParticipants(participantImportDirectory);
    }
}
