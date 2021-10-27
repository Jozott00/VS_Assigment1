package dslab.util.worker.repository;

import dslab.util.worker.abstracts.Worker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConcurrentWorkerRepository implements WorkerRepository {

    private final List<Worker> activeWorkers = Collections.synchronizedList(new ArrayList<>());

    public ConcurrentWorkerRepository() {}

    @Override
    public List<Worker> getActiveWorkers() {
        return activeWorkers;
    }
}
