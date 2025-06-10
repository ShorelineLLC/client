package net.shoreline.client.api.macro;

import lombok.Getter;
import net.shoreline.client.api.module.Toggleable;

public class ModuleKeybind extends Macro
{
    @Getter
    private final Toggleable module;

    public ModuleKeybind(int keycode, Toggleable module)
    {
        super(keycode, null);
        this.module = module;
    }

    @Override
    public Runnable getCommand()
    {
        return module::toggle;
    }
}
