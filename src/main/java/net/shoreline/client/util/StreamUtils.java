package net.shoreline.client.util;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtils
{
    /**
     * Since {@link Comparator} calculates the comparison keys twice per comparison, we end up with O(2NlogN) key
     * calculations. For expensive {@code keyExtractor}s, it's better to pre-emptively calculate the keys, turning it
     * into O(N).
     *
     * @author Crosby
     */
    public static <T, U extends Comparable<? super U>> Stream<T> sortCached(Stream<T> stream, Function<? super T, ? extends U> keyExtractor)
    {
        return stream
                .map(t ->
                {
                    U key = keyExtractor.apply(t);
                    return new Intermediary<>(t, key);
                })
                .sorted(Comparator.comparing(Intermediary::key))
                .map(Intermediary::value);
    }

    private record Intermediary<T, U extends Comparable<? super U>>(T value, U key)
    {
    }
}