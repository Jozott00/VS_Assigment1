package dslab.util.worker.abstracts;

import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.exception.ExecutionStopException;
import dslab.util.protocolParser.listener.IProtocolListener;
import dslab.util.worker.repository.WorkerRepository;

import java.io.Closeable;
import java.io.IOException;

public abstract class Worker implements Runnable, IProtocolListener {

    protected WorkerRepository repo;

    private final Closeable clientSocket;
    private boolean quit = false;

    protected Worker(Closeable clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        addWorker();
        init();

        while (!quit) {
            try {
                execution();
            } catch (ExecutionStopException e) {
                System.out.println("Stop worker " + this);
                break;
            }
        }

        removeWorker();
        closeConnection();
    }

    public void setup(WorkerRepository repo) {
        this.repo = repo;
    }

    @Override
    @Command
    public String quit() {
        quit = true;
        closeConnection();
        return "ok bye";
    }

    @Override
    public void errorQuit() {
        quit = true;
        closeConnection();
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

    private void addWorker() {
        if(repo == null) throw new RuntimeException("No Worker Repository available");
        repo.getActiveWorkers().add(this);
    }

    private void removeWorker() {
        if(repo == null) throw new RuntimeException("No Worker Repository available");
        repo.getActiveWorkers().remove(this);
    }

}
