package com.attendance.service;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailNotifier {
    private static final String EMAIL_FROM = "your_email@gmail.com"; // Replace with your Gmail
    private static final String APP_PASSWORD ="your_password"; // Replace with your generated App Password

    public static void send(List<String> recipients, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });

        try {
            for (String recipient : recipients) {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);
                message.setText(body);

                Transport.send(message);
                System.out.println("✅ Email sent to: " + recipient);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Error sending email: " + e.getMessage());
        }
    }

    public static void sendAlertToParents(List<String> parentEmails, String alertMessage) {
        String subject = "🚨 Emergency Alert Notification 🚨";
        String body = "Dear Parent,\n\nAn emergency alert has been issued:\n"
                + alertMessage +
                "\n\nPlease take necessary actions.\n\nBest regards,\nCollege Administration";

        send(parentEmails, subject, body);
    }

    public static void sendHolidayNotification(List<String> parentEmails, String dateStr, String reason) {
        String subject = "📢 Holiday Announcement: " + reason;
        String body = "Dear Parent,\n\nWe are pleased to inform you that there will be a holiday on:\n\n" +
                "📅 Date: " + dateStr + "\n📌 Reason: " + reason +
                "\n\nEnjoy your day!\n\nBest Regards,\nCollege Administration";

        send(parentEmails, subject, body);
    }
}
