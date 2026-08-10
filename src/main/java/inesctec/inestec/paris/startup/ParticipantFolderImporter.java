/* Copyright (C) [2023-2026], INESC TEC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 */
package inesctec.inestec.paris.startup;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iais.eis.Participant;
import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.exceptions.ResourceAlreadyExistsException;
import inesctec.inestec.paris.util.ParticipantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Component
public class ParticipantFolderImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantFolderImporter.class);

    private final ParticipantUtil participantUtil;
    private final JsonConverter jsonConverter;

    public ParticipantFolderImporter(ParticipantUtil participantUtil, JsonConverter jsonConverter) {
        this.participantUtil = participantUtil;
        this.jsonConverter = jsonConverter;
    }

    public ImportSummary importParticipants(Path directory) {
        if (Files.notExists(directory)) {
            LOGGER.info("Participant import directory '{}' does not exist; nothing to import.", directory);
            return ImportSummary.empty();
        }
        if (!Files.isDirectory(directory)) {
            LOGGER.warn("Participant import path '{}' is not a directory; nothing to import.", directory);
            return ImportSummary.empty();
        }

        final List<Path> participantFiles;
        try (Stream<Path> files = Files.list(directory)) {
            participantFiles = files
                .filter(Files::isRegularFile)
                .filter(ParticipantFolderImporter::isParticipantFile)
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .toList();
        }
        catch (Exception exception) {
            LOGGER.error("Could not inspect participant import directory '{}'.", directory, exception);
            return new ImportSummary(0, 0, 0, 1);
        }

        if (participantFiles.isEmpty()) {
            LOGGER.info("Participant import directory '{}' contains no .json or .jsonc files.", directory);
            return ImportSummary.empty();
        }

        int imported = 0;
        int skipped = 0;
        int failed = 0;
        for (Path participantFile : participantFiles) {
            switch (importParticipant(participantFile)) {
                case IMPORTED -> imported++;
                case SKIPPED -> skipped++;
                case FAILED -> failed++;
            }
        }

        ImportSummary summary = new ImportSummary(
            participantFiles.size(), imported, skipped, failed);
        LOGGER.info(
            "Participant import from '{}' finished: {} discovered, {} imported, {} skipped, {} failed.",
            directory, summary.discovered(), summary.imported(), summary.skipped(), summary.failed());
        return summary;
    }

    private ImportStatus importParticipant(Path participantFile) {
        try (InputStream inputStream = Files.newInputStream(participantFile)) {
            JsonNode json = jsonConverter.inputStream2Json(inputStream);

            if (json.has("participant")) {
                ParticipantFraunDto participant =
                    jsonConverter.json2Object(ParticipantFraunDto.class, json);
                participantUtil.addParticipant(participant);
            }
            else {
                Participant participant = jsonConverter.json2Object(Participant.class, json);
                participantUtil.addParticipant(participant);
            }

            LOGGER.info("Imported participant file '{}'.", participantFile.getFileName());
            return ImportStatus.IMPORTED;
        }
        catch (ResourceAlreadyExistsException exception) {
            LOGGER.info("Skipped participant file '{}': {}",
                participantFile.getFileName(), exception.getMessage());
            return ImportStatus.SKIPPED;
        }
        catch (Exception exception) {
            LOGGER.error("Could not import participant file '{}'.",
                participantFile.getFileName(), exception);
            return ImportStatus.FAILED;
        }
    }

    private static boolean isParticipantFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return filename.endsWith(".json") || filename.endsWith(".jsonc");
    }

    private enum ImportStatus {
        IMPORTED,
        SKIPPED,
        FAILED
    }

    public record ImportSummary(int discovered, int imported, int skipped, int failed) {
        private static ImportSummary empty() {
            return new ImportSummary(0, 0, 0, 0);
        }
    }
}
