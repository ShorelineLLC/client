package net.shoreline.client.util.collection;

import com.google.common.collect.ForwardingQueue;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A non-blocking queue which automatically evicts elements from the head of the queue when
 * attempting to add new elements onto the queue and it is full. This queue orders elements FIFO
 * (first-in-first-out). This data structure is logically equivalent to a circular buffer (i.e.,
 * cyclic buffer or ring buffer).
 *
 * <p>An evicting queue must be configured with a maximum size. Each time an element is added to a
 * full queue, the queue automatically removes its head element. This is different from conventional
 * bounded queues, which either block or reject new elements when full.
 *
 * <p>This class is not thread-safe, and does not accept null elements.
 *
 * @author Kurt Alfred Kluever
 * @since 15.0
 */
public final class FirstOutQueue<E> extends ForwardingQueue<E> implements Serializable
{

    private final ArrayDeque<E> delegate;

    final int maxSize;

    public FirstOutQueue(int maxSize)
    {
        checkArgument(maxSize >= 0, "maxSize (%s) must >= 0", maxSize);
        this.delegate = new ArrayDeque<>(maxSize);
        this.maxSize = maxSize;
    }

    /**
     * Returns the number of additional elements that this queue can accept without evicting; zero if
     * the queue is currently full.
     *
     * @since 16.0
     */
    public int remainingCapacity()
    {
        return maxSize - size();
    }

    @Override
    protected @NotNull ArrayDeque<E> delegate()
    {
        return delegate;
    }

    /**
     * Adds the given element to this queue. If the queue is currently full, the element at the head
     * of the queue is evicted to make room.
     *
     * @return {@code true} always
     */
    @Override
    public boolean offer(E e)
    {
        return add(e);
    }

    /**
     * Adds the given element to this queue. If the queue is currently full, the element at the head
     * of the queue is evicted to make room.
     *
     * @return {@code true} always
     */
    @Override
    public boolean add(E e)
    {
        checkNotNull(e); // check before removing
        if (maxSize == 0)
        {
            return true;
        }
        if (size() == maxSize)
        {
            delegate.remove();
        }
        delegate.add(e);
        return true;
    }

    public E addFirst(E e)
    {
        checkNotNull(e); // check before removing
        if (maxSize == 0)
        {
            return null;
        }
        E removed = null;
        if (size() == maxSize)
        {
            removed = delegate.remove();
        }
        delegate.addFirst(e);
        return removed;
    }

    public E getFirst()
    {
        return delegate.getFirst();
    }

    public E getLast()
    {
        return delegate.getLast();
    }

    @Override
    public boolean addAll(Collection<? extends E> collection)
    {
        int size = collection.size();
        if (size >= maxSize)
        {
            clear();
            return Iterables.addAll(this, Iterables.skip(collection, size - maxSize));
        }
        return standardAddAll(collection);
    }

    @Override // Incompatible return type change. Use inherited implementation
    public Object[] toArray()
    {
        /*
         * If we could, we'd declare the no-arg `Collection.toArray()` to return "Object[] but elements
         * have the same nullness as E." Since we can't, we declare it to return nullable elements, and
         * we can override it in our non-null-guaranteeing subtypes to present a better signature to
         * their users.
         *
         * However, the checker *we* use has this special knowledge about `Collection.toArray()` anyway,
         * so in our implementation code, we can rely on that. That's why the expression below
         * type-checks.
         */
        return super.toArray();
    }
}