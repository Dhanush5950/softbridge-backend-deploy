package com.softbridge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sent to client immediately after a requirement is submitted.
     */
    @Async
    public void sendSubmissionConfirmation(String toEmail,
                                           String clientName,
                                           String reqId,
                                           String projectName) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(toEmail);
            msg.setSubject("[SoftBridge] Requirement Received — " + reqId);
            msg.setText(
                "Hi " + clientName + ",\n\n" +
                "Thank you for submitting your software requirement!\n\n" +
                "Reference ID : " + reqId + "\n" +
                "Project Name : " + projectName + "\n\n" +
                "Our team will review your submission and make a build decision shortly.\n" +
                "You can track the status of your requirement by logging into your SoftBridge dashboard.\n\n" +
                "Best regards,\n" +
                "The SoftBridge Team\n" +
                "admin@softbridge.com"
            );
            mailSender.send(msg);
            log.info("Confirmation email sent to {} for {}", toEmail, reqId);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Sent to client when admin makes an In-House / Outsource decision.
     */
    @Async
    public void sendDecisionNotification(String toEmail,
                                          String clientName,
                                          String reqId,
                                          String projectName,
                                          String decision,
                                          String adminNotes) {
        try {
            String decisionLabel = "INHOUSE".equals(decision) ? "Build In-House" : "Route to Outsource";
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(toEmail);
            msg.setSubject("[SoftBridge] Decision Made — " + reqId);
            msg.setText(
                "Hi " + clientName + ",\n\n" +
                "Our team has reviewed your software requirement and made a decision.\n\n" +
                "Reference ID  : " + reqId + "\n" +
                "Project Name  : " + projectName + "\n" +
                "Decision      : " + decisionLabel + "\n" +
                (adminNotes != null && !adminNotes.isBlank()
                    ? "Admin Notes   : " + adminNotes + "\n"
                    : "") +
                "\nFor further details or questions, please contact us at admin@softbridge.com.\n\n" +
                "Best regards,\n" +
                "The SoftBridge Team"
            );
            mailSender.send(msg);
            log.info("Decision email sent to {} for {} → {}", toEmail, reqId, decision);
        } catch (Exception e) {
            log.error("Failed to send decision email to {}: {}", toEmail, e.getMessage());
        }
    }
}
