package dslab.util.worker;

import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.exception.ExecutionStopException;
import dslab.exception.ValidationException;
import dslab.mailbox.MailboxServer;
import dslab.mailbox.repository.IMailboxRepository;
import dslab.mailbox.repository.MailboxRepository;
import dslab.model.StoredEmail;
import dslab.util.Config;
import dslab.util.protocolParser.ProtocolParser;
import dslab.util.protocolParser.listener.DMAPListener;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

public abstract class DMAPWorker extends TCPWorker implements DMAPListener {

    IMailboxRepository repo = MailboxRepository.getRepo();

    private String loggedInUser;
    private final Config config = new Config(MailboxServer.config.getString("users.config"));

    protected DMAPWorker(Socket clientSocket) {
        super(clientSocket, "ok DMAP");
    }

    @Override
    @Command
    public String login(String username, String password) throws ValidationException {
        if(loggedInUser != null) throw new ValidationException("logout first");

        String pass;
        try {
            pass = config.getString(username);
        } catch (MissingResourceException e) {
            throw new ValidationException("unknown username");
        }

        if(!pass.equals(password)) throw new ValidationException("wrong password");

        this.loggedInUser = username;

        return "ok";
    }

    @Override
    @Command
    public String list() throws ValidationException {
        validateLoggedIn();

        return repo.getAllEmailsBy(loggedInUser)
            .stream()
            .map(e -> String.format("%d %s %s", e.getId(), e.getFrom(), e.getSubject()))
            .collect(Collectors.joining("\n"));
    }

    @Override
    @Command
    public String logout() throws ValidationException {
        loggedInUser = null;
        return "ok";
    }

    @Override
    @Command
    public String show(Integer emailId) throws ValidationException {
        validateLoggedIn();

        StoredEmail email = repo.getByIdAndUser(emailId.longValue(), loggedInUser);
        if(email == null) throw new ValidationException("unknown message id");

        return String.format(
            "from %s \n" +
            "to %s \n" +
                "subject %s \n" +
                "data %s",
            email.getFrom(),
            String.join(",", email.getRecipients()),
            email.getSubject(),
            email.getData()
            );
    }

    @Override
    @Command
    public String delete(Integer emailId) throws ValidationException {
        validateLoggedIn();
        repo.deleteEmailById(emailId.longValue(), loggedInUser);
        return "ok";
    }

    private void validateLoggedIn() {
        if(loggedInUser == null) throw new ValidationException("not logged in");
    }

}
