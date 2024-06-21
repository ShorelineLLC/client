package net.shoreline.client.api.waypoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.file.ConfigFile;
import net.shoreline.client.init.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author linus
 * @see Waypoint
 * @since 1.0
 */
public class WaypointFile extends ConfigFile
{
    /**
     * @param dir
     */
    public WaypointFile(Path dir)
    {
        super(dir, "waypoints");
    }

    @Override
    public void save()
    {
        try
        {
            Path filepath = getFilepath();
            if (!Files.exists(filepath))
            {
                Files.createFile(filepath);
            }
            final JsonArray array = new JsonArray();
            for (Waypoint point : Managers.WAYPOINT.getWaypoints())
            {
                if (point instanceof UserWaypoint)
                {
                    array.add(point.toJson());
                }
            }
            write(filepath, serialize(array));
        }
        // error writing file
        catch (IOException e)
        {
            Shoreline.error("Could not save file for waypoints.json!");
            e.printStackTrace();
        }
    }

    @Override
    public void load()
    {
        try
        {
            Path filepath = getFilepath();
            if (Files.exists(filepath))
            {
                final String content = read(filepath);
                JsonArray array = parseArray(content);
                if (array == null)
                {
                    return;
                }
                for (JsonElement e : array.asList())
                {
                    JsonObject obj = e.getAsJsonObject();
                    if (!obj.has("tag") || !obj.has("dimension") || !obj.has("ip"))
                    {
                        continue;
                    }
                    JsonElement tag = obj.get("tag");
                    JsonElement ip = obj.get("ip");
                    JsonElement dimension = obj.get("dimension");
                    JsonElement x = obj.get("x");
                    JsonElement y = obj.get("y");
                    JsonElement z = obj.get("z");
                    Managers.WAYPOINT.register(new UserWaypoint(tag.getAsString(),
                            ip.getAsString(), dimension.getAsInt(), x.getAsDouble(), y.getAsDouble(), z.getAsDouble()));
                }
            }
        }
        // error reading file
        catch (IOException e)
        {
            Shoreline.error("Could not read file for waypoints.json!");
            e.printStackTrace();
        }
    }
}
