package com.jj.jig.license;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class MachineIdService {

    private String cachedId;

    public synchronized String getMachineId() {
        if (cachedId == null) cachedId = detect();
        return cachedId;
    }

    private String detect() {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("mac")) {
                String out = run("ioreg", "-rd1", "-c", "IOPlatformExpertDevice");
                Matcher m = Pattern.compile("\"IOPlatformUUID\"\\s*=\\s*\"([^\"]+)\"").matcher(out);
                if (m.find()) return m.group(1);
            } else if (os.contains("win")) {
                String out = run("wmic", "csproduct", "get", "UUID");
                String[] lines = out.split("\\r?\\n");
                if (lines.length > 1) {
                    String uuid = lines[1].trim();
                    if (!uuid.isEmpty()) return uuid;
                }
            }
        } catch (Exception ignored) {}

        // Fallback: first non-loopback MAC address
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || !ni.isUp()) continue;
                byte[] mac = ni.getHardwareAddress();
                if (mac == null || mac.length == 0) continue;
                StringBuilder sb = new StringBuilder("MAC-");
                for (byte b : mac) sb.append(String.format("%02X", b));
                return sb.toString();
            }
        } catch (Exception ignored) {}

        return "FALLBACK-" + System.getProperty("user.name", "unknown");
    }

    private String run(String... cmd) throws Exception {
        Process p = Runtime.getRuntime().exec(cmd);
        return new String(p.getInputStream().readAllBytes());
    }
}
