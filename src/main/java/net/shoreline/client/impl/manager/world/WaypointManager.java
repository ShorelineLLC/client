package net.shoreline.client.impl.manager.world;

import net.shoreline.client.api.waypoint.UserWaypoint;
import net.shoreline.client.api.waypoint.Waypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linus
 * @see Waypoint
 * @since 1.0
 */
public class WaypointManager
{
    //
    private final List<Waypoint> waypoints = new CopyOnWriteArrayList<>();

    /**
     * @param waypoint
     */
    public void register(Waypoint waypoint)
    {
        waypoints.add(waypoint);
    }

    /**
     * @param waypoints
     */
    public void register(Waypoint... waypoints)
    {
        for (Waypoint waypoint : waypoints)
        {
            register(waypoint);
        }
    }

    /**
     * @param waypoint
     * @return
     */
    public boolean remove(Waypoint waypoint)
    {
        return waypoints.remove(waypoint);
    }

    /**
     * @param waypoint
     * @return
     */
    public boolean removeContains(String waypoint)
    {
        return waypoints.removeIf(w -> w.getName().contains(waypoint));
    }

    /**
     * @param waypoint
     * @return
     */
    public boolean remove(String waypoint)
    {
        return waypoints.removeIf(w -> w.getName().equalsIgnoreCase(waypoint));
    }

    public boolean contains(String waypoint)
    {
        return waypoints.stream().anyMatch(w -> w.getName().contains(waypoint));
    }

    public void clear()
    {
        waypoints.removeIf(w -> !(w instanceof UserWaypoint));
    }

    /**
     * @return
     */
    public List<Waypoint> getWaypoints()
    {
        return waypoints;
    }

    /**
     * @return
     */
    public List<String> getIps()
    {
        final List<String> ips = new ArrayList<>();
        for (Waypoint waypoint : getWaypoints())
        {
            ips.add(waypoint.getIp());
        }
        return ips;
    }
}
