package dslab.util.worker;

import dslab.mailbox.repository.MailboxServerRepository;
import dslab.util.worker.abstracts.DMAPWorker;

import java.net.Socket;

public class MailboxDMAPWorker extends DMAPWorker {

    protected MailboxDMAPWorker(Socket clientSocket, MailboxServerRepository repo) {

        super(clientSocket, repo);
    }

}
