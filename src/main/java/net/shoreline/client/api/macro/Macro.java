package net.shoreline.client.api.macro;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Macro
{
    @Getter
    @Setter
    private int keycode;

    @Getter
    private final Runnable command;

    public Macro(int keycode, Runnable command)
    {
        this.keycode = keycode;
        this.command = command;
    }

    public void onKeyPress()
    {
        command.run();
    }
}
