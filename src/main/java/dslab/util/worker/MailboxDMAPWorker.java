package dslab.util.worker;

import dslab.exception.ExecutionStopException;

import java.net.Socket;

public class MailboxDMAPWorker extends DMAPWorker {

    protected MailboxDMAPWorker(Socket clientSocket) {
        super(clientSocket);
    }

}
