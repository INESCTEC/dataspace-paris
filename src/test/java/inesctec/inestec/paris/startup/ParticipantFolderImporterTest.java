package inesctec.inestec.paris.startup;

import inesctec.inestec.paris.converter.JsonConverter;
import inesctec.inestec.paris.dto.ParticipantFraunDto;
import inesctec.inestec.paris.util.ParticipantUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ParticipantFolderImporterTest {

    @Mock
    private ParticipantUtil participantUtil;

    @TempDir
    private Path temporaryDirectory;

    @Test
    void doesNothingWhenDirectoryDoesNotExist() throws Exception {
        ParticipantFolderImporter importer = createImporter();

        ParticipantFolderImporter.ImportSummary summary =
            importer.importParticipants(temporaryDirectory.resolve("missing"));

        assertThat(summary).isEqualTo(new ParticipantFolderImporter.ImportSummary(0, 0, 0, 0));
        verifyNoInteractions(participantUtil);
    }

    @Test
    void doesNothingWhenDirectoryIsEmpty() {
        ParticipantFolderImporter.ImportSummary summary =
            createImporter().importParticipants(temporaryDirectory);

        assertThat(summary).isEqualTo(new ParticipantFolderImporter.ImportSummary(0, 0, 0, 0));
        verifyNoInteractions(participantUtil);
    }

    @Test
    void importsOnlyParticipantJsonFilesFromDirectory() throws Exception {
        copyResource("populatingDatabase/producer.json", temporaryDirectory.resolve("a.json"));
        copyResource("ParticipantsToInsert/InsertParticipant2.jsonc", temporaryDirectory.resolve("b.jsonc"));
        Files.writeString(temporaryDirectory.resolve("ignored.txt"), "not participant data");

        ParticipantFolderImporter.ImportSummary summary =
            createImporter().importParticipants(temporaryDirectory);

        assertThat(summary).isEqualTo(new ParticipantFolderImporter.ImportSummary(2, 2, 0, 0));
        verify(participantUtil).addParticipant(any(ParticipantFraunDto.class));
        verify(participantUtil).addParticipant(any(de.fraunhofer.iais.eis.Participant.class));
    }

    private ParticipantFolderImporter createImporter() {
        return new ParticipantFolderImporter(participantUtil, new JsonConverter());
    }

    private void copyResource(String resourceName, Path target) throws Exception {
        try (var inputStream = new ClassPathResource(resourceName).getInputStream()) {
            Files.copy(inputStream, target);
        }
    }
}
