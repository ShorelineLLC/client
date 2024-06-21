package net.shoreline.client.api.waypoint;

public class UserWaypoint extends Waypoint
{
    /**
     * @param name
     * @param ip
     * @param dimension
     * @param x
     * @param y
     * @param z
     */
    public UserWaypoint(String name, String ip, int dimension, double x, double y, double z)
    {
        super(name, ip, dimension, x, y, z);
    }
}
