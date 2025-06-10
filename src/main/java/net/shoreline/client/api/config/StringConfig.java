package net.shoreline.client.api.config;

public class StringConfig extends Config<String>
{
    public StringConfig(String name, String description) {
        super(name, description);
    }

    public static class Builder extends ConfigBuilder<String>
    {
        public Builder(String name, String description) {
            super(name, description);
        }
    }
}
