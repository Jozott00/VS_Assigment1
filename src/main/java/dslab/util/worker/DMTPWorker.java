package dslab.util.worker;

import dslab.util.protocolParser.DMTPInterpreter;
import dslab.util.protocolParser.ProtocolParseException;
import dslab.util.protocolParser.listener.DMTPListener;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;

public abstract class DMTPWorker extends DMTPListener implements Runnable {

    protected final Socket clientSocket;
    protected SockCom comm;
    protected DMTPInterpreter interp = new DMTPInterpreter(this);
    protected boolean quit = false;

    public DMTPWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        establishCommunication();

        comm.writeLine("ok DMTP");

        while (!quit) {
            String input = comm.readLine();

            if (input == null) {
                if (this.clientSocket.isClosed()) {
                    break;
                }
                continue;
            }

            try {
                comm.writeLine(interp.interpretRequest(input));
            } catch (ProtocolParseException e) {
                comm.writeLine(e.getMessage());
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onQuit() {
        this.quit = true;
    }

    private void establishCommunication() {
        try {
            this.comm = new SockCom(clientSocket);
        } catch (IOException e) {
            throw new RuntimeException("Error initiating communication to client", e);
        }
    }

}
