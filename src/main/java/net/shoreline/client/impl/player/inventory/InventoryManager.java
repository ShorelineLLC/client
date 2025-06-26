package net.shoreline.client.impl.player.inventory;

import lombok.Getter;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.item.ItemUseEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
public class InventoryManager extends GenericFeature
{
    private int serverSlot;

    private final Object swapLock = new Object();

    private final SwapData current = new SwapData();

    public InventoryManager()
    {
        super("Inventory");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet)
        {
            final int packetSlot = packet.getSelectedSlot();
            if (serverSlot == packetSlot)
            {
                event.setCanceled(true);
                return;
            }
            serverSlot = packetSlot;
        }
    }

    @EventListener
    public void onItemUse(ItemUseEvent event)
    {
        if (mc.player != null && isSilentSwapping())
        {
            event.cancel();
            event.setItemStack(mc.player.getInventory().getStack(serverSlot));
        }
    }

    public boolean isSilentSwapping()
    {
        return mc.player.getInventory().getSelectedSlot() != serverSlot;
    }

    public boolean startSwap(int itemSlot, SilentSwapType swapType)
    {
        if (itemSlot == -1 || mc.player.getInventory().getSelectedSlot() == itemSlot)
        {
            return false;
        }

        synchronized (swapLock)
        {
            if (current.isSwapped())
            {
                return false;
            }

            int fromSlot = mc.player.getInventory().getSelectedSlot();
            int to = mc.player.playerScreenHandler.getSlot(itemSlot).id;
            int from = mc.player.playerScreenHandler.getSlot(fromSlot).id;
            switch (swapType)
            {
                case HOTBAR -> Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(itemSlot));
                case INVENTORY -> mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, to, from, SlotActionType.SWAP, mc.player);
            }

            current.setSlotTo(itemSlot);
            current.setSlotFrom(from);
            current.setSwapType(swapType);
            current.setSwapped(true);
        }

        return true;
    }

    public void endSwap()
    {
        synchronized (swapLock)
        {
            if (!current.isSwapped() || current.getSwapType() == null
                    || current.getSlotFrom() == -1 || current.getSlotTo() == -1)
            {
                return;
            }

            if (isSilentSwapping())
            {
                switch (current.getSwapType())
                {
                    case HOTBAR -> Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().getSelectedSlot()));
                    case INVENTORY -> mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, current.getSlotTo(),
                            current.getSlotFrom(), SlotActionType.SWAP, mc.player);
                }
            }

            current.setSlotTo(-1);
            current.setSlotFrom(-1);
            current.setSwapType(null);
            current.setSwapped(false);
        }
    }
}
