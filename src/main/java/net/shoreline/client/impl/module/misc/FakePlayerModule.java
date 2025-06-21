package net.shoreline.client.impl.module.misc;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.loader.Loader;

public class FakePlayerModule extends Toggleable
{
    private FakePlayerEntity fakePlayer;

    public FakePlayerModule()
    {
        super("FakePlayer", "Spawns a fake player", GuiCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        if (!checkNull())
        {
            fakePlayer = new FakePlayerEntity(mc.player, "FakePlayer");
            fakePlayer.spawnPlayer();
        }
    }

    @Override
    public void onDisable()
    {
        if (fakePlayer != null && !fakePlayer.isRemoved())
        {
            fakePlayer.despawnPlayer();
        }
    }
}
