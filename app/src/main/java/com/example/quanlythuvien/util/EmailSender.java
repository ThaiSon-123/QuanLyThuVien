package com.example.quanlythuvien.util;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String FROM_EMAIL    = "thaisonpro328@gmail.com";
    private static final String FROM_PASSWORD = "gsar qybv dyea ntwk";

    public interface Callback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void sendPin(String toEmail, String pin, Callback cb) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth",            "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host",            "smtp.gmail.com");
                props.put("mail.smtp.port",            "587");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
                    }
                });

                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(FROM_EMAIL));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                msg.setSubject("Mã xác thực đặt lại mật khẩu - Quản Lý Thư Viện");
                msg.setText(
                        "Xin chào,\n\n" +
                        "Mã PIN đặt lại mật khẩu của bạn là:\n\n" +
                        "  " + pin + "\n\n" +
                        "Mã có hiệu lực trong 5 phút.\n" +
                        "Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n" +
                        "Quản Lý Thư Viện"
                );

                Transport.send(msg);
                cb.onSuccess();
            } catch (Exception e) {
                cb.onFailure(e.getMessage());
            }
        }).start();
    }
}
