import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TrustWipeApiClient {

    private static final String BASE_URL = "http://localhost:8081/api";

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Please make sure the TrustWipe backend application is running before running this client.");

        HttpClient client = HttpClient.newHttpClient();

        // 1. Scan for drives
        System.out.println("\n--- Scanning for drives ---");
        HttpRequest scanRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/drives/scan"))
                .GET()
                .build();

        HttpResponse<String> scanResponse = client.send(scanRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Scan Response: " + scanResponse.body());

        // 2. Create a new asset (using new model fields)
        System.out.println("\n--- Creating asset ---");
        String newAsset = """
                {
                    "name": "E:\\\\",
                    "type": "USB",
                    "size": 32000000000,
                    "status": "CONNECTED"
                }
                """;

        HttpRequest createAssetRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/assets"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(newAsset))
                .build();

        HttpResponse<String> createAssetResponse = client.send(createAssetRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Create Asset Response: " + createAssetResponse.body());

        // 3. Get all assets
        System.out.println("\n--- Getting all assets ---");
        HttpRequest getAllAssetsRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/assets"))
                .GET()
                .build();

        HttpResponse<String> getAllAssetsResponse = client.send(getAllAssetsRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Get All Assets Response: " + getAllAssetsResponse.body());

        // 4. Start full wipe (assuming an asset ID)
        // assetId should be grabbed from createAssetResponse in real use
        String assetId = "sample-asset-id"; 
        System.out.println("\n--- Starting Full Wipe (Simulation) ---");
        HttpRequest fullWipeRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/wipe/" + assetId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> fullWipeResponse = client.send(fullWipeRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Full Wipe Response: " + fullWipeResponse.statusCode());

        // 5. Start partial wipe
        System.out.println("\n--- Starting Partial Wipe (Simulation) ---");
        String partialWipeJson = """
                {
                    "assetId": "sample-asset-id",
                    "paths": ["C:\\\\temp\\\\test.txt", "C:\\\\temp\\\\folder"]
                }
                """;
        HttpRequest partialWipeRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/wipe/partial"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(partialWipeJson))
                .build();

        HttpResponse<String> partialWipeResponse = client.send(partialWipeRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Partial Wipe Response: " + partialWipeResponse.statusCode());

        // 6. Get progress
        System.out.println("\n--- Getting Wipe Progress ---");
        HttpRequest progressRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/wipe/progress/" + assetId))
                .GET()
                .build();

        HttpResponse<String> progressResponse = client.send(progressRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Progress: " + progressResponse.body());

        // 7. Get reports
        System.out.println("\n--- Getting All Reports ---");
        HttpRequest reportsRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/reports"))
                .GET()
                .build();

        HttpResponse<String> reportsResponse = client.send(reportsRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Reports: " + reportsResponse.body());
    }
}
