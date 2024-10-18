package net.shoreline.client.api.waypoint;

import com.google.gson.JsonObject;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.ConfigContainer;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;

/**
 * @author linus
 * @since 1.0
 */
public class Waypoint extends ConfigContainer implements Globals
{
    //
    private final String ip;
    private final int dimension;
    //
    double x;
    double y;
    double z;
    private final Timer timer;

    /**
     * @param name
     * @param ip
     * @param x
     * @param y
     * @param z
     */
    public Waypoint(String name, String ip, int dimension, double x, double y, double z)
    {
        super(name);
        this.ip = ip;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timer = new CacheTimer();
    }

    /**
     * @param time
     * @return
     */
    private boolean passedTime(long time)
    {
        return timer.passed(time);
    }

    public String getIp()
    {
        return ip;
    }

    @Override
    public JsonObject toJson()
    {
        final JsonObject obj = new JsonObject();
        obj.addProperty("tag", getName());
        obj.addProperty("ip", getIp());
        obj.addProperty("dimension", getDimension());
        obj.addProperty("x", getX());
        obj.addProperty("y", getY());
        obj.addProperty("z", getZ());
        return obj;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }

    /**
     * @return
     */
    public Vec3d getPos()
    {
        return new Vec3d(getX(), getY(), getZ());
    }

    public int getDimension()
    {
        return dimension;
    }
}
