package net.shoreline.client.api;

/**
 * @param <T> The argument type
 * @author linus
 * @since 1.0
 */
public interface Invokable<T>
{
    /**
     * @param arg The argument
     */
    void invoke(T arg);
}
