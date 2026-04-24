package com.expense.tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail,
                                       String fullName,
                                       String resetCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Expense Tracker — Parolni tiklash");

            String html = """
                    <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px;">
                        <div style="background: linear-gradient(135deg, #6366F1, #8B5CF6); padding: 30px; border-radius: 16px 16px 0 0; text-align: center;">
                            <h1 style="color: white; margin: 0; font-size: 28px;">💸 Expense Tracker</h1>
                        </div>
                        <div style="background: #ffffff; padding: 30px; border-radius: 0 0 16px 16px; border: 1px solid #e5e7eb;">
                            <h2 style="color: #1f2937; margin-top: 0;">Salom, %s! 👋</h2>
                            <p style="color: #6b7280;">Parolni tiklash uchun quyidagi kodni kiriting:</p>
                            <div style="background: #f3f4f6; border-radius: 12px; padding: 20px; text-align: center; margin: 20px 0;">
                                <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #6366F1;">%s</span>
                            </div>
                            <p style="color: #9ca3af; font-size: 14px;">⏰ Bu kod <strong>15 daqiqa</strong> davomida amal qiladi.</p>
                            <p style="color: #9ca3af; font-size: 14px;">Agar siz so'rov yubormagan bo'lsangiz, bu emailni e'tiborsiz qoldiring.</p>
                        </div>
                    </div>
                    """.formatted(fullName, resetCode);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email yuborishda xatolik: " + e.getMessage());
        }
    }

    public void sendVerificationEmail(String toEmail,
                                      String fullName,
                                      String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Expense Tracker — Emailni tasdiqlang");

            String verifyUrl = "https://expensetrackerfrontend-tau.vercel.app/verify-email?token=" + token;

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #6366F1, #8B5CF6); padding: 30px; border-radius: 16px 16px 0 0; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">💸 Expense Tracker</h1>
                    </div>
                    <div style="background: #ffffff; padding: 30px; border-radius: 0 0 16px 16px; border: 1px solid #e5e7eb;">
                        <h2 style="color: #1f2937; margin-top: 0;">Salom, %s! 👋</h2>
                        <p style="color: #6b7280;">Emailingizni tasdiqlash uchun quyidagi tugmani bosing:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s"
                               style="background: linear-gradient(135deg, #6366F1, #8B5CF6);
                                      color: white;
                                      padding: 14px 32px;
                                      border-radius: 12px;
                                      text-decoration: none;
                                      font-weight: bold;
                                      font-size: 16px;">
                                ✅ Emailni tasdiqlash
                            </a>
                        </div>
                        <p style="color: #9ca3af; font-size: 14px;">Yoki quyidagi havolani brauzerga kiriting:</p>
                        <p style="color: #6366F1; font-size: 13px; word-break: break-all;">%s</p>
                        <p style="color: #9ca3af; font-size: 14px;">Agar siz ro'yxatdan o'tmagan bo'lsangiz, bu emailni e'tiborsiz qoldiring.</p>
                    </div>
                </div>
                """.formatted(fullName, verifyUrl, verifyUrl);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email yuborishda xatolik: " + e.getMessage());
        }
    }
}