package net.shoreline.client.api.macro;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.shoreline.client.api.Serializable;

@Getter
@Setter
public abstract class Macro implements Serializable
{
    private int keycode;
    private final Runnable command;

    private boolean hold;

    public Macro(int keycode, Runnable command)
    {
        this.keycode = keycode;
        this.command = command;
    }

    public void onKeyPress()
    {
        if (getCommand() != null)
        {
            getCommand().run();
        }
    }
}
