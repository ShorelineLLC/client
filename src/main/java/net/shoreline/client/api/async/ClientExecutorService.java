package net.shoreline.client.api.async;

import java.util.concurrent.*;

public class ClientExecutorService
{
    public static ClientExecutorService INSTANCE = new ClientExecutorService();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Future<?> submit(Callable<?> callable)
    {
        return executor.submit(callable);
    }
}
