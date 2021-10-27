package dslab.util.worker;

import dslab.mailbox.repository.MailboxServerRepository;
import dslab.transfer.TransferRepository;
import dslab.util.worker.abstracts.Worker;
import dslab.util.worker.repository.WorkerRepository;

import java.net.Socket;

public class WorkerFactory {

    private final WorkerRepository workerRepo;

    public WorkerFactory(WorkerRepository repo) {
        this.workerRepo = repo;
    }

    public Worker createMailboxWorker(Socket conn, ProtocolType type, MailboxServerRepository repo) {
        Worker worker;

        if(type == ProtocolType.DMTP)
            worker = new MailboxDMTPWorker(conn, repo);
        else
            worker = new MailboxDMAPWorker(conn, repo);

        worker.setup(workerRepo);
        return worker;
    }

    public Worker createTransferWorker(Socket conn, TransferRepository repo)
    {
        Worker worker = new TransferDMTPWorker(conn, repo);
        worker.setup(workerRepo);
        return worker;
    }

}
