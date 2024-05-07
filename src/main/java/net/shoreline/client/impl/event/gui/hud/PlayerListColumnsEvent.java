package net.shoreline.client.impl.event.gui.hud;

import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

@Cancelable
public class PlayerListColumnsEvent extends Event
{
    private int tabHeight;

    public void setTabHeight(int tabHeight) {
        this.tabHeight = tabHeight;
    }

    public int getTabHeight() {
        return tabHeight;
    }
}
