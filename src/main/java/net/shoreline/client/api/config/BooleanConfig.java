package net.shoreline.client.api.config;

public class BooleanConfig extends Config<Boolean>
{
    public BooleanConfig(String name, String description) {
        super(name, description);
    }

    public static class Builder extends ConfigBuilder<Boolean>
    {
        public Builder(String name, String description) {
            super(name, description);
        }
    }


}
