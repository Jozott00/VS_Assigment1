package dslab.mailbox.worker;

import java.net.Socket;

public class MailboxWorkerFactory {

    public static Runnable createMailboxWorker(Socket conn, ProtocolType type) {
        if(type == ProtocolType.DMTP)
            return new MailboxDMTPWorker(conn);

        return new MailboxDMAPWorker(conn);
    }

}
