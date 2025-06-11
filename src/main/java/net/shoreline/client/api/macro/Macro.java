package net.shoreline.client.api.macro;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Macro
{
    private int keycode;
    private final Runnable command;

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
