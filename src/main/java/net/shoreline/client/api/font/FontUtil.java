package net.shoreline.client.api.font;

import lombok.experimental.UtilityClass;

@UtilityClass
class FontUtil {
    public int mulFix(long value, long scalar1616) {
        return (int) ((value * scalar1616) >> 16L);
    }

    public int floor266(int value) {
        return value & (~63);
    }

    public int round266(int value) {
        return floor266(value + 32);
    }

    public int ceil266(int value) {
        return floor266(value + 63);
    }
}
