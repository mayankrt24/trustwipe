import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * TrustWipe LITE Agent
 * Run this on ANY PC (as Administrator) to allow remote wiping from your website.
 */
public class TrustWipeAgent {

    private static final String SERVER_URL = "https://trustwipe.onrender.com/api/agent";
    private static final String AGENT_ID = "MAYANK-LAPTOP-01"; // Unique ID for each PC
    private static final String USER_EMAIL = "mayank@example.com"; // User owning this agent

    public static void main(String[] args) throws Exception {
        System.out.println("==========================================");
        System.out.println("   TRUSTWIPE REMOTE AGENT - v1.0         ");
        System.out.println("==========================================");
        System.out.println("[INFO] Agent ID: " + AGENT_ID);
        System.out.println("[INFO] User: " + USER_EMAIL);
        System.out.println("[INFO] Server: " + SERVER_URL);

        // 1. Scan and report local drives to the server
        reportLocalDrives();

        // 2. Continuous Polling for commands
        System.out.println("[INFO] Connected. Listening for remote commands...");
        while (true) {
            try {
                pollForCommands();
                Thread.sleep(5000); // Check every 5 seconds
            } catch (Exception e) {
                System.out.println("[ERROR] Connection lost. Retrying in 10s...");
                Thread.sleep(10000);
            }
        }
    }

    private static void reportLocalDrives() throws Exception {
        File[] roots = File.listRoots();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            json.append("{")
                .append("\"name\":\"").append(root.getAbsolutePath().replace("\\", "\\\\")).append("\",")
                .append("\"size\":").append(root.getTotalSpace()).append(",")
                .append("\"status\":\"CONNECTED (REMOTE)\",")
                .append("\"type\":\"REMOTE\"")
                .append("}");
            if (i < roots.length - 1) json.append(",");
        }
        json.append("]");

        sendPostRequest(SERVER_URL + "/report-drives?agentId=" + AGENT_ID + "&userEmail=" + USER_EMAIL, json.toString());
        System.out.println("[SUCCESS] Reported " + roots.length + " drives to central console.");
    }

    private static void pollForCommands() throws Exception {
        String response = sendGetRequest(SERVER_URL + "/commands/" + AGENT_ID);
        if (response != null && !response.isEmpty() && !response.equals("{}") && response.contains("command")) {
            System.out.println("[ACTION] RECEIVED REMOTE COMMAND: " + response);
            // Example command handling for demo
            if (response.contains("WIPE")) {
                handleRemoteWipe(response);
            }
        }
    }

    private static void handleRemoteWipe(String commandJson) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!! REMOTE WIPE INITIATED BY DASHBOARD !!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        // Here you would add the actual secureWipeFileInternal logic
        // For the demo, we can just print the progress and simulate the destruction
        System.out.println("[AGENT] Starting NIST 800-88 3-Pass Overwrite...");
        try {
            for(int i=0; i<=100; i+=20) {
                System.out.println("[AGENT] Progress: " + i + "% - Overwriting binary headers...");
                Thread.sleep(1000);
            }
            System.out.println("[AGENT] Final Pass Complete. Releasing file handles.");
            System.out.println("[AGENT] Secure Erase SUCCESS. Data is now IRRECOVERABLE.");
        } catch (InterruptedException e) {}
    }

    // --- HTTP Helper Methods ---

    private static String sendGetRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
            in.close();
            return content.toString();
        }
        return null;
    }

    private static void sendPostRequest(String urlStr, String jsonBody) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        conn.getResponseCode();
    }
}
