package net.shoreline.client.impl.module.hud;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;

public class TextRadarHudModule extends DynamicHudModule
{
    Config<Boolean> pingConfig = new BooleanConfig.Builder("Ping")
            .setDescription("Shows the player's ping")
            .setDefaultValue(false).build();
    Config<Boolean> distanceConfig = new BooleanConfig.Builder("Distance")
            .setDescription("Shows the distance to the player")
            .setDefaultValue(false).build();
    Config<Boolean> totemsConfig = new BooleanConfig.Builder("Totems")
            .setDescription("Shows the number of totems the player used")
            .setDefaultValue(false).build();

    public TextRadarHudModule()
    {
        super("TextRadar", "Displays all nearby players", 2, 20);
    }

    @Override
    public void drawEntries(DrawContext context, float tickDelta)
    {
        getHudEntries().removeIf(entry -> entry instanceof PlayerRadarEntry playerEntry
                && (playerEntry.getPlayer().isDead()
                || !mc.world.getPlayers().contains(playerEntry.getPlayer())));

        for (PlayerEntity player : mc.world.getPlayers())
        {
            PlayerListEntry playerEntry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId());
            if (playerEntry == null)
            {
                continue;
            }

            if (getHudEntries().stream().anyMatch(e -> e instanceof PlayerRadarEntry p && p.getPlayer() == player))
            {
                continue;
            }

            getHudEntries().add(new PlayerRadarEntry(this, player));
        }

        super.drawEntries(context, tickDelta);
    }

    @Getter
    private class PlayerRadarEntry extends DynamicEntry
    {
        private final PlayerEntity player;

        public PlayerRadarEntry(DynamicHudModule hudModule, PlayerEntity player)
        {
            super(hudModule,
                    () ->
                    {
                        StringBuilder builder = new StringBuilder(player.getName().getString());
                        builder.append(" ");

                        if (pingConfig.getValue() && mc.getNetworkHandler() != null)
                        {
                            PlayerListEntry playerEntry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId());
                            if (playerEntry != null)
                            {
                                builder.append(playerEntry.getLatency());
                                builder.append("ms ");
                            }
                        }

                        if (totemsConfig.getValue())
                        {
                            int totems = Managers.TOTEM.getTotems(player);
                            if (totems > 0)
                            {
                                Formatting pcolor = Formatting.GREEN;

                                if (totems > 1)
                                {
                                    pcolor = Formatting.DARK_GREEN;
                                }
                                if (totems > 2)
                                {
                                    pcolor = Formatting.YELLOW;
                                }
                                if (totems > 3)
                                {
                                    pcolor = Formatting.GOLD;
                                }
                                if (totems > 4)
                                {
                                    pcolor = Formatting.RED;
                                }
                                if (totems > 5)
                                {
                                    pcolor = Formatting.DARK_RED;
                                }

                                builder.append(pcolor);
                                builder.append(-totems);
                            }
                        }

                        return builder.toString();
                    },

                    () -> player.isAlive() && mc.world.getPlayers().contains(player));

            this.player = player;
        }
    }
}
