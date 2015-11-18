package edu.nure.email;

/**
 * Created by bod on 17.09.15.
 */

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;


public class EmailSender {

    private String username;
    private String password;
    private Properties props;

    public EmailSender() {
        this.username = "myuser";
        this.password = "mypassword";
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

    }

    public void send(final String subject, final String text, final String toEmail){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Session session = Session.getDefaultInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress("no+reply@photomanager.com", "Admin"));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                    message.setSubject(subject);
                    message.setText(text);

                    Transport.send(message);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MessagingException ex){
                    ex.printStackTrace();
                }
            }
        }).start();

    }
}

