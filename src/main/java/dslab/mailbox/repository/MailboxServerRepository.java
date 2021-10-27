package dslab.mailbox.repository;

import dslab.util.Config;
import dslab.util.worker.repository.ConcurrentWorkerRepository;

public class MailboxServerRepository extends ConcurrentWorkerRepository {

    private final Config config;
    private final IMailboxDataRepository dataRepo;

    public MailboxServerRepository(Config config) {
        this.config = config;
        this.dataRepo = new MailboxDataRepository(new Config(config.getString("users.config")));
    }

    public Config getConfig() {
        return config;
    }

    public IMailboxDataRepository getDataRepo() {
        return dataRepo;
    }
}
