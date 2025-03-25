package com.attendance.util;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class SMSService {
    public static void sendEmail(String recipient, String subject, String content) {
        final String username = "samir7005alam@gmail.com"; // Replace with your email
        final String password = "qhmo kemf ouag rdac"; // Use App Password for Gmail

        // SMTP Server Settings
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("✅ Email sent successfully to: " + recipient);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Email sending failed.");
        }
    }
}
