
package com.example.primeiro_projeto.services;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine) {

        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void enviarEmailTexto(
            String para,
            String assunto,
            String corpo) {

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setFrom("onboarding@resend.dev");
        msg.setTo(para);
        msg.setSubject(assunto);
        msg.setText(corpo);

        mailSender.send(msg);
    }

    public void enviarEmailComTemplate(
            String para,
            String assunto,
            String template,
            Map<String, Object> variaveis)
            throws MessagingException {

        Context context = new Context();
        context.setVariables(variaveis);

        String html = templateEngine.process(template, context);

        MimeMessage mime = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(mime, true, "UTF-8");

        helper.setFrom("onboarding@resend.dev");
        helper.setTo(para);
        helper.setSubject(assunto);
        helper.setText(html, true);

        mailSender.send(mime);
    }
}
