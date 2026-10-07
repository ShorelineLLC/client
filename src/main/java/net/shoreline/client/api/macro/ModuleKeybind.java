package net.shoreline.client.api.macro;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.shoreline.client.api.module.Toggleable;

public class ModuleKeybind extends Macro
{
    @Getter
    protected final Toggleable module;

    public ModuleKeybind(int keycode, Toggleable module)
    {
        super(module.getId(), keycode, null);
        this.module = module;
    }

    @Override
    public void execute(String command)
    {
        /* We just toggle the module directly instead of executing a ToggleCommand. */
        module.toggle();
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("module", getModule().getId());
        jsonObject.addProperty("keycode", getKeycode());
        return jsonObject;
    }
}
