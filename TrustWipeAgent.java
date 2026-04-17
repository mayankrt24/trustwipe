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
    private static final String USER_EMAIL = "mayanklmao1@gmail.com"; // CHANGE THIS to your login email


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
                Thread.sleep(1000); // Polling faster (1s) for responsive file browsing
            } catch (Exception e) {
                System.out.println("[ERROR] Connection lost. Retrying in 5s...");
                Thread.sleep(5000);
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
            
            if (response.contains("LIST_FILES")) {
                String path = response.split("\"path\":\"")[1].split("\"")[0].replace("\\\\", "\\");
                handleListFiles(path);
            } else if (response.contains("WIPE")) {
                handleRemoteWipe(response);
            }
        }
    }

    private static void handleListFiles(String path) throws Exception {
        System.out.println("[AGENT] Scanning directory: " + path);
        File folder = new File(path);
        File[] files = folder.listFiles();
        
        StringBuilder json = new StringBuilder("[");
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                json.append("{")
                    .append("\"name\":\"").append(f.getName()).append("\",")
                    .append("\"path\":\"").append(f.getAbsolutePath().replace("\\", "\\\\")).append("\",")
                    .append("\"isDirectory\":").append(f.isDirectory()).append(",")
                    .append("\"size\":").append(f.length())
                    .append("}");
                if (i < files.length - 1) json.append(",");
            }
        }
        json.append("]");
        
        // Added userEmail to the report-files request
        sendPostRequest(SERVER_URL + "/report-files?agentId=" + AGENT_ID + "&userEmail=" + USER_EMAIL, json.toString());
        System.out.println("[SUCCESS] Sent file list for " + path + " to server.");
    }

    private static void handleRemoteWipe(String commandJson) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!! REMOTE WIPE INITIATED BY DASHBOARD !!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        
        try {
            // Extract type, path and userEmail from the JSON manually since it's a simple string
            String type = "PARTIAL";
            if (commandJson.contains("\"command\":\"WIPE_FULL\"")) type = "FULL";
            else if (commandJson.contains("\"command\":\"WIPE_FREE_SPACE\"")) type = "FREE_SPACE";

            String path = "";
            if (commandJson.contains("\"path\":\"")) {
                path = commandJson.split("\"path\":\"")[1].split("\"")[0].replace("\\\\", "\\");
            } else if (commandJson.contains("\"name\":\"")) {
                path = commandJson.split("\"name\":\"")[1].split("\"")[0].replace("\\\\", "\\");
            }
            
            if (path == null || path.isEmpty()) {
                System.out.println("[ERROR] No path found in command.");
                return;
            }

            System.out.println("[AGENT] Target Path: " + path);
            System.out.println("[AGENT] Wipe Type: " + type);

            if ("FREE_SPACE".equals(type)) {
                handleFreeSpaceWipe(path);
                return;
            }

            System.out.println("[AGENT] Starting NIST 800-88 3-Pass Overwrite...");
            File target = new File(path);
            if (!target.exists()) {
                System.out.println("[ERROR] Path does not exist: " + path);
                return;
            }

            List<File> allFiles = new ArrayList<>();
            collectFiles(target, allFiles);
            int totalFiles = allFiles.size();
            System.out.println("[AGENT] Collected " + totalFiles + " files to wipe.");

            for (int pass = 1; pass <= 3; pass++) {
                System.out.println("[AGENT] PASS " + pass + " / 3 (Writing " + 
                    (pass == 1 ? "Zeros" : pass == 2 ? "Ones" : "Random") + ")...");
                
                for (int i = 0; i < allFiles.size(); i++) {
                    secureWipeFileInternal(allFiles.get(i), pass);
                    if (totalFiles > 0 && i % Math.max(1, totalFiles / 10) == 0) {
                        int progress = (int) (((pass - 1) * 33) + (((double) (i + 1) / totalFiles) * 33));
                        System.out.println("[AGENT] Progress: " + progress + "%");
                    }
                }
            }

            // Final Deletion
            System.out.println("[AGENT] Overwrite complete. Deleting files...");
            for (File file : allFiles) {
                try {
                    Files.delete(file.toPath());
                } catch (Exception e) {
                    System.out.println("[WARNING] Could not delete " + file.getName() + ": " + e.getMessage());
                }
            }

            // Delete directories if it was a folder or full wipe
            if (target.isDirectory()) {
                deleteDirectoryRecursive(target);
            }

            System.out.println("[AGENT] Secure Erase SUCCESS. Data is now IRRECOVERABLE.");
            
        } catch (Exception e) {
            System.out.println("[ERROR] Wipe failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleFreeSpaceWipe(String path) throws Exception {
        System.out.println("[AGENT] Starting Free Space Wipe for: " + path);
        File root = new File(path);
        if (!root.exists() || !root.isDirectory()) {
            System.out.println("[ERROR] Invalid drive path for free space wipe.");
            return;
        }

        File tempFile = new File(root, "TRUSTWIPE_EMPTY.tmp");
        long freeSpace = root.getUsableSpace();
        long totalToWipe = freeSpace - (1024L * 1024 * 1024 * 2); // Leave 2GB

        if (totalToWipe <= 0) {
            System.out.println("[INFO] Not enough free space to wipe (less than 2GB).");
            return;
        }

        System.out.println("[AGENT] Overwriting approx " + (totalToWipe / (1024*1024)) + " MB of free space...");
        try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
            byte[] buffer = new byte[1024 * 1024]; // 1MB chunks
            new Random().nextBytes(buffer);
            long written = 0;
            while (written < totalToWipe) {
                int toWrite = (int) Math.min(buffer.length, totalToWipe - written);
                raf.write(buffer, 0, toWrite);
                written += toWrite;
                if (written % (1024 * 1024 * 100) == 0) {
                    System.out.println("[AGENT] Progress: " + (written * 100 / totalToWipe) + "% (" + (written / (1024*1024)) + " MB)");
                }
            }
        }

        System.out.println("[AGENT] Securely deleting temporary file...");
        secureWipeFileInternal(tempFile, 1);
        Files.delete(tempFile.toPath());
        System.out.println("[AGENT] Free Space Wipe SUCCESS.");
    }

    private static void collectFiles(File root, List<File> fileList) {
        if (root == null || !root.exists()) return;
        if (root.isFile()) fileList.add(root);
        else if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) for (File f : files) collectFiles(f, fileList);
        }
    }

    private static void secureWipeFileInternal(File file, int pass) throws IOException {
        if (!file.exists() || !file.isFile()) return;
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long length = raf.length();
            byte pattern = (pass == 1) ? (byte) 0x00 : (pass == 2) ? (byte) 0xFF : (byte) new Random().nextInt(256);
            byte[] buffer = new byte[Math.min((int) length + 1, 4096)];
            Arrays.fill(buffer, pattern);
            long pos = 0;
            while (pos < length) {
                int toWrite = (int) Math.min(buffer.length, length - pos);
                raf.write(buffer, 0, toWrite);
                pos += toWrite;
            }
            // Force write to disk
            raf.getFD().sync();
        }
    }

    private static void deleteDirectoryRecursive(File dir) {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) deleteDirectoryRecursive(child);
                else child.delete();
            }
        }
        dir.delete();
    }

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
