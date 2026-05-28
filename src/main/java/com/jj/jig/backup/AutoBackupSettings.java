package com.jj.jig.backup;

public class AutoBackupSettings {

    private boolean enabled = false;
    private String frequency = "daily"; // daily, weekly, monthly
    private String location = "";
    private String lastRun = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLastRun() { return lastRun; }
    public void setLastRun(String lastRun) { this.lastRun = lastRun; }
}
