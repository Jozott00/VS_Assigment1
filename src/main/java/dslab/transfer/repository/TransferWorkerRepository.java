package dslab.transfer.repository;

import dslab.util.repository.IDmtpRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferWorkerRepository implements IDmtpRepository {


    @Override
    public Executor getThreadPool() {
        return null;
    }


//    static private TransferWorkerRepository repo;
//    private TransferWorkerRepository() {}
//    synchronized static public TransferWorkerRepository getRepo() {
//        if(repo == null)
//            repo = new TransferWorkerRepository();
//        return repo;
//    }


}
