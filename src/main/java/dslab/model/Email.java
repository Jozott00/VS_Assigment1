package dslab.model;

import dslab.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class Email {

    private String subject;
    private String data;
    private String from;
    private List<String> recipients;

    public Email(){}
    public Email(Email config) {
        subject = config.subject;
        data = config.data;
        from = config.from;
        recipients = new ArrayList<>(config.recipients);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) throws ValidationException {
        if(invalidAddress(from))
            throw new ValidationException("invalid email");

        this.from = from;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) throws ValidationException {
        for(String rec : recipients) {
            if(invalidAddress(rec))
                throw new ValidationException("inclues invalid email");
        }

        this.recipients = recipients;
    }

    public void valid() throws ValidationException {
        if(subject == null || subject.isBlank()) throw new ValidationException("no subject");
        if(data == null || data.isBlank()) throw new ValidationException("no data");
        if(from == null) throw new ValidationException("no from address");
        if(invalidAddress(from)) throw new ValidationException("invalid from address ");
        if(recipients == null || recipients.isEmpty()) throw new ValidationException("no recipients");
        for(String r : recipients)
            if(invalidAddress(r)) throw new ValidationException("includes invalid recipients address");
    }

    public static boolean invalidAddress(String email) {
        return !email.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    }

    @Override
    public String toString() {
        return "Email{" +
            "subject='" + subject + '\'' +
            ", data='" + data + '\'' +
            ", from='" + from + '\'' +
            ", recipients=" + recipients +
            '}';
    }
}
