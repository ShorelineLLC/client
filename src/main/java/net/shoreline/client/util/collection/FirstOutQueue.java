package net.shoreline.client.util.collection;

import net.shoreline.client.util.collection.EvictingQueue;
import org.jetbrains.annotations.NotNull;

public class FirstOutQueue<E> extends EvictingQueue<E> {

    public FirstOutQueue(int limit) {
        super(limit);
    }

    @Override
    public void addFirst(@NotNull E element) {
        if (size() + 1 > limit()) {
            super.removeFirst();
        }
        super.addFirst(element);
    }
}
