package dslab.util.worker;

import dslab.exception.ValidationException;
import dslab.mailbox.repository.IMailboxRepository;
import dslab.mailbox.repository.MailboxRepository;
import dslab.model.Email;

import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class MailboxDMTPWorker extends DMTPWorker{

    IMailboxRepository repo = MailboxRepository.getRepo();

    public MailboxDMTPWorker(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    public void onTo(List<String> to) throws ValidationException {
        List<String> usernames = to.stream()
            .map(r -> r.split("@")[0])
            .collect(Collectors.toList());

        //TODO: What if it has wrong domain?

        List<String> storedUsers = repo.getAllUsers();

        List<String> notStoredUsers = usernames.stream()
            .filter(u -> storedUsers.stream()
                .noneMatch(su -> su.equals(u))
            ).collect(Collectors.toList());

        if(!notStoredUsers.isEmpty())
            throw new ValidationException("unkonwn recipient " + String.join(",", notStoredUsers));
    }

    @Override
    public void onSend(Email email) {
        repo.addEmailToUsers(email);
    }
}
