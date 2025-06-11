package net.shoreline.client.api.config;

import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.impl.Managers;

public class MacroConfig extends Config<Macro>
{

    public MacroConfig(String name, String description) {
        super(name, description);
    }

    public static class Builder extends ConfigBuilder<Macro>
    {
        public Builder(String name, String description) {
            super(name, description);
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
