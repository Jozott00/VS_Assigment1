package dslab.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;
import dslab.util.worker.abstracts.Worker;
import dslab.util.worker.WorkerFactory;

public class TransferServer implements ITransferServer, Runnable {

    private final Config config;
    private final Shell shell;

    private Boolean shutdown = false;
    private TransferRepository repo;
    private WorkerFactory factory;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out, TransferRepository repo) {
        this.config = config;
        this.repo = repo;
        this.factory = new WorkerFactory(repo);

        shell = new Shell(in, out);
        shell.register(this);
        shell.setPrompt(componentId + "> ");
    }

    @Override
    public void run() {

        openServer();

        System.out.println("Listening on port: " + repo.getServerSocket().getLocalPort());

        Thread loopThread = new Thread(() -> {
            while (!shutdown) {
                Socket newConn;
                try {
                    newConn = repo.getServerSocket().accept();
                } catch (IOException e) {
                    if(shutdown) {
                        continue;
                    }
                    throw new RuntimeException("Error accepting client connection", e);
                }

                repo.getConnectionPool().execute(
                    factory.createTransferWorker(newConn, repo)
                );
            }
        });
        loopThread.start();

        shell.run();
        if(!shutdown) {
            shutdown();
        }

        // interrupt thread to avoid deadlock
        loopThread.interrupt();

        System.out.println("Server stopped.");
    }

    @Override
    @Command
    public void shutdown() {
        closeServer();
        shutdown = true;
        repo.getConnectionPool().shutdownNow();
        repo.getActiveWorkers().forEach(Worker::quit);
        repo.getActiveSenderTasks().forEach(TransferSenderTask::closeConnection);

        throw new StopShellException();
    }

    public void openServer() {
        int port = config.getInt("tcp.port");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            repo.setServerSocket(serverSocket);
        } catch (IOException e) {
            throw new RuntimeException("Could not open port " + port, e);
        }
    }

    public void closeServer() {
        try {
            repo.getServerSocket().close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close server", e);
        }
    }

    @Command
    public void connStatus() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) repo.getConnectionPool();
        System.out.println("------\n" +
            "Threadpool for request execution \n" +
            "Threads: " + executor.getActiveCount() + " \n" +
            "Queue: " + executor.getQueue().size() + " \n" +
            "PoolSize " + executor.getPoolSize() + " \n" +
            "-----");
    }

    @Command
    public void forwardStatus() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) repo.getForwardPool();
        System.out.println("------\n" +
            "Threadpool for email forwarding\n" +
            "MaxThreads: " + executor.getMaximumPoolSize() + " \n" +
            "Threads: " + executor.getActiveCount() + " \n" +
            "Queue: " + executor.getQueue().size() + " \n" +
            "PoolSize " + executor.getPoolSize() + " \n" +
            "-----");
    }

    @Command
    public void setMaxForwardThreads(int count) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) repo.getForwardPool();
        executor.setMaximumPoolSize(count);
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }


}
