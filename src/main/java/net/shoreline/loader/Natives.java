package net.shoreline.loader;

/**
 * Native methods and class names cannot be obfuscated since their names are literally
 * linked to internal code. So we make obscure signatures and names for all these methods.
 */
public final class Natives
{
    /**
     * Creates a new instance of the given class and returns it, without calling its constructor
     *
     * @param p0 Class instance to create (Class<?>)
     * @return New instance of p0
     */
    public static native Object stop_decompiling_0(Object p0);

    /**
     * Grabs the mixin config from native memory and returns it
     *
     * @param p0 Null parameter
     * @return Mixin config as a byte[]
     */
    public static native Object stop_decompiling_1(Object p0);

    /**
     * Grabs the mixin refmap from native memory and returns it
     *
     * @param p0 Null parameter
     * @return Refmap as a byte[]
     */
    public static native Object stop_decompiling_2(Object p0);

    /**
     * Downloads the client resources, assigns the mixin config & refmap in native memory
     * Dynamically loads and defines all the client classes
     *
     * @param p0 Null parameter
     * @return Null
     */
    public static native Object stop_decompiling_3(Object p0);

    public static native Object stop_decompiling_4(Object p0);
}
