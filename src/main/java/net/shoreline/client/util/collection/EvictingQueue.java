package net.shoreline.client.util.collection;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Modified implementation of https://guava.dev/releases/15.0/api/docs/com/google/common/collect/EvictingQueue.html
 * backed by a {@link ConcurrentLinkedDeque}.
 *
 * @param <E>
 * @author linus
 * @see ArrayDeque
 * @since 1.0
 */
public class EvictingQueue<E> extends ConcurrentLinkedDeque<E>
{
    //
    private final int limit;

    /**
     * @param limit
     */
    public EvictingQueue(int limit)
    {
        this.limit = limit;
    }

    /**
     * @param element element whose presence in this collection is to be ensured
     * @return
     */
    @Override
    public boolean add(@NotNull E element)
    {
        boolean add = super.add(element);
        while (add && size() > limit)
        {
            super.remove();
        }
        return add;
    }

    /**
     * @param element element whose presence in this collection is to be ensured
     */
    public void addFirst(@NotNull E element)
    {
        super.addFirst(element);
        while (size() > limit)
        {
            super.removeLast();
        }
    }

    /**
     * @return
     */
    public int limit()
    {
        return limit;
    }
}
