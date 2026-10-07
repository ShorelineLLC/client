package net.shoreline.client.api.config;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

public class BooleanConfig extends Config<Boolean>
{
    @Getter
    @Setter
    private boolean visibilityDependant;

    public BooleanConfig(String name, String description)
    {
        super(name, description);
    }

    @Override
    public Boolean getValue()
    {
        return (!visibilityDependant || isVisible()) && super.getValue();
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject jsonObject = super.toJson();
        jsonObject.addProperty("value", getValue());
        return jsonObject;
    }

    public static class Builder extends ConfigBuilder<Boolean>
    {
        private boolean visibilityDependant;

        public Builder(String name) {
            super(name);
        }

        public Builder setVisibilityDependant(boolean visibilityDependant)
        {
            this.visibilityDependant = visibilityDependant;
            return this;
        }

        @Override
        public BooleanConfig build()
        {
            BooleanConfig config = (BooleanConfig) super.build();
            config.setVisibilityDependant(visibilityDependant);
            return config;
        }
    }
}
