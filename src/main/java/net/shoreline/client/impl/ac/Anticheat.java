package net.shoreline.client.impl.ac;

public enum Anticheat
{
    VANILLA,
    GRIM;

    public static final float GCD_DIVISOR = (float) (Math.pow(0.20000000298023224D, 3.0D) * 8.0D * 0.15D - 0.001D);
}
