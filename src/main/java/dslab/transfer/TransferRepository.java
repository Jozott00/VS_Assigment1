package dslab.transfer;

import dslab.util.Config;
import dslab.util.worker.repository.ConcurrentWorkerRepository;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferRepository extends ConcurrentWorkerRepository {

    private final ExecutorService connectionPool = Executors.newCachedThreadPool();
    private final Executor forwardPool = Executors.newFixedThreadPool(3);
    private final Config config;
    private ServerSocket serverSocket;
    private List<TransferSenderTask> activeSenderTasks = Collections.synchronizedList(new ArrayList<>());

    public TransferRepository(Config config) {
        this.config = config;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public Config getConfig() {
        return config;
    }

    public Executor getForwardPool() {
        return forwardPool;
    }

    public ExecutorService getConnectionPool() {
        return connectionPool;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public List<TransferSenderTask> getActiveSenderTasks() {
        return activeSenderTasks;
    }
}
