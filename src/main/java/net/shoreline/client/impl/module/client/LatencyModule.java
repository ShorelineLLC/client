package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.IdentityHashMap;
import java.util.Map;

public class LatencyModule extends Toggleable
{
    public static LatencyModule INSTANCE;

    Config<Latency> modeConfig = new EnumConfig.Builder<Latency>("Mode")
            .setValues(Latency.values())
            .setDescription("The mode to get server latency")
            .setDefaultValue(Latency.PING).build();

    @Getter
    private int currentLatency;
    private final Map<Integer, Long> trackedLatency = new IdentityHashMap<>();

    private int categoryIndex;
    private final Identifier[] categories = new Identifier[] {
            Identifier.of("minecraft:story/root"),
            Identifier.of("minecraft:recipes/root"),
            Identifier.of("minecraft:nether/root"),
            Identifier.of("minecraft:adventure/root"),
            Identifier.of("minecraft:end/root"),
            Identifier.of("minecraft:husbandry/root")
    };

    public LatencyModule()
    {
        super("Latency", "Attempts to resolve client latency", GuiCategory.CLIENT);
        INSTANCE = this;
    }

    @Override
    public String getModuleData()
    {
        return currentLatency + "ms";
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        if (mc.player.age % 2 == 0 && !(mc.currentScreen instanceof AdvancementsScreen))
        {
            sendPacket(new AdvancementTabC2SPacket(AdvancementTabC2SPacket.Action.OPENED_TAB, categories[categoryIndex]));
            trackedLatency.put(categoryIndex, System.currentTimeMillis());
            categoryIndex++;
            if (categoryIndex >= 6)
            {
                categoryIndex = 0;
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof SelectAdvancementTabS2CPacket packet && packet.getTabId() != null)
        {
            for (int i = 0; i < 6; i++)
            {
                if (!categories[i].equals(packet.getTabId()))
                {
                    continue;
                }

                currentLatency = (int) (System.currentTimeMillis() - trackedLatency.getOrDefault(i, System.currentTimeMillis()));
                break;
            }
        }
    }

    private enum Latency
    {
        TAB, PING
    }
}
