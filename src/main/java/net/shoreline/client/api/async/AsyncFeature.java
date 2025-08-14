package net.shoreline.client.api.async;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.shoreline.client.api.GenericFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncFeature<T> extends GenericFeature
{
    protected Future<List<T>> currentResult;

    public AsyncFeature(String name)
    {
        super(name);
    }

    public AsyncFeature(String name, String[] nameAliases)
    {
        super(name, nameAliases);
    }

    @SuppressWarnings("unchecked cast")
    public void runAsync(Callable<List<T>> calc)
    {
        currentResult = (Future<List<T>>) ClientExecutorService.INSTANCE.submit(calc);
    }

    public List<T> getResults()
    {
        if (currentResult == null || !currentResult.isDone())
        {
            return new ArrayList<>();
        }

        try
        {
            return currentResult.get();
        } catch (InterruptedException | ExecutionException e)
        {
            return null;
        }
    }
}
