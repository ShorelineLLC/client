package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.impl.Managers;

public class MacroConfig extends Config<Macro>
{
    public MacroConfig(String name, String description)
    {
        super(name, description);
    }

    @Override
    public JsonObject toJson()
    {
        return null;
    }

    @Override
    public void setValue(Macro macro)
    {
        Managers.MACROS.unregister(getValue());
        super.setValue(macro);
        Managers.MACROS.register(macro);
    }

    public static class Builder extends ConfigBuilder<Macro>
    {
        public Builder(String name) {
            super(name);
        }

        @Override
        public ConfigBuilder<Macro> setDefaultValue(Macro value)
        {
            super.setDefaultValue(value);
            Managers.MACROS.register(value);
            return this;
        }
    }
}
