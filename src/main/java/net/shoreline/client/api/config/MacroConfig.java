package net.shoreline.client.api.config;

import net.shoreline.client.api.macro.Macro;

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
    }
}
