package dslab.util.worker;

import dslab.transfer.TransferServer;

import java.net.Socket;

public class WorkerFactory {

    public static Worker createMailboxWorker(Socket conn, ProtocolType type) {
        if(type == ProtocolType.DMTP)
            return new MailboxDMTPWorker(conn);

        return new MailboxDMAPWorker(conn);
    }

    public static Worker createTransferWorker(Socket conn)
    {
        return new TransferDMTPWorker(conn, TransferServer.getForwardPool());
    }

}
