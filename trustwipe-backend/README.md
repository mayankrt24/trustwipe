# TrustWipe Backend

This is a Spring Boot backend for TrustWipe, a system for securely wiping IT storage devices.

## Prerequisites

- Java 17 or later
- Maven
- MongoDB

## How to Run

1. **Start MongoDB:**
   Make sure your MongoDB server is running on `mongodb://localhost:27017`.

2. **Run the Spring Boot application:**
   Open a terminal in the `trustwipe-backend` directory and run the following command:
   ```bash
   mvn spring-boot:run
   ```
   The application will start on port 8080.

## How to Test

1. **Compile and run the test client:**
   Open a separate terminal in the `trustwipe-backend` directory and run the following commands:
   ```bash
   javac TrustWipeApiClient.java
   java TrustWipeApiClient
   ```
   This will send requests to the backend and print the responses.

## API Endpoints

- `POST /api/assets`: Create a new asset.
- `GET /api/assets`: Get all assets.
- `GET /api/assets/{id}`: Get an asset by ID.
- `POST /api/wipe/{assetId}`: Start the wiping process for an asset.
- `GET /api/wipe/progress/{assetId}`: Get the progress of the wiping process.
- `GET /api/reports`: Get all wipe reports.