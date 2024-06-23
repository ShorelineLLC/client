package net.shoreline.client.api.waypoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigContainer;
import net.shoreline.client.api.config.setting.NumberConfig;
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
    private final Config<Double> xConfig = register(new NumberConfig<>("X", "X " +
            "position of waypoint.", 0.0D, 0.0D, Double.MAX_VALUE));
    private final Config<Double> yConfig = register(new NumberConfig<>("Y", "Y " +
            "position of waypoint.", 0.0D, 0.0D, Double.MAX_VALUE));
    private final Config<Double> zConfig = register(new NumberConfig<>("Z", "Z " +
            "position of waypoint.", 0.0D, 0.0D, Double.MAX_VALUE));
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
        xConfig.setValue(x);
        yConfig.setValue(y);
        zConfig.setValue(z);
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
        return xConfig.getValue();
    }

    public double getY()
    {
        return yConfig.getValue();
    }

    public double getZ()
    {
        return zConfig.getValue();
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
