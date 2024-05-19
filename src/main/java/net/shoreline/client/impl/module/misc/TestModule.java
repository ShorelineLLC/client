package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.util.math.timer.TickTimer;

import static java.lang.String.format;

public final class TestModule extends ToggleModule
{
    private final TickTimer timer = new TickTimer();
    private SlotActionType lastActionType;
    private int lastButton, lastSlot;

    public TestModule()
    {
        super("Test", "in new york i milly rock", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        lastActionType = null;
        lastButton = -1;
        lastSlot = -1;
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event)
    {
        // Passed 50 ticks since last inventory action
        if (timer.passed(50))
        {
            sendModuleMessage("reset");
            lastActionType = null;
            lastButton = -1;
            lastSlot = -1;
        }
    }

    @EventListener
    public void onPacketOutbound(final PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof ClickSlotC2SPacket packet)
        {
            if (packet.getSyncId() != 0)
            {
                return;
            }

            timer.reset();

            if (lastSlot == packet.getSlot() && lastButton == packet.getButton() && lastActionType == packet.getActionType())
            {
                sendModuleMessage("Duplicate inventory action! wtf man!!!");
                event.setCanceled(true);
                return;
            }

            sendModuleMessage(format("button: %s, slot: %s, action: %s", packet.getButton(), packet.getSlot(), packet.getActionType()));

            lastButton = packet.getButton();
            lastSlot = packet.getSlot();
            lastActionType = packet.getActionType();
        }
    }
}
