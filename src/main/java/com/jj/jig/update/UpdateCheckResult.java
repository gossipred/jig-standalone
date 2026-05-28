package com.jj.jig.update;

public class UpdateCheckResult {

    private final String currentVersion;
    private final String latestVersion;
    private final String releaseDate;
    private final String downloadUrl;
    private final String releaseNotes;
    private final String releaseNotesZh;
    private final boolean hasUpdate;

    public UpdateCheckResult(String currentVersion, String latestVersion, String releaseDate,
                             String downloadUrl, String releaseNotes, String releaseNotesZh,
                             boolean hasUpdate) {
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
        this.releaseDate = releaseDate;
        this.downloadUrl = downloadUrl;
        this.releaseNotes = releaseNotes;
        this.releaseNotesZh = releaseNotesZh;
        this.hasUpdate = hasUpdate;
    }

    public String getCurrentVersion()  { return currentVersion; }
    public String getLatestVersion()   { return latestVersion; }
    public String getReleaseDate()     { return releaseDate; }
    public String getDownloadUrl()     { return downloadUrl; }
    public String getReleaseNotes()    { return releaseNotes; }
    public String getReleaseNotesZh()  { return releaseNotesZh; }
    public boolean hasUpdate()         { return hasUpdate; }
}
