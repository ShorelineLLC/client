package net.shoreline.client.impl.module.hud;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;
import net.shoreline.eventbus.annotation.EventListener;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    Config<Boolean> icons = new BooleanConfig.Builder("Icons")
            .setDescription("Shows the players face next to the info")
            .setDefaultValue(false).build();
    Config<RadarSorting> sortingConfig = new EnumConfig.Builder<RadarSorting>("Sorting")
            .setValues(RadarSorting.values())
            .setDefaultValue(RadarSorting.LENGTH).build();
    Config<Integer> limit = new NumberConfig.Builder<Integer>("Limit")
            .setMin(1).setMax(100).setDefaultValue(10)
            .setDescription("Limit for the length of the text radar.").build();

    public TextRadarHudModule()
    {
        super("TextRadar", "Displays all nearby players", 2, 20);
    }

    @Override
    public void loadEntries() {}

    @Override
    public void drawEntries(DrawContext context, float tickDelta)
    {
        for (DynamicEntry entry : getHudEntries())
        {
            if (entry instanceof PlayerRadarEntry playerEntry)
            {
                if (entry.isDrawing())
                {
                    boolean draw = playerEntry.getPlayer().isDead() || !mc.world.getPlayers().contains(playerEntry.getPlayer());
                    if (draw)
                    {
                        entry.setDrawing(() -> false);
                    }
                }
                else if (entry.isDone())
                {
                    getHudEntries().remove(entry);
                }
            }
        }

        // this limit shit looks horrible but its the best way to do it so the animations dont look clunky
        sortingConfig.getValue().sortEntries(getHudEntries(), isTop());
        int i = 0;
        offset = 0;
        for (DynamicEntry entry : getHudEntries())
        {
            if (i >= limit.getValue())
            {
                entry.setDrawing(() -> false);
            }

            if (entry.isDrawing() || !entry.isDone())
            {
                entry.draw(context, getX() + (isLeft() ? 0 : getWidth()), getY(), offset, tickDelta);
            }

            if (entry.isDrawing())
            {
                i++;
            }
        }

        for (PlayerEntity player : mc.world.getPlayers())
        {
            if (player == mc.player)
            {
                continue;
            }

            PlayerListEntry playerEntry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId());
            if (playerEntry == null)
            {
                continue;
            }

            if (getHudEntries().stream().anyMatch(e -> e instanceof PlayerRadarEntry p && p.getPlayer() == player))
            {
                continue;
            }

            getHudEntries().add(new PlayerRadarEntry(this, player, playerEntry.getSkinTextures().texture()));
        }
    }

    @EventListener
    public void onDisconnect(WorldEvent.Disconnect event)
    {
        getHudEntries().clear();
    }

    @Override
    public void sortEntries()
    {
        sortingConfig.getValue().sortEntries(getHudEntries(), isTop());
    }

    @Getter
    private class PlayerRadarEntry extends DynamicEntry
    {
        private final PlayerEntity player;
        private final Identifier texture;

        public PlayerRadarEntry(DynamicHudModule hudModule, PlayerEntity player, Identifier texture)
        {
            super(hudModule,
                    () ->
                    {
                        StringBuilder builder = new StringBuilder(player.getName().getString());

                        if (pingConfig.getValue() && mc.getNetworkHandler() != null)
                        {
                            PlayerListEntry playerEntry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId());
                            if (playerEntry != null)
                            {
                                builder.append(" ");
                                builder.append(Formatting.WHITE);
                                builder.append(playerEntry.getLatency());
                                builder.append("ms");
                            }
                        }

                        if (distanceConfig.getValue())
                        {
                            builder.append(Formatting.WHITE).append(" ");
                            builder.append(new DecimalFormat("0.0").format(mc.player.distanceTo(player)));
                        }

                        if (totemsConfig.getValue())
                        {
                            int totems = Managers.TOTEM.getTotems(player);
                            if (totems > 0)
                            {
                                builder.append(" ");
                                builder.append(Formatting.WHITE);
                                builder.append(-totems);
                            }
                        }

                        return builder.toString();
                    },

                    () -> player.isAlive() && mc.world.getPlayers().contains(player));

            this.player = player;
            this.texture = texture;
        }

        @Override
        public void drawText(DrawContext context, String string, float x, float y)
        {
            if (icons.getValue() && texture != null)
            {
                PlayerSkinDrawer.draw(context, texture, (int) x - 1, (int) y - 1, 8, true, false, 0xFFFFFFFF);
                super.drawText(context, string, x + 10, y);
                return;
            }

            super.drawText(context, string, x, y);
        }
    }

    private enum RadarSorting
    {
        DISTANCE
        {
            @Override
            public void sortEntries(List<DynamicEntry> entries, boolean top)
            {
                entries.sort(Comparator.comparingDouble(entry ->
                {
                    if (entry instanceof PlayerRadarEntry playerEntry)
                    {
                        double distance = mc.player.distanceTo(playerEntry.getPlayer());
                        return distance * (top ? 1 : -1);
                    }
                    else
                    {
                        return top ? Double.MAX_VALUE : -Double.MAX_VALUE;
                    }
                }));
            }
        },
        ALPHABETICAL
        {
            @Override
            public void sortEntries(List<DynamicEntry> entries, boolean top)
            {
                entries.sort(Comparator.comparing(entry -> entry.getText().get()));
            }
        },
        LENGTH
        {
            @Override
            public void sortEntries(List<DynamicEntry> entries, boolean top)
            {
                entries.sort(Comparator.comparingDouble(entry -> Managers.RENDER.getTextWidth(entry.getText().get()) * (top ? -1 : 1)));
            }
        };

        public abstract void sortEntries(List<DynamicEntry> entries, boolean top);
    }
}
