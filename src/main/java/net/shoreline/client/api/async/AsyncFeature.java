package net.shoreline.client.api.async;

import net.shoreline.client.api.GenericFeature;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncFeature<T> extends GenericFeature
{
    protected Future<T> currentResult;
    private final T nullValue;

    public AsyncFeature(String name)
    {
        this(name, null);
    }

    public AsyncFeature(String name, T defaultValue)
    {
        this(name, new String[0], defaultValue);
    }

    public AsyncFeature(String name, String[] nameAliases, T defaultValue)
    {
        super(name, nameAliases);
        this.nullValue = defaultValue;
    }

    @SuppressWarnings("unchecked cast")
    public void runAsync(Callable<T> calc)
    {
        currentResult = (Future<T>) ClientExecutorService.INSTANCE.submit(calc);
    }

    public void cancelRun()
    {
        if (currentResult != null)
        {
            currentResult.cancel(false);
            currentResult = null;
        }
    }

    public T getResults()
    {
        if (currentResult == null || !currentResult.isDone())
        {
            return nullValue;
        }

        try
        {
            return currentResult.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
            return nullValue;
        }
    }
}
