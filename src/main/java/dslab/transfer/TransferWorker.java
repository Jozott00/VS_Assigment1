package dslab.transfer;

import dslab.model.ServerSpecificEmail;
import dslab.model.TransferSenderPreparation;
import dslab.util.protocolParser.DMTPInterpreter;
import dslab.util.protocolParser.ProtocolParseException;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransferWorker implements Runnable{

    public static final Executor forwardPool = Executors.newFixedThreadPool(3);

    private final Socket clientSocket;
    private SockCom comm;
//    private DMTPInterpreter interp = new DMTPInterpreter();

    TransferWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        establishCommunication();

        comm.writeLine("ok DMTP");

//        while (!interp.hasQuit()) {
//            String input = comm.readLine();
//
//            if (input == null) {
//                if (this.clientSocket.isClosed()) {
//                    break;
//                }
//                continue;
//            }
//
//            try {
//                comm.writeLine(interp.interpretRequest(input));
//            } catch (ProtocolParseException e) {
//                comm.writeLine(e.getMessage());
//            }
//
//            // if it has sent -> send to server
//            if (interp.hasSend()) {
//                forwardEmail();
//                interp.setHasSend(false);
//            }
//        }

        try {
            this.clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing socket", e);
        }
    }


    private void forwardEmail() {
        // send to mailbox server
//        TransferSenderPreparation config = new TransferSenderPreparation(
//            interp.getEmail()
//        );
//
//        // send valid emails
//        List<ServerSpecificEmail> toSend = config.getToSend();
//        toSend.forEach(e -> forwardPool.execute(new TransferSenderTask(e)));
//
//
//        // send error of domain lookup failures
//        ServerSpecificEmail lookUpFailures = config.getDomainLookUpFailure();
//        if(lookUpFailures != null) {
//            forwardPool.execute(new TransferSenderTask(lookUpFailures));
//        }
    }

    private void establishCommunication() {
        try {
            this.comm = new SockCom(clientSocket);
        } catch (IOException e) {
            throw new RuntimeException("Error initiating communication to client", e);
        }
    }
}
