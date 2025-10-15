package net.shoreline.client.impl.event.render;

import net.shoreline.eventbus.Event;

public class GlintTextureEvent extends Event
{
    public static class Pre extends GlintTextureEvent {}

    public static class Post extends GlintTextureEvent {}
}
