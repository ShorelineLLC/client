package net.shoreline.client.api;

import com.google.gson.JsonObject;

public interface Serializable
{
    JsonObject toJson();

    void fromJson(JsonObject jsonObject);
}
