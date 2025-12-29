package net.shoreline.client.api;

import java.util.function.Consumer;

public interface Observable<T>
{
    void setValue(T value);

    T getValue();

    void addObserver(Consumer<T> l);

    void removeObserver(Consumer<T> l);
}
