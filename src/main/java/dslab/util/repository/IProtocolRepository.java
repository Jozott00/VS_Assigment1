package dslab.util.repository;

import java.util.concurrent.Executor;

public interface IProtocolRepository {

    Executor getThreadPool();

}
