package dslab.util.worker;

import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.model.ServerSpecificEmail;
import dslab.model.TransferSenderPreparation;
import dslab.transfer.TransferRepository;
import dslab.transfer.TransferSenderTask;
import dslab.util.worker.abstracts.DMTPWorker;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executor;

public class TransferDMTPWorker extends DMTPWorker {

    private final Executor executor;
    private final TransferRepository repo;

    public TransferDMTPWorker(Socket clientSocket, TransferRepository repo) {
        super(clientSocket);
        this.executor = repo.getForwardPool();
        this.repo = repo;
    }

    @Override
    @Command
    public String send() {
        getEmail().valid();

        // send to mailbox server
        TransferSenderPreparation config = new TransferSenderPreparation(getEmail());

        // send valid emails
        List<ServerSpecificEmail> toSend = config.getToSend();
        toSend.forEach(e -> executor.execute(new TransferSenderTask(e, repo)));

        // send error of domain lookup failures
        ServerSpecificEmail lookUpFailures = config.getDomainLookUpFailure();

        if(lookUpFailures != null) {
            executor.execute(new TransferSenderTask(lookUpFailures, repo));
        }

        return "ok";
    }

}
