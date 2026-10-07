package net.shoreline.client.api.macro;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.api.LoggingFeature;
import net.shoreline.client.api.Serializable;
import net.shoreline.client.impl.Managers;

@Getter
@Setter
public class Macro extends LoggingFeature implements Serializable
{
    private int keycode;
    private final String command;

    private boolean hold;

    public Macro(String name, int keycode, String command)
    {
        super(name);
        this.keycode = keycode;
        this.command = command;
    }

    public void onKeyPress()
    {
        execute(getCommand());
    }

    public void execute(String command)
    {
        try
        {
            Managers.COMMANDS.execute(command);
        }
        catch (Exception e)
        {
            sendErrorChatMessage("Failed to execute Macro: " + getName());
            e.printStackTrace();
        }
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("keycode", getKeycode());
        jsonObject.addProperty("command", getCommand());
        return jsonObject;
    }
}
