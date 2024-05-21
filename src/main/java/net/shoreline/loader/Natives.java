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
     * @param p0 User context information for internal alerting
     * @return A <String, byte[]> map of mixin bytecode
     */
    public static native Object stop_decompiling_3(Object p0);

    /**
     * Loads any late-loading classes that need to be initialized. AKA, any classes that extend MC classes.
     *
     * @param p0 Null parameter
     * @return Null
     */
    public static native Object stop_decompiling_4(Object p0);

    /**
     * Attempts to connect to the webserver and authorize the user
     *
     * @param p0 Null parameter
     * @return String value of the username and UID of the user, seperated by ":"
     */
    public static native Object stop_decompiling_5(Object p0);

    /**
     * Checks with the server to ensure the loader is on its most recent version
     * Will crash if not
     *
     * @param p0 The current loader version
     * @return Null
     */
    public static native Object stop_decompiling_6(Object p0);

    /**
     * Alerts the webhook with the loaded user context, then crashes the client
     *
     * @param p0 A String array of:
     *           [0] = The message to send (or the reason why we are alerting)
     *           [1] = The HWID of the user
     *           [2] = The username of the user
     * @return Null
     */
    public static native Object stop_decompiling_7(Object p0);

    /**
     * Adds the given set of classes to jdk/internal/reflect/Reflection field & method filter map
     *
     * @param p0 A set of classes
     * @return Null
     */
    public static native Object stop_decompiling_8(Object p0);
}
