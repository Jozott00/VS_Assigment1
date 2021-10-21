package dslab.util.worker;

import dslab.exception.ExecutionStopException;
import dslab.util.protocolParser.DMTPInterpreter;
import dslab.util.protocolParser.IProtocolInterpreter;
import dslab.util.protocolParser.ProtocolParseException;
import dslab.util.protocolParser.listener.DMTPListener;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;

public abstract class DMTPWorker extends Worker implements DMTPListener {

    protected IProtocolInterpreter interp = new DMTPInterpreter(this);

    public DMTPWorker(Socket clientSocket) {
        super(clientSocket);
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

        try {
            comm.writeLine(interp.interpretRequest(input));
        } catch (ProtocolParseException e) {
            comm.writeLine(e.getMessage());
        }
    }

    @Override
    public void onQuit() {
        this.quit();
    }

    @Override
    protected void init() {
        comm.writeLine("ok DMTP");
    }
}
