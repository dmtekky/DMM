package com.nonononoki.alovoa.service;

import com.nonononoki.alovoa.Tools;
import com.nonononoki.alovoa.entity.User;
import com.nonononoki.alovoa.entity.user.Conversation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class MailService {

    private static String UUID_FORMAT = "<br><br><br><p style=\"color:#848484\">%s</p>";

    @Value("${spring.mail.username}")
    private String defaultFrom;

    @Autowired
    private MessageSource messageSource;

    @Autowired(required = false)
    private JavaMailSender mailSender;
    // Added required=false to bypass mail sender dependency for now; add checks in methods if mailSender is null to avoid NPEs during email operations.

    @Value("${app.name}")
    private String appName;

    @Value("${app.domain}")
    private String appDomain;

    @Value("${app.url.front-end}")
    private String appFrontend;

    @Value("${app.company.name}")
    private String companyName;

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    public boolean sendMail(String to, String from, String subject, String body) {
        try {
            if (mailSender == null) {
                return false;
            }
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(getEmailText(body), true);
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public void sendAdminMail(String to, String subject, String body) {
        sendMail(to, defaultFrom, subject, body);
    }

    public void sendAdminMailAll(String subject, String body, List<User> users) throws MessagingException, IOException {
        boolean failed = false;
        for (User u : users) {
            if (!sendMail(u.getEmail(), defaultFrom, subject, body)) {
                failed = true;
            }
        }
        if (failed) {
            throw new MessagingException("One or more emails failed to send. Please check your logs.");
        }
    }

    private String getEmailText(String body) throws IOException {
        String template = Tools.getResourceText("static/templates/email.html");
        String hrefWebsite = appDomain + "/";
        String hrefDonate = appDomain + "/donate-list";
        String imgSrc = Tools.imageToB64("static/img/mail_icon.jpg", "jpeg");
        String text = template.replace("MAIL_BODY", body);
        text = text.replace("COMPANY_NAME", companyName);
        text = text.replace("SRC_IMAGE", imgSrc);
        text = text.replace("HREF_APP_FRONTEND", appFrontend);
        text = text.replace("HREF_WEBSITE", hrefWebsite);
        text = text.replace("HREF_DONATE", hrefDonate);

        return text;
    }

    public void sendRegistrationMail(User user) throws MessagingException, IOException {
        if (mailSender == null) {
            return;
        }
        Locale locale = Tools.getUserLocale(user);
        String subject = messageSource.getMessage("backend.mail.register.subject", new String[]{appName}, "",
                locale);
        String body = messageSource.getMessage("backend.mail.register.body",
                new String[]{user.getFirstName(), appName, appDomain, user.getRegisterToken().getContent()}, "",
                locale);
        body += String.format(UUID_FORMAT, user.getUuid());
        sendMail(user.getEmail(), defaultFrom, subject, body);
    }

    public void sendPasswordResetMail(User user) throws MessagingException, IOException {
        if (mailSender == null) {
            return;
        }
        Locale locale = Tools.getUserLocale(user);
        String subject = messageSource.getMessage("backend.mail.password-reset.subject", new String[]{appName},
                locale);
        String body = messageSource.getMessage("backend.mail.password-reset.body",
                new String[]{user.getFirstName(), appName, appDomain, user.getPasswordToken().getContent()}, locale);
        sendMail(user.getEmail(), defaultFrom, subject, body);
    }

    public void sendAccountDeleteRequest(User user) throws MessagingException, IOException {
        if (mailSender == null) {
            return;
        }
        Locale locale = Tools.getUserLocale(user);
        String subject = messageSource.getMessage("backend.mail.account-delete-request.subject",
                new String[]{appName}, locale);
        String body = messageSource.getMessage("backend.mail.account-delete-request.body",
                new String[]{user.getFirstName(), appName, appDomain, user.getDeleteToken().getContent()}, "",
                locale);
        sendMail(user.getEmail(), defaultFrom, subject, body);
    }

    public void sendAccountDeleteConfirm(User user) throws MessagingException, IOException {
        if (mailSender == null) {
            return;
        }
        Locale locale = Tools.getUserLocale(user);
        String subject = messageSource.getMessage("backend.mail.account-delete-confirm.subject",
                new String[]{appName}, locale);
        String body = messageSource.getMessage("backend.mail.account-delete-confirm.body",
                new String[]{user.getFirstName(), appName}, locale);
        sendMail(user.getEmail(), defaultFrom, subject, body);
    }

    public void sendAccountConfirmed(User user) throws MessagingException, IOException {
        if (mailSender == null) {
            return;
        }
        Locale locale = Tools.getUserLocale(user);
        String subject = messageSource.getMessage("backend.mail.account-confirmed.subject", new String[]{appName},
                locale);
        String body = messageSource.getMessage("backend.mail.account-confirmed.body",
                new String[]{user.getFirstName(), appName, appDomain}, "", locale);
        sendMail(user.getEmail(), defaultFrom, subject, body);
    }

    public void sendLikeNotificationMail(User user) {
        if (mailSender == null) {
            return;
        }
        Locale locale = Tools.getUserLocale(user);
        String subject = messageSource.getMessage("backend.mail.like.subject", new String[]{}, locale);
        String body = messageSource.getMessage("backend.mail.like.body", new String[]{user.getFirstName()}, locale);
        sendMail(user.getEmail(), defaultFrom, subject, body);
    }

    public void sendMatchNotificationMail(User user) {
        if (mailSender == null) {
            return;
        }
        Locale locale = Tools.getUserLocale(user);
        String subject = messageSource.getMessage("backend.mail.match.subject", new String[]{}, locale);
        String body = messageSource.getMessage("backend.mail.match.body", new String[]{user.getFirstName()}, locale);
        sendMail(user.getEmail(), defaultFrom, subject, body);
    }

    public boolean sendChatNotificationMail(User user, User currUser, String message, Conversation conversation) {
        if (mailSender == null) {
            return false;
        }
        //only send mail notification if the previous message was NOT from current user in order to avoid spam
        if (conversation.getMessages().size() <= 1 ||
                !Objects.equals(conversation.getMessages().get(conversation.getMessages().size() - 2).getUserFrom().getId(), currUser.getId())) {
            Locale locale = Tools.getUserLocale(user);
            String subject = messageSource.getMessage("backend.mail.chat.subject", new String[]{}, locale);
            String body = messageSource.getMessage("backend.mail.chat.body", new String[]{currUser.getFirstName(), user.getFirstName(), message}, locale);
            sendMail(defaultFrom, user.getEmail(), subject, body);
            return true;
        }
        return false;
    }
}
