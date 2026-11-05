package com.banking.dashboard;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerfileTest {

    @Test
    void finalBuildStageRunsAsNonRootUser() throws IOException {
        List<String> lines = Files.readAllLines(Path.of("Dockerfile"));
        int lastFrom = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().toUpperCase().startsWith("FROM ")) {
                lastFrom = i;
            }
        }

        assertTrue(lastFrom >= 0, "Dockerfile must contain a build stage");

        int userLine = -1;
        int commandLine = -1;
        String user = "";
        for (int i = lastFrom + 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.toUpperCase().startsWith("USER ")) {
                userLine = i;
                user = line.substring("USER ".length()).trim();
            }
            if (line.toUpperCase().startsWith("ENTRYPOINT ") || line.toUpperCase().startsWith("CMD ")) {
                commandLine = i;
                break;
            }
        }

        assertTrue(userLine >= 0, "Final build stage must declare a USER");
        assertTrue(userLine < commandLine, "USER must appear before ENTRYPOINT or CMD");
        assertFalse(user.equalsIgnoreCase("root") || user.equals("0"),
                "Final build stage must use a non-root user");
    }
}
