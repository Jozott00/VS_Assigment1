package dslab.util.worker;

import dslab.model.Email;
import dslab.model.ServerSpecificEmail;
import dslab.model.TransferSenderPreparation;
import dslab.transfer.TransferSenderTask;
import dslab.util.protocolParser.listener.DMTPListener;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransferDMTPWorker extends DMTPWorker {

    private final Executor executor;

    public TransferDMTPWorker(Socket clientSocket, Executor threadExecutor) {
        super(clientSocket);
        this.executor = threadExecutor;
    }

    @Override
    public void onSend(Email email) {
        // send to mailbox server
        TransferSenderPreparation config = new TransferSenderPreparation(email);

        // send valid emails
        List<ServerSpecificEmail> toSend = config.getToSend();
        toSend.forEach(e -> executor.execute(new TransferSenderTask(e)));

        // send error of domain lookup failures
        ServerSpecificEmail lookUpFailures = config.getDomainLookUpFailure();
//        lookUpFailures.setFailureMail(true);
        if(lookUpFailures != null) {
            executor.execute(new TransferSenderTask(lookUpFailures));
        }
    }
}
