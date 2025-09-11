package net.shoreline.client.api.preset;

import com.google.gson.JsonObject;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;

import java.io.IOException;
import java.util.List;

public class ModulePreset<T extends Module> extends AbstractPreset<T>
{
    public ModulePreset(String name, List<T> modules) throws IOException
    {
        super(name, modules);
    }

    @Override
    protected void apply(T module, JsonObject object)
    {
        if (module instanceof Toggleable toggleable)
        {
            object.addProperty("toggled", toggleable.isEnabled());
        }
    }

    @Override
    protected void load(T module, JsonObject object)
    {
        if (module instanceof Toggleable toggleable && object.has("toggled"))
        {
            boolean enabled = toggleable.isEnabled();
            if (enabled)
            {
                toggleable.enable();
            }
            else
            {
                toggleable.disable();
            }
        }
    }
}