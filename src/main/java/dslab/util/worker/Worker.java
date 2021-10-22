package dslab.util.worker;

import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.exception.ExecutionStopException;
import dslab.util.protocolParser.listener.IProtocolListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Worker implements Runnable, IProtocolListener {

    static public List<Worker> activeWorkers = Collections.synchronizedList(new ArrayList<>());

    private final Closeable clientSocket;
    private boolean quit = false;

    protected Worker(Closeable clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        activeWorkers.add(this);
        init();

        while (!quit) {
            try {
                execution();
            } catch (ExecutionStopException e) {
                break;
            }
        }

        activeWorkers.remove(this);
        closeConnection();
    }

    @Override
    @Command
    public String quit() {
        quit = true;
        closeConnection();
        return "ok bye";
    }

    protected abstract void init();

    protected abstract void execution() throws ExecutionStopException;

    protected void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
