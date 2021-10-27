package dslab.util.worker.repository;

import dslab.util.worker.abstracts.Worker;

import java.util.List;

public interface WorkerRepository {

    List<Worker> getActiveWorkers();

}
