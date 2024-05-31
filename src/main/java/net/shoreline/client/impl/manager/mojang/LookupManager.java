package net.shoreline.client.impl.manager.mojang;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.network.PlayerListEntry;
import net.shoreline.client.util.Globals;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LookupManager implements Globals
{
    private final Map<String, UUID> lookupsUUID = new HashMap<>();
    private final Map<UUID, String> lookupsName = new HashMap<>();

    public UUID getUUIDFromName(String name)
    {
        UUID uuid = lookupsUUID.get(name);
        if (uuid != null)
        {
            return uuid;
        }

        if (mc.getNetworkHandler() != null)
        {
            List<PlayerListEntry> playerListEntries =
                    new ArrayList<>(mc.getNetworkHandler().getPlayerList());
            PlayerListEntry profile = playerListEntries.stream().filter(info -> info.getProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
            if (profile != null)
            {
                UUID result = profile.getProfile().getId();
                lookupsUUID.put(name, result);
                return result;
            }
        }
        return null;
    }

    public String getNameFromUUID(UUID uuid)
    {
        if (lookupsName.containsKey(uuid))
        {
            return lookupsName.get(uuid);
        }
        String uuidString = uuid.toString().replace("-", "");
        String url = String.format("https://api.mojang.com/user/profiles/%s/names", uuidString);
        try
        {
            String name = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            JsonArray array = (JsonArray) JsonParser.parseString(name);
            String player = array.get(array.size() - 1).toString();
            JsonObject object = (JsonObject) JsonParser.parseString(player);
            String result = object.get("name").toString();
            lookupsName.put(uuid, result);
            return result;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Date, String> getNameHistoryFromUUID(UUID uuid)
    {
        Map<Date, String> result = new TreeMap<>(Collections.reverseOrder());
        try
        {
            String uuidString = uuid.toString().replace("-", "");
            String url = String.format("https://api.mojang.com/user/profiles/%s/names", uuidString);
            JsonArray array;
            HttpsURLConnection connection = null;
            try
            {
                connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json");
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder builder = new StringBuilder();
                while (scanner.hasNextLine())
                {
                    builder.append(scanner.nextLine());
                    builder.append('\n');
                }
                scanner.close();
                String json = builder.toString();
                array = JsonParser.parseString(json).getAsJsonArray();
            }
            finally
            {
                if (connection != null)
                {
                    connection.disconnect();
                }
            }
            if (array == null)
            {
                return null;
            }
            for (JsonElement element : array)
            {
                JsonObject object = element.getAsJsonObject();
                String name = object.get("name").getAsString();
                long changedAt = object.has("changedToAt") ? object.get("changedToAt").getAsLong() : 0;
                result.put(new Date(changedAt), name);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
