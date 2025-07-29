package net.shoreline.client.impl.inventory;

import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.ac.Anticheat;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.gui.hud.RenderHotbarItemEvent;
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
    private final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    private final SwapData.Mutable current = new SwapData.Mutable();
    private final List<SwapData> trackedSwaps = new CopyOnWriteArrayList<>();
    private final Timer lastSwapTimer = new NanoTimer();

    private HotbarCache swapCache;
    private int serverSlot;

    public InventoryManager()
    {
        super("Inventory");
        EventBus.INSTANCE.subscribe(this);
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
                && anticheat.getAcModeConfig().getValue() == Anticheat.GRIM
                && !InventoryUtil.isInInventoryScreen())
        {
            int slot = packet.getSlot() > PlayerInventory.getHotbarSize() ? packet.getSlot() - PlayerInventory.MAIN_SIZE : packet.getSlot();
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

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet)
        {
            final int packetSlot = packet.getSelectedSlot();
            if (serverSlot == packetSlot)
            {
                event.cancel();
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
    public void onRenderHotbarItem(RenderHotbarItemEvent event)
    {
        if (!lastSwapTimer.hasPassed(500) && swapCache != null)
        {
            event.cancel();
            event.setStack(swapCache.getStack(event.getSeed()));
        }
    }

    public boolean isSilentSwapping()
    {
        return mc.player.getInventory().getSelectedSlot() != serverSlot;
    }

    public boolean startSwap(int itemSlot, SilentSwapType swapType)
    {
        PlayerInventory playerInventory = mc.player.getInventory();
        ScreenHandler handler = mc.player.currentScreenHandler;

        if (swapType == SilentSwapType.HOTBAR && !PlayerInventory.isValidHotbarIndex(itemSlot))
        {
            return false;
        }

        if (playerInventory.getSelectedSlot() == itemSlot)
        {
            return true;
        }

        swapCache = new HotbarCache(playerInventory);

        int fromSlot = playerInventory.getSelectedSlot();

        if (current.isSwapped())
        {
            return false;
        }

        switch (swapType)
        {
            case HOTBAR ->
            {
                Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(itemSlot));
                trackedSwaps.add(new SwapData(swapCache, itemSlot, fromSlot));
            }
            case INVENTORY ->
            {
                int toSlot = InventoryUtil.getPacketSlotIndex(itemSlot);
                mc.interactionManager.clickSlot(handler.syncId, toSlot, fromSlot, SlotActionType.SWAP, mc.player);
                lastSwapTimer.reset();
            }
        }

        current.setSlotTo(itemSlot);
        current.setSlotFrom(fromSlot);
        current.setSwapType(swapType);
        current.setSwapped(true);

        return true;
    }

    public void endSwap()
    {
        PlayerInventory playerInventory = mc.player.getInventory();
        ScreenHandler handler = mc.player.currentScreenHandler;

        if (!current.isSwapped())
        {
            return;
        }

        switch (current.getSwapType())
        {
            case HOTBAR ->
            {
                if (isSilentSwapping())
                {
                    Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(playerInventory.getSelectedSlot()));
                }
            }
            case INVENTORY ->
            {
                int toSlot = InventoryUtil.getPacketSlotIndex(current.getSlotTo());
                mc.interactionManager.clickSlot(handler.syncId, toSlot, current.getSlotFrom(), SlotActionType.SWAP, mc.player);
            }
        }

        current.reset();
    }

    public void clickSwap(int fromSlot, int toSlot, Item item)
    {
        ScreenHandler handler = mc.player.currentScreenHandler;

        int slot1 = InventoryUtil.getPacketSlotIndex(fromSlot);

        if (!handler.getCursorStack().getItem().equals(item))
        {
            mc.interactionManager.clickSlot(handler.syncId, slot1, 0, SlotActionType.PICKUP, mc.player);
        }

        if (handler.getCursorStack().getItem().equals(item))
        {
            int slot = InventoryUtil.getPacketSlotIndex(toSlot);
            mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        }

        if (!handler.getCursorStack().isEmpty())
        {
            mc.interactionManager.clickSlot(handler.syncId, slot1, 0, SlotActionType.PICKUP, mc.player);
        }
    }
}
