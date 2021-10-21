package dslab.mailbox.worker;

import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;

public class MailboxDMAPWorker implements Runnable{
    public final Socket clientSocket;
    public SockCom comm;

    public MailboxDMAPWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {

        try {
            this.comm = new SockCom(clientSocket);
        } catch (IOException e) {
            throw new RuntimeException("Error initiating communication to client", e);
        }

        while (true) {
            String req = comm.readLine().trim();
            System.out.println("Client: " + req);
            comm.writeLine("ok");
            if(req.equals("quit")) {
                break;
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
