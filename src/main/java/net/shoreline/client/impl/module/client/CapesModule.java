package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.imixin.IGameOptions;
import net.shoreline.client.irc.CapeType;
import net.shoreline.client.irc.user.OnlineUser;

@Getter
public class CapesModule extends Toggleable
{
    public static CapesModule INSTANCE;

    Config<CapeType> capeType = new EnumConfig.Builder<CapeType>("Cape")
            .setValues(CapeType.values())
            .setDescription("The cape for the player")
            .setDefaultValue(CapeType.BLACK).build();
    Config<Boolean> optifineCapes = new BooleanConfig.Builder("Optifine")
            .setDescription("Shows optifine capes")
            .setDefaultValue(true).build();

    private boolean prevCapes;

    public CapesModule()
    {
        super("Capes", "Shows client capes", GuiCategory.CLIENT);
        INSTANCE = this;
        unregisterConfig(keybind);
    }

    @Override
    public void onEnable()
    {
        if (mc.options != null)
        {
            prevCapes = ((IGameOptions) mc.options).getPlayerModelParts().contains(PlayerModelPart.CAPE);
            mc.options.setPlayerModelPart(PlayerModelPart.CAPE, true);
        }
    }

    @Override
    public void onDisable()
    {
        if (mc.options == null)
        {
            mc.options.setPlayerModelPart(PlayerModelPart.CAPE, prevCapes);
        }
    }

    private Identifier getCapeIdentifier(OnlineUser onlineUser)
    {
        StringBuilder capePath = new StringBuilder("cape");
        switch (onlineUser.getCapeType())
        {
            case WHITE -> capePath.append("/white_bg");
            case BLACK -> capePath.append("/black_bg");
        }

        switch (onlineUser.getUsertype())
        {
            case RELEASE -> capePath.append("/white.png");
            case BETA -> capePath.append("/blue.png");
            case DEV -> capePath.append("/red.png");
        }

        return Identifier.of(ShorelineMod.MOD_ID, capePath.toString());
    }
}
