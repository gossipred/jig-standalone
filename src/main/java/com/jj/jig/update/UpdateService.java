package com.jj.jig.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UpdateService {

    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/gossipred/jig-and-toolings-management-system" +
            "/main/standalone/version.json";

    private final String currentVersion;
    private final ObjectMapper objectMapper;

    public UpdateService(@Value("${app.version}") String currentVersion,
                         ObjectMapper objectMapper) {
        this.currentVersion = currentVersion;
        this.objectMapper = objectMapper;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public UpdateCheckResult checkForUpdate() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VERSION_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String latestVersion  = root.get("version").asText();
        String releaseDate    = root.get("releaseDate").asText();
        String downloadUrl    = root.get("downloadUrl").asText();
        String releaseNotes   = root.get("releaseNotes").asText();
        String releaseNotesZh = root.get("releaseNotesZh").asText();

        boolean hasUpdate = isNewer(latestVersion, currentVersion);

        return new UpdateCheckResult(currentVersion, latestVersion, releaseDate,
                downloadUrl, releaseNotes, releaseNotesZh, hasUpdate);
    }

    // Semantic version comparison: "1.2.0" > "1.1.0"
    private boolean isNewer(String latest, String current) {
        String[] l = latest.split("\\.");
        String[] c = current.split("\\.");
        int len = Math.max(l.length, c.length);
        for (int i = 0; i < len; i++) {
            int lv = i < l.length ? Integer.parseInt(l[i]) : 0;
            int cv = i < c.length ? Integer.parseInt(c[i]) : 0;
            if (lv > cv) return true;
            if (lv < cv) return false;
        }
        return false;
    }
}
