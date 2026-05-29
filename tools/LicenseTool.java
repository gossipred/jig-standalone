import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Scanner;

/**
 * JIG License Generator — JJ 專用工具
 *
 * 使用方式：
 *   javac tools/LicenseTool.java -d tools/
 *   java -cp tools LicenseTool
 *
 * 私鑰位置：~/.jig-license/private_key_b64.txt
 */
public class LicenseTool {

    // 從 ~/.jig-license/private_key_b64.txt 讀取私鑰
    private static final Path PRIVATE_KEY_FILE =
        Path.of(System.getProperty("user.home"), ".jig-license", "private_key_b64.txt");

    public static void main(String[] args) throws Exception {
        System.out.println("==============================================");
        System.out.println("  JIG License Generator / 授權產生工具");
        System.out.println("==============================================");
        System.out.println();

        if (!Files.exists(PRIVATE_KEY_FILE)) {
            System.err.println("ERROR: Private key not found at " + PRIVATE_KEY_FILE);
            System.exit(1);
        }

        PrivateKey privateKey = loadPrivateKey();
        Scanner sc = new Scanner(System.in);

        System.out.print("Customer name / 客戶名稱: ");
        String customer = sc.nextLine().trim();

        System.out.print("License type (SUBSCRIPTION / PERPETUAL / MASTER): ");
        String typeStr = sc.nextLine().trim().toUpperCase();
        if (!typeStr.equals("SUBSCRIPTION") && !typeStr.equals("PERPETUAL") && !typeStr.equals("MASTER")) {
            System.err.println("Invalid type.");
            System.exit(1);
        }

        String machineId = "*";
        String expiresAt = "null";

        if (!typeStr.equals("MASTER")) {
            System.out.print("Machine ID / 機器 ID: ");
            machineId = sc.nextLine().trim();
        }

        if (typeStr.equals("SUBSCRIPTION")) {
            System.out.print("Expiry date / 到期日 (yyyy-MM-dd): ");
            expiresAt = "\"" + sc.nextLine().trim() + "\"";
        }

        String today = LocalDate.now().toString();
        String payloadJson = String.format(
            "{\"version\":1,\"type\":\"%s\",\"customer\":\"%s\",\"machineId\":\"%s\",\"issuedAt\":\"%s\",\"expiresAt\":%s}",
            typeStr, escape(customer), escape(machineId), today, expiresAt
        );

        byte[] payloadBytes = payloadJson.getBytes(StandardCharsets.UTF_8);
        String payloadB64 = Base64.getEncoder().encodeToString(payloadBytes);

        // RSA-SHA256 sign
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(payloadBytes);
        String signatureB64 = Base64.getEncoder().encodeToString(sig.sign());

        String licJson = String.format(
            "{\n  \"payload\": \"%s\",\n  \"signature\": \"%s\"\n}",
            payloadB64, signatureB64
        );

        System.out.print("\nOutput file / 輸出檔案路徑 (e.g. customer.lic): ");
        String outPath = sc.nextLine().trim();
        Files.writeString(Path.of(outPath), licJson);

        System.out.println();
        System.out.println("License generated: " + outPath);
        System.out.println("Payload: " + payloadJson);
    }

    private static PrivateKey loadPrivateKey() throws Exception {
        String b64 = Files.readString(PRIVATE_KEY_FILE).trim();
        byte[] keyBytes = Base64.getDecoder().decode(b64);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
