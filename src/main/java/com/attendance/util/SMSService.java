package com.attendance.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.net.URLEncoder;

public class SMSService {

    // Replace with your actual SMS API key and sender ID
    private static final String API_KEY = "your_api_key_here";
    private static final String SENDER_ID = "ATTENDSYS";
    private static final String SMS_GATEWAY_URL = "https://api.smsgateway.com/send";

    /**
     * Sends an SMS to the given phone number.
     * @param phoneNumber The recipient's phone number.
     * @param message The message content.
     * @return True if the message was sent successfully, false otherwise.
     */
    public static boolean sendSMS(String phoneNumber, String message) {
        try {
            // Encode parameters for URL
            String encodedMessage = URLEncoder.encode(message, "UTF-8");
            String requestData = "apikey=" + API_KEY
                    + "&sender=" + SENDER_ID
                    + "&numbers=" + phoneNumber
                    + "&message=" + encodedMessage;

            // Create URL connection
            URL url = new URL(SMS_GATEWAY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestData.getBytes());
                os.flush();
            }

            // Check response code
            int responseCode = conn.getResponseCode();
            return responseCode == 200; // Successful response

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send SMS.");
            return false;
        }
    }
}
