package net.shoreline.client.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NumberFormat
{
    PERCENT("%"),
    METERS("m"),
    MILLIS("ms"),
    SECONDS("s"),
    DEGREES("deg");

    @Getter
    private final String units;
}
