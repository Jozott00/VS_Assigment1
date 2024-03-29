package dslab.mailbox;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.mailbox.repository.MailboxServerRepository;
import dslab.util.Config;
import dslab.util.worker.ProtocolType;
import dslab.util.worker.abstracts.Worker;
import dslab.util.worker.WorkerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MailboxServer implements IMailboxServer, Runnable {

    private ServerSocket dmtpServerSocket;
    private ServerSocket dmapServerSocket;
    private Shell shell;

    private final ExecutorService dmapConnectionPool = Executors.newCachedThreadPool();
    private final ExecutorService dmtpConnectionPool = Executors.newFixedThreadPool(10);

    private Boolean shutdown = false;

    private Config config;

    private final MailboxServerRepository repo;
    private final WorkerFactory factory;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out, MailboxServerRepository repo) {
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

        System.out.println("Listening DMAP on port: " + this.dmapServerSocket.getLocalPort());
        System.out.println("Listening DMTP on port: " + this.dmtpServerSocket.getLocalPort());

        Thread dmapLoop = getRequestLoopThread(ProtocolType.DMAP);
        Thread dmtpLoop = getRequestLoopThread(ProtocolType.DMTP);

        dmapLoop.start();
        dmtpLoop.start();

        shell.run();
        if(!shutdown) {
            try {
                shutdown();
            } catch (StopShellException ignored) {}
        }

        // interrupt thread to avoid deadlock
        dmapLoop.interrupt();
        dmtpLoop.interrupt();

        System.out.println("Server stopped.");
    }

    @Override
    @Command
    public void shutdown() {
        System.out.println("Shutdown Server...");
        closeServer();
        shutdown = true;
        dmapConnectionPool.shutdown();
        dmtpConnectionPool.shutdown();
        repo.getActiveWorkers().forEach(Worker::quit);
        throw new StopShellException();
    }

    public void openServer() {
        int dmapPort = config.getInt("dmap.tcp.port");
        int dmtpPort = config.getInt("dmtp.tcp.port");
        try {
            dmapServerSocket = new ServerSocket(dmapPort);
            dmtpServerSocket = new ServerSocket(dmtpPort);
        } catch (IOException e) {
            closeServer();
            throw new RuntimeException("Could not open port " + dmapPort + " or " + dmtpPort, e);
        }
    }

    public void closeServer() {
        try {
            if(this.dmapServerSocket != null)
                this.dmapServerSocket.close();

            if(this.dmtpServerSocket != null)
                this.dmtpServerSocket.close();

        } catch (IOException e) {
            throw new RuntimeException("Failed to close server", e);
        }
    }

    @Command
    public void dmtpStatus() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) dmtpConnectionPool;
        System.out.println("------\n" +
            "Threadpool for DMTP requests \n" +
            "MaxPoolsize:" + executor.getMaximumPoolSize() + "\n" +
            "Threads: " + executor.getActiveCount() + " \n" +
            "Queue: " + executor.getQueue().size() + " \n" +
            "PoolSize " + executor.getPoolSize() + " \n" +
            "-----");
    }

    @Command
    public void dmapStatus() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) dmapConnectionPool;
        System.out.println("------\n" +
            "Threadpool for DMAP requests \n" +
            "Threads: " + executor.getActiveCount() + " \n" +
            "Queue: " + executor.getQueue().size() + " \n" +
            "PoolSize " + executor.getPoolSize() + " \n" +
            "-----");
    }

    @Command
    public void listUsers() {
        System.out.println(String.join("\n", new Config(config.getString("users.config")).listKeys()));
    }


    private Thread getRequestLoopThread(ProtocolType type) {
        return new Thread(() -> {
            while (!shutdown) {
                Socket newConn;
                try {
                    if(type == ProtocolType.DMAP)
                        newConn = this.dmapServerSocket.accept();
                    else
                        newConn = this.dmtpServerSocket.accept();
                } catch (IOException e) {
                    if(shutdown) {
                        continue;
                    }
                    throw new RuntimeException("Error accepting client connection", e);
                }

                Runnable worker = factory.createMailboxWorker(newConn, type, repo);

                if(type == ProtocolType.DMAP)
                    dmapConnectionPool.execute(worker);
                else
                    dmtpConnectionPool.execute(worker);
            }
        });
    }


    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
