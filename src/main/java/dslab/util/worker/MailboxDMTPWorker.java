package dslab.util.worker;

import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.exception.ValidationException;
import dslab.mailbox.repository.IMailboxDataRepository;
import dslab.model.Email;
import dslab.mailbox.repository.MailboxServerRepository;
import dslab.util.Config;
import dslab.util.worker.abstracts.DMTPWorker;

import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MailboxDMTPWorker extends DMTPWorker {

    private final IMailboxDataRepository repo;
    private final Config config;

    public MailboxDMTPWorker(Socket clientSocket, MailboxServerRepository repo) {
        super(clientSocket);
        this.repo = repo.getDataRepo();
        this.config = repo.getConfig();
    }

    @Override
    @Command
    public String to(String to) throws ValidationException {
        String domain = config.getString("domain");

        List<String> emails = Stream.of(to.split(","))
            .filter(e -> !Email.invalidAddress(e))
            .filter(s -> s.split("@")[1].equals(domain))
            .collect(Collectors.toList());
        List<String> usernames = emails.stream()
            .map(r -> r.trim().split("@")[0])
            .collect(Collectors.toList());

        List<String> storedUsers = repo.getAllUsers();

        List<String> notStoredUsers = usernames.stream()
            .filter(u -> storedUsers.stream()
                .noneMatch(su -> su.equals(u))
            ).collect(Collectors.toList());

        if(!notStoredUsers.isEmpty())
            throw new ValidationException("unknown recipient " + String.join(",", notStoredUsers));

        getEmail().setRecipients(emails);

        return "ok " + emails.size();
    }

    @Override
    @Command
    public String send() {
        getEmail().valid();
        repo.addEmailToUsers(getEmail());

        return "ok";
    }
}
