package dslab.util.worker;

import dslab.exception.ExecutionStopException;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;

public abstract class Worker implements Runnable {

    protected final Socket clientSocket;
    protected SockCom comm;
    private boolean quit = false;

    protected Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        establishCommunication();
        init();

        while (!quit) {
            try {
                execution();
            } catch (ExecutionStopException e) {
                break;
            }
        }

        closeConnection();
    }

    protected void quit() {
        quit = true;
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

    private void establishCommunication() {
        try {
            this.comm = new SockCom(clientSocket);
        } catch (IOException e) {
            throw new RuntimeException("Error initiating communication to client", e);
        }
    }

}
