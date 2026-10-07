package net.shoreline.client.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.*;

@UtilityClass
public class LookupUtil
{
    private final Map<String, UUID> cache = new HashMap<>();

    public UUID getUUID(String name)
    {
        UUID cached = cache.get(name);
        if (cached != null)
        {
            return cached;
        }

        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler != null)
        {
            PlayerListEntry entry = handler.getPlayerList().stream()
                    .filter(playerEntry -> playerEntry.getProfile().getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);

            if (entry != null)
            {
                UUID uuid = entry.getProfile().getId();
                cache.put(name, uuid);
                return uuid;
            }
        }

        return null;
    }

    public Map<String, String> getHistory(UUID uuid)
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