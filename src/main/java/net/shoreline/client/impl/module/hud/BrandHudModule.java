package net.shoreline.client.impl.module.hud;

import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;

public class BrandHudModule extends DynamicHudModule
{
    public BrandHudModule()
    {
        super("Brand", "Displays the server brand", 200, 300);
    }

    @Override
    public void loadEntries()
    {
        getHudEntries().add(new DynamicEntry(this, this::getBrandText, () -> true));
    }

    public String getBrandText()
    {
        return mc.player.networkHandler.getBrand();
    }
}
