package net.shoreline.client.impl.manager.inventory;

import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.item.ItemUseEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.util.item.ItemUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class InventoryManager extends GenericFeature
{
    private final SwapData.Mutable current = new SwapData.Mutable();
    private final List<SwapData> trackedSwaps = new CopyOnWriteArrayList<>();
    private HotbarCache swapCache;

    private int serverSlot;

    private final Object swapLock = new Object();

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

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        trackedSwaps.clear();
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket(int slot))
        {
            serverSlot = slot;
        }

        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket packet
                && AnticheatModule.INSTANCE.isGrim() && !InventoryUtil.isInInventoryScreen())
        {
            int slot = packet.getSlot() > PlayerInventory.getHotbarSize() ? packet.getSlot() - 36 : packet.getSlot();
            if (packet.getStack().isEmpty() || !PlayerInventory.isValidHotbarIndex(slot))
            {
                return;
            }

            for (SwapData data : trackedSwaps)
            {
                if (data.getSlotTo() != slot && data.getSlotFrom() != slot)
                {
                    continue;
                }

                ItemStack preStack = data.getPreHotbar().getStack(slot);
                if (!ItemUtil.isSameItem(preStack, packet.getStack()))
                {
                    event.cancel();
                    trackedSwaps.remove(data);
                    break;
                }
            }
        }
    }

    public boolean isSilentSwapping()
    {
        return mc.player.getInventory().getSelectedSlot() != serverSlot;
    }

    public boolean startSwap(int itemSlot, SilentSwapType swapType)
    {
        PlayerInventory playerInventory = mc.player.getInventory();
        if (swapType == SilentSwapType.HOTBAR && !PlayerInventory.isValidHotbarIndex(itemSlot))
        {
            return false;
        }

        if (playerInventory.getSelectedSlot() == itemSlot)
        {
            return true;
        }

        int toSlot = itemSlot < PlayerInventory.getHotbarSize() ? itemSlot + playerInventory.getMainStacks().size() : itemSlot;
        int fromSlot = playerInventory.getSelectedSlot();
        synchronized (swapLock)
        {
            if (current.isSwapped())
            {
                return false;
            }

            switch (swapType)
            {
                case HOTBAR ->
                {
                    Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(itemSlot));

                    swapCache = new HotbarCache(playerInventory);
                    trackedSwaps.add(new SwapData(swapCache, itemSlot, fromSlot));
                }

                case INVENTORY -> mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                        toSlot, fromSlot, SlotActionType.SWAP, mc.player);
            }

            current.setSlotTo(itemSlot);
            current.setSlotFrom(fromSlot);
            current.setSwapType(swapType);
            current.setSwapped(true);
        }

        return true;
    }

    public void endSwap()
    {
        PlayerInventory playerInventory = mc.player.getInventory();
        synchronized (swapLock)
        {
            if (!current.isSwapped())
            {
                return;
            }

            int toSlot = current.getSlotTo() < PlayerInventory.getHotbarSize() ? current.getSlotTo() + playerInventory.getMainStacks().size() : current.getSlotTo();
            switch (current.getSwapType())
            {
                case HOTBAR ->
                {
                    if (isSilentSwapping())
                    {
                        Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(playerInventory.getSelectedSlot()));
                    }
                }

                case INVENTORY -> mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                        toSlot, current.getSlotFrom(), SlotActionType.SWAP, mc.player);
            }

            current.reset();
        }
    }
}
