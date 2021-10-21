package dslab.mailbox.worker;

import dslab.util.protocolParser.DMTPInterpreter;
import dslab.util.protocolParser.ProtocolParseException;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;

public class MailboxDMTPWorker implements Runnable{

    private final Socket clientSocket;
    private SockCom comm;
//    private DMTPInterpreter interp = new DMTPInterpreter();

    public MailboxDMTPWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {
        establishCommunication();

        comm.writeLine("ok DMTP");

        while (true) {
            String input = comm.readLine();

            if (input == null) {
                if (this.clientSocket.isClosed()) {
                    break;
                }
                continue;
            }

//            try {
////                comm.writeLine(interp.interpretRequest(input));
//            } catch (ProtocolParseException e) {
//                comm.writeLine(e.getMessage());
//            }

//            if(interp.hasSend()) {
//                storeMail();
//            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    private void storeMail() {
//        interp.getEmail();
//    }

    private void establishCommunication() {
        try {
            this.comm = new SockCom(clientSocket);
        } catch (IOException e) {
            throw new RuntimeException("Error initiating communication to client", e);
        }
    }


}
