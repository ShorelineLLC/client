package net.shoreline.client.api.macro;

import com.google.gson.JsonObject;
import net.shoreline.client.api.module.Toggleable;

public class HoldKeybind extends ModuleKeybind
{
    public HoldKeybind(int keycode, Toggleable module)
    {
        super(keycode, module);
    }

    @Override
    public Runnable getCommand()
    {
        return () ->
        {
            if (!module.isEnabled())
            {
                module.enable();
            }
        };
    }

    public void onKeyEndPress()
    {
        if (module.isEnabled())
        {
            module.disable();
        }
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("hold", true);
        return jsonObject;
    }
}
