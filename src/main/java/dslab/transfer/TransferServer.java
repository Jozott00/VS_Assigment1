package dslab.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;
import dslab.util.worker.TransferDMTPWorker;

public class TransferServer implements ITransferServer, Runnable {

    private Config config;
    private ServerSocket serverSocket;
    private Shell shell;

    private final ExecutorService connectionPool = Executors.newCachedThreadPool(); //TODO determine what threadpool is the best
    public  final Executor forwardPool = Executors.newFixedThreadPool(3);

    private Boolean shutdown = false;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {
        this.config = config;

        shell = new Shell(in, out);
        shell.register(this);
        shell.setPrompt(componentId + "> ");
    }

    @Override
    public void run() {

        openServer();
        System.out.println("Listening on port: " + this.serverSocket.getLocalPort());

        Thread loopThread = new Thread(() -> {
            while (!shutdown) {
                Socket newConn;
                try {
                    newConn = this.serverSocket.accept();
                } catch (IOException e) {
                    if(shutdown) {
                        continue;
                    }
                    throw new RuntimeException("Error accepting client connection", e);
                }

                connectionPool.execute(
                    new TransferDMTPWorker(newConn, forwardPool)
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
        connectionPool.shutdown();
        throw new StopShellException();
    }

    public void openServer() {
        int port = config.getInt("tcp.port");
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException("Could not open port " + port, e);
        }
    }

    public void closeServer() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close server", e);
        }
    }

    @Command
    public void connStatus() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) connectionPool;
        System.out.println("------\n" +
            "Threadpool for request execution \n" +
            "Threads: " + executor.getActiveCount() + " \n" +
            "Queue: " + executor.getQueue().size() + " \n" +
            "PoolSize " + executor.getPoolSize() + " \n" +
            "-----");
    }

    @Command
    public void forwardStatus() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) forwardPool;
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
        ThreadPoolExecutor executor = (ThreadPoolExecutor) forwardPool;
        executor.setMaximumPoolSize(count);
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }

}
