package net.shoreline.client.api;

import java.util.function.Consumer;

public interface Observable<T>
{
    void setValue(T value);

    T getValue();

    void addListener(Consumer<T> l);

    void removeListener(Consumer<T> l);
}
