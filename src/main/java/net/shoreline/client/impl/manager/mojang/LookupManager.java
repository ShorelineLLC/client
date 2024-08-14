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
    private static final Map<String, UUID> LOOKUPS_UUID = new HashMap<>();
    private static final Map<UUID, String> LOOKUPS_NAME = new HashMap<>();

    public UUID getUUIDFromName(String name)
    {
        UUID uuid = LOOKUPS_UUID.get(name);
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
                LOOKUPS_UUID.put(name, result);
                return result;
            }
        }
        return null;
    }

    public String getNameFromUUID(UUID uuid)
    {
        if (LOOKUPS_NAME.containsKey(uuid))
        {
            return LOOKUPS_NAME.get(uuid);
        }
        String url = String.format("https://laby.net/api/v2/user/%s/get-profile", uuid.toString());
        try
        {
            String name = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(name).getAsJsonObject();
            String result = jsonObject.get("username").toString();
            LOOKUPS_NAME.put(uuid, result.replace("\"", ""));
            return result;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> getNameHistoryFromUUID(UUID uuid)
    {
        Map<String, String> result = new TreeMap<>(Collections.reverseOrder());
        try
        {
            String url = String.format("https://laby.net/api/v2/user/%s/get-profile", uuid.toString());
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
                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                array = jsonObject.getAsJsonArray("username_history");
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
                String name = object.get("username").getAsString();
                String changedAt = object.has("changed_at") ? object.get("changed_at").getAsString() : "";
                result.put(changedAt, name);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
