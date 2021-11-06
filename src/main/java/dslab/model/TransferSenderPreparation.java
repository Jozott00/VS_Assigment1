package dslab.model;

import dslab.exception.DomainLookUpException;
import dslab.exception.ValidationException;
import dslab.util.Config;

import java.util.*;

public class TransferSenderPreparation {

    private ServerSpecificEmail domainLookUpFailure;
    private List<ServerSpecificEmail> toSend = new ArrayList<ServerSpecificEmail>();

    private static final Config config = new Config("domains");

    private boolean ignoreFailures = false;

    public TransferSenderPreparation(Email email, String ip) {
        domainLookUp(email, ip);
    }

    public ServerSpecificEmail getDomainLookUpFailure() {
        return domainLookUpFailure;
    }

    public List<ServerSpecificEmail> getToSend() {
        return toSend;
    }


    private void domainLookUp(Email email, String ip) {

        List<String> recipients = new ArrayList<>(email.getRecipients());
        email = new Email(email);
        email.getRecipients().clear();

        HashMap<String, ServerSpecificEmail> emailsToSend = new HashMap<String, ServerSpecificEmail>();

        for (String r : recipients) {
            String domain = getDomain(r);

            String[] ipPort;
            try {
                ipPort = getIpPort(domain);

                if(!emailsToSend.containsKey(domain)) {
                    emailsToSend.put(domain, new ServerSpecificEmail(email, ipPort[0], Integer.parseInt(ipPort[1])));
                }

                emailsToSend.get(domain).getRecipients().add(r);

            } catch (DomainLookUpException e) {
                if(ignoreFailures) continue;

                if(domainLookUpFailure == null ) {
                    try {
                        domainLookUpFailure = createEmailDeliveryFailure(email.getFrom(), "error domain unknown for ", ip);
                        domainLookUpFailure.setFailureMail(true);
                    } catch (DomainLookUpException ex) {
                        ignoreFailures = true;
                        continue;
                    }
                }

                domainLookUpFailure.setData(domainLookUpFailure.getData() + r +  ",");
            }
        }

        for(Map.Entry<String, ServerSpecificEmail> entry : emailsToSend.entrySet()) {
            toSend.add(entry.getValue());
        }
    }

    public static ServerSpecificEmail createEmailDeliveryFailure(String receiver, String data, String ip) throws DomainLookUpException {
        String failureDomain = getDomain(receiver);
        String[] ipPortFailure = getIpPort(failureDomain);

        Email failureEmail = new Email();
        failureEmail.setSubject("Mail Delivery Error");
        failureEmail.setData(data);
        try {
            failureEmail.setFrom("mailer@" + ip);
            failureEmail.setRecipients(new ArrayList<String>(Collections.singleton(receiver)));
        } catch (ValidationException ignored) {}
        int port = Integer.parseInt(ipPortFailure[1]);
        ServerSpecificEmail mail = new ServerSpecificEmail(failureEmail, ipPortFailure[0], port);
        mail.setFailureMail(true);
        return mail;
    }

    public static String getDomain(String email) {
        return email.split("@")[1];
    }

    public static String[] getIpPort(String domain) throws DomainLookUpException {
        try {
            return config.getString(domain).split(":");
        } catch (MissingResourceException e) {
            throw new DomainLookUpException();
        }
    }


}
