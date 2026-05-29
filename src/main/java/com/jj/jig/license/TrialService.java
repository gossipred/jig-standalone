package com.jj.jig.license;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class TrialService {

    private static final int  TRIAL_DAYS = 30;
    private static final String SALT     = "jig-standalone-trial-v1";
    private static final Path  TRIAL_FILE =
        Path.of(System.getProperty("user.home"), ".jig-standalone", "trial.dat");

    private final MachineIdService machineIdService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TrialService(MachineIdService machineIdService) {
        this.machineIdService = machineIdService;
    }

    public AuthStatus checkTrial() {
        String machineId = machineIdService.getMachineId();

        if (!Files.exists(TRIAL_FILE)) {
            return initTrial(machineId);
        }

        try {
            String encrypted = Files.readString(TRIAL_FILE);
            String json = decrypt(encrypted, machineId);

            @SuppressWarnings("unchecked")
            Map<String, String> data = objectMapper.readValue(json, Map.class);

            String storedMachineId = data.get("machineId");
            if (!machineId.equals(storedMachineId)) {
                return AuthStatus.expired();
            }

            LocalDate firstLaunch = LocalDate.parse(data.get("firstLaunch"));
            long daysUsed = ChronoUnit.DAYS.between(firstLaunch, LocalDate.now());
            int daysLeft = (int) Math.max(0, TRIAL_DAYS - daysUsed);

            if (daysLeft <= 0) return AuthStatus.expired();

            updateLastSeen(machineId, firstLaunch.toString(), json);
            return AuthStatus.trial(daysLeft);

        } catch (Exception e) {
            // Tampered / deleted / machine changed → expired, not reset
            return AuthStatus.expired();
        }
    }

    private AuthStatus initTrial(String machineId) {
        try {
            String today = LocalDate.now().toString();
            String json = objectMapper.writeValueAsString(Map.of(
                "firstLaunch", today,
                "machineId", machineId,
                "lastSeen", today
            ));
            String encrypted = encrypt(json, machineId);
            Files.createDirectories(TRIAL_FILE.getParent());
            Files.writeString(TRIAL_FILE, encrypted);
            return AuthStatus.trial(TRIAL_DAYS);
        } catch (Exception e) {
            return AuthStatus.expired();
        }
    }

    private void updateLastSeen(String machineId, String firstLaunch, String currentJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(currentJson, Map.class);
            data.put("lastSeen", LocalDate.now().toString());
            String updated = objectMapper.writeValueAsString(data);
            Files.writeString(TRIAL_FILE, encrypt(updated, machineId));
        } catch (Exception ignored) {}
    }

    // ── AES-256-GCM helpers ──────────────────────────────────────────────────

    private SecretKey deriveKey(String machineId) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest((machineId + SALT).getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    private String encrypt(String plaintext, String machineId) throws Exception {
        SecretKey key = deriveKey(machineId);
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + ct.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ct, 0, combined, iv.length, ct.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    private String decrypt(String encrypted, String machineId) throws Exception {
        SecretKey key = deriveKey(machineId);
        byte[] combined = Base64.getDecoder().decode(encrypted);
        byte[] iv = java.util.Arrays.copyOfRange(combined, 0, 12);
        byte[] ct = java.util.Arrays.copyOfRange(combined, 12, combined.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
    }
}
