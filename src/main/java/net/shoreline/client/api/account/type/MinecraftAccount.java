package net.shoreline.client.api.account.type;

import com.google.gson.JsonObject;
import net.minecraft.client.session.Session;

/**
 * @author xgraza
 * @since 03/31/24
 */
public interface MinecraftAccount
{
    Session login();

    String username();

    default JsonObject toJSON()
    {
        final JsonObject object = new JsonObject();
        object.addProperty("username", username());
        return object;
    }
}
