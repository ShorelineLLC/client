package net.shoreline.client.impl.event.gui.hud;

import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.annotation.Cancelable;

public class OverlayEvent extends Event
{
    @Cancelable
    public static class Fire extends OverlayEvent {}

    @Cancelable
    public static class Water extends OverlayEvent {}

    @Cancelable
    public static class Blocks extends OverlayEvent {}

    @Cancelable
    public static class Portal extends OverlayEvent {}

    @Cancelable
    public static class Pumpkin extends OverlayEvent {}

    @Cancelable
    public static class Frostbite extends OverlayEvent {}

    @Cancelable
    public static class Spyglass extends OverlayEvent {}

    @Cancelable
    public static class BossBar extends OverlayEvent {}
}
