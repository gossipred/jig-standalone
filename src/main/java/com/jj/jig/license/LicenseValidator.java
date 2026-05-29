package com.jj.jig.license;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LicenseValidator {

    private static final String PUBLIC_KEY_B64 =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1E4JcACFHecydYZQLAuk" +
        "FOuzciI20RgLmXlBXm+5W9/FdzB3+4JRDHCsN5QTu9P5bxEP5s4PlXLzzlxYkvc" +
        "aky1O7jEsKT75tw2c8gLAbAwsoqXOFgC2TAouZxu2xTFzo6n7ZEQjynWcz0La0HR" +
        "ZkPZxDQ5Wqr3YETWhxym7hnN9XRKM9tkna1ITolXtPKW8+xpapo2OJPEZtuEyfpI" +
        "E27aI7fLJXZTEPnv4CwnE4nWjIKQRg0P3o/2ytgNiXIvEjispvpuhT9vqSleXS2G" +
        "0rTtU0BHHUU6syBHvGDVjw0xh1dGbHfv6kkR8UmKW8MbkapzmorGySdyqhH8W6fH" +
        "8pwIDAQAB";

    public static final Path LICENSE_FILE =
        Path.of(System.getProperty("user.home"), ".jig-standalone", "license.lic");

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<LicenseInfo> loadAndValidate() {
        return loadFrom(LICENSE_FILE);
    }

    public Optional<LicenseInfo> loadFrom(Path licFile) {
        if (!Files.exists(licFile)) return Optional.empty();
        try {
            String content = Files.readString(licFile);
            @SuppressWarnings("unchecked")
            Map<String, String> wrapper = objectMapper.readValue(content, Map.class);
            String payloadB64   = wrapper.get("payload");
            String signatureB64 = wrapper.get("signature");
            if (payloadB64 == null || signatureB64 == null) return Optional.empty();

            byte[] payloadBytes   = Base64.getDecoder().decode(payloadB64);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureB64);

            // Verify RSA-SHA256 signature
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(loadPublicKey());
            sig.update(payloadBytes);
            if (!sig.verify(signatureBytes)) return Optional.empty();

            // Parse payload
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
            LicensePayload p = objectMapper.readValue(payloadJson, LicensePayload.class);

            return Optional.of(new LicenseInfo(
                p.version(),
                LicenseType.valueOf(p.type()),
                p.customer(),
                p.machineId(),
                p.issuedAt()  != null ? LocalDate.parse(p.issuedAt())  : null,
                p.expiresAt() != null ? LocalDate.parse(p.expiresAt()) : null
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(PUBLIC_KEY_B64);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LicensePayload(int version, String type, String customer,
                          String machineId, String issuedAt, String expiresAt) {}
}
