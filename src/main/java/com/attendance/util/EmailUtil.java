package com.attendance.util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailUtil {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "your-email@gmail.com";
    private static final String EMAIL_PASSWORD = "your-email-password"; // Use App Password if needed

    /**
     * Sends an email notification
     * @param recipientEmail The recipient's email address
     * @param subject The subject of the email
     * @param messageBody The body content of the email
     */
    public static void sendEmail(String recipientEmail, String subject, String messageBody) {
        // Set mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);

        // Create a session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(messageBody);

            // Send the email
            Transport.send(message);
            System.out.println("Email sent successfully to: " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error sending email to: " + recipientEmail);
        }
    }
}
