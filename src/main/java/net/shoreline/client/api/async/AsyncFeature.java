package net.shoreline.client.api.async;

import net.shoreline.client.api.GenericFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncFeature<T> extends GenericFeature
{
    protected Future<Collection<T>> currentResult;

    public AsyncFeature(String name)
    {
        super(name);
    }

    public AsyncFeature(String name, String[] nameAliases)
    {
        super(name, nameAliases);
    }

    @SuppressWarnings("unchecked cast")
    public void runAsync(Callable<Collection<T>> calc)
    {
        currentResult = (Future<Collection<T>>) ClientExecutorService.INSTANCE.submit(calc);
    }

    public void cancelRun()
    {
        if (currentResult != null)
        {
            currentResult.cancel(false);
            currentResult = null;
        }
    }

    public Collection<T> getResults()
    {
        if (currentResult == null || !currentResult.isDone())
        {
            return new ArrayList<>();
        }

        try
        {
            return currentResult.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
