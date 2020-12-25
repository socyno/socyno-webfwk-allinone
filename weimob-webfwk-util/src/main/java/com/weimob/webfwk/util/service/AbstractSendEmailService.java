package com.weimob.webfwk.util.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.weimob.webfwk.util.tool.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public abstract class AbstractSendEmailService {
    
    public static class InternetAddresses extends HashSet<InternetAddress> {
        
        private static final long serialVersionUID = 1L;
        
        public void add(String... addresses) throws AddressException {
            for (String address : addresses) {
                if (StringUtils.isBlank(address)) {
                    continue;
                }
                this.add(new InternetAddress(address));
            }
        }
        
        @Override
        public InternetAddress[] toArray() {
            return super.toArray(new InternetAddress[0]) ;
        }
    }
    
    @Data
    public static class EmailEntity {

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private InternetAddresses addressesTo;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private InternetAddresses addressesCc;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private InternetAddresses addressesBcc;
        
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private Set<File> attachments;
        
        private String contentType;
        
        private String subject;
        
        private String body;
        
        @Setter(AccessLevel.NONE)
        private InternetAddress from;
        
        public InternetAddresses getAddressesTo() {
            if (addressesTo == null) {
                addressesTo = new InternetAddresses();
            }
            return addressesTo;
        }
        
        public InternetAddresses getAddressesCc() {
            if (addressesCc == null) {
                addressesCc = new InternetAddresses();
            }
            return addressesCc;
        }
        
        public InternetAddresses getAddressesBcc() {
            if (addressesBcc == null) {
                addressesBcc = new InternetAddresses();
            }
            return addressesBcc;
        }
        
        public Set<File> getAttachments() {
            if (attachments == null) {
                attachments = new HashSet<>();
            }
            return attachments;
        }
        
        public boolean hasAattchments() {
            return attachments != null && attachments.size() > 0;
        }
        
        public void setFrom(String address) throws AddressException {
            this.from = new InternetAddress(address);
        }
        
        public void setFrom(InternetAddress address) {
            this.from = address;
        }
        
        public void addAttachment(File... attachments) throws AddressException {
            for (File file : attachments) {
                if (file != null) {
                    this.attachments.add(file);
                }
            }
        }
    }
    
    protected abstract String getSmtpHost();
    
    protected abstract String getUsername();
    
    protected abstract String getPassword();
    
    protected int getSmtpPort() {
        return 25;
    }
    
    protected boolean getDebugMode() {
        return false;
    };
    
    public void send(@NonNull EmailEntity entity) throws MessagingException, UnsupportedEncodingException {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", getSmtpHost());
        props.setProperty("mail.smtp.port", "" + getSmtpPort());
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        
        Session session = null;
        if (StringUtils.isNotBlank(getUsername()) && StringUtils.isNotBlank(getPassword())) {
            props.put("mail.smtp.auth", "true");
            session = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getUsername(), getPassword());
                }
            });
        } else {
            session = Session.getDefaultInstance(props);
        }
        session.setDebug(getDebugMode());
        
        Message msg = new MimeMessage(session);
        InternetAddress[] addressesTo;
        entity.getAddressesTo().remove(null);
        if ((addressesTo = entity.getAddressesTo().toArray()).length > 0) {
            msg.setRecipients(Message.RecipientType.TO, addressesTo);
        }
        InternetAddress[] addressesCc;
        entity.getAddressesCc().remove(null);
        if ((addressesCc = entity.getAddressesCc().toArray()).length > 0) {
            msg.setRecipients(Message.RecipientType.CC, addressesCc);
        }
        InternetAddress[] addressesBcc;
        entity.getAddressesBcc().remove(null);
        if ((addressesBcc = entity.getAddressesBcc().toArray()).length > 0) {
            msg.setRecipients(Message.RecipientType.BCC, addressesBcc);
        }
        
        String contentType;
        if(StringUtils.isBlank(contentType = entity.getContentType())) {
            contentType = "text/html;charset=utf-8";
        }
        Set<File> attachments;
        if ((attachments = entity.getAttachments()).size() > 0) {
            BodyPart mdp = new MimeBodyPart();
            mdp.setContent(StringUtils.trimToEmpty(entity.getBody()), contentType);
            Multipart mm = new MimeMultipart();
            mm.addBodyPart(mdp);
            MimeBodyPart filePart;
            FileDataSource fileSource;
            for (File file : attachments) {
                if (file == null) {
                    continue;
                }
                filePart = new MimeBodyPart();
                fileSource = new FileDataSource(file);
                filePart.setDataHandler(new DataHandler(fileSource));
                filePart.setFileName(MimeUtility.encodeText(fileSource.getName()));
                mm.addBodyPart(filePart);
            }
            msg.setContent(mm);
        } else {
            msg.setContent(StringUtils.trimToEmpty(entity.getBody()), contentType);
        }
        msg.setSentDate(new Date());
        msg.setSubject(StringUtils.trimToEmpty(entity.getSubject()));
        if (entity.getFrom() != null) {
            msg.setFrom(entity.getFrom());
        }
        msg.saveChanges();
        Transport.send(msg);
    }
}
