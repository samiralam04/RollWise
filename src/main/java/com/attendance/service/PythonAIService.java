package com.attendance.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.json.JSONObject;

public class PythonAIService {
    private static final Logger LOGGER = Logger.getLogger(PythonAIService.class.getName());
    private static final String PYTHON_SERVICE_URL = "http://localhost:5000";

    public JSONObject enrollFace(InputStream imageStream, String filename, String contentType) {
        return uploadImage(PYTHON_SERVICE_URL + "/enroll-face", imageStream, filename, contentType);
    }

    private JSONObject uploadImage(String targetUrl, InputStream imageStream, String filename, String contentType) {
        String boundary = "---" + System.currentTimeMillis() + "---";
        String LINE_FEED = "\r\n";
        HttpURLConnection connection = null;

        try {
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000); // 5 sec
            connection.setReadTimeout(15000); // 15 sec
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                            true)) {

                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + filename + "\"")
                        .append(LINE_FEED);
                writer.append("Content-Type: " + (contentType != null ? contentType : "application/octet-stream"))
                        .append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.flush();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = imageStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();

                writer.append(LINE_FEED);
                writer.flush();
                writer.append("--" + boundary + "--").append(LINE_FEED);
                writer.flush();
            }

            return getResponse(connection);

        } catch (IOException e) {
            String msg = "Error talking to Python service: " + e.getMessage();
            LOGGER.severe(msg);
            return new JSONObject().put("status", "error").put("message",
                    "Python service unreachable. Ensure it is started on port 5000.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public JSONObject recognizeClass(InputStream imageStream, String filename, String contentType,
            String knownEncodingsJson) {
        String boundary = "---" + System.currentTimeMillis() + "---";
        String LINE_FEED = "\r\n";
        HttpURLConnection connection = null;

        try {
            URL url = new URL(PYTHON_SERVICE_URL + "/recognize-class");
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                            true)) {

                // Add Image
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + filename + "\"")
                        .append(LINE_FEED);
                writer.append("Content-Type: " + (contentType != null ? contentType : "application/octet-stream"))
                        .append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.flush();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = imageStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                writer.append(LINE_FEED);

                // Add Known Encodings
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"known_encodings\"").append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.append(knownEncodingsJson).append(LINE_FEED);

                writer.append("--" + boundary + "--").append(LINE_FEED);
                writer.flush();
            }

            return getResponse(connection);

        } catch (IOException e) {
            String msg = "Error talking to Python service: " + e.getMessage();
            LOGGER.severe(msg);
            return new JSONObject().put("status", "error").put("message",
                    "Python service unreachable. Ensure it is started on port 5000.");
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private JSONObject getResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        InputStream stream = (responseCode >= 200 && responseCode < 300) ? connection.getInputStream()
                : connection.getErrorStream();

        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
        }

        String raw = response.toString().trim();
        LOGGER.info("Python raw response: " + raw);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            if (!raw.startsWith("{")) {
                LOGGER.severe("Invalid JSON response from AI service: " + raw);
                return new JSONObject()
                        .put("status", "error")
                        .put("message", "Invalid response from AI service")
                        .put("raw_response", raw.length() > 500 ? raw.substring(0, 500) + "..." : raw);
            }
            return new JSONObject(raw);
        } else {
            LOGGER.severe("Python service returned error code: " + responseCode + ", details: " + raw);
            try {
                if (raw.startsWith("{")) {
                    return new JSONObject(raw);
                }
            } catch (Exception ignored) {
            }
            return new JSONObject().put("status", "error").put("message",
                    "Service error (" + responseCode + "): " + raw);
        }
    }

    public JSONObject verifyLiveness(InputStream imageStream, String filename, String contentType,
            String referenceEncoding) {
        String boundary = "---" + System.currentTimeMillis() + "---";
        String LINE_FEED = "\r\n";
        HttpURLConnection connection = null;

        try {
            URL url = new URL(PYTHON_SERVICE_URL + "/verify-live-student");
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = connection.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                            true)) {

                // Add Image
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + filename + "\"")
                        .append(LINE_FEED);
                writer.append("Content-Type: " + (contentType != null ? contentType : "application/octet-stream"))
                        .append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.flush();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = imageStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                writer.append(LINE_FEED);

                // Add Reference Encoding if available
                if (referenceEncoding != null) {
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"reference_encoding\"").append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.append(referenceEncoding).append(LINE_FEED);
                }

                writer.append("--" + boundary + "--").append(LINE_FEED);
                writer.flush();
            }

            return getResponse(connection);

        } catch (IOException e) {
            String msg = "Error talking to Python service: " + e.getMessage();
            LOGGER.severe(msg);
            return new JSONObject().put("status", "error").put("message",
                    "Python service unreachable. Ensure it is started on port 5000.");
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }
}
