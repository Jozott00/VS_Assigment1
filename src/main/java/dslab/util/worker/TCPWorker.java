package dslab.util.worker;

import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.exception.ExecutionStopException;
import dslab.util.protocolParser.ProtocolParser;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;

public abstract class TCPWorker extends Worker {

    private final Socket clientSocket;
    private SockCom comm;
    private ProtocolParser handler;

    private String initMessage;

    protected TCPWorker(Socket clientSocket, String initMessage) {
        super(clientSocket);
        this.clientSocket = clientSocket;
        this.initMessage = initMessage;
    }

    @Override
    protected void init() {
        establishCommunication();
        handler = new ProtocolParser(this, comm.out());

        comm.writeLine(initMessage);
    }

    @Override
    protected void execution() throws ExecutionStopException {
        String input;
        try {
            input = comm.readLine();
        } catch (IOException e) {
            throw new ExecutionStopException();
        }


        if (input == null) {
            if (this.clientSocket.isClosed()) {
                throw new ExecutionStopException("Client connection closed.");
            }
            return;
        }

        handler.interpretRequest(input);
    }

    @Override
    @Command
    public String quit() {
        comm.writeLine("ok bye");
        return super.quit();
    }

    @Override
    protected void closeConnection() {
        if(!clientSocket.isClosed())
            super.closeConnection();
    }

    private void establishCommunication() {
        try {
            this.comm = new SockCom(clientSocket);
        } catch (IOException e) {
            throw new RuntimeException("Error initiating communication to client", e);
        }
    }
}
