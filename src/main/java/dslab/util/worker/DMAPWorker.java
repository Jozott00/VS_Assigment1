package dslab.util.worker;

import dslab.exception.ExecutionStopException;
import dslab.util.protocolParser.ProtocolParseException;

import java.net.Socket;

public abstract class DMAPWorker extends Worker {

    protected DMAPWorker(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected void init() {
        comm.writeLine("ok DMAP");
    }

    @Override
    protected void execution() throws ExecutionStopException {
        String input = comm.readLine();

        if (input == null) {
            if (this.clientSocket.isClosed()) {
                throw new ExecutionStopException("Client connection closed.");
            }
            return;
        }

        comm.writeLine("ok");
        if(input.equals("quit")) quit();
    }
}
