package net.shoreline.client.impl.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.gui.hud.RenderHotbarItemEvent;
import net.shoreline.client.impl.event.item.ItemUseEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.util.item.ItemUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class InventoryManager extends GenericFeature
{
    private final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    private final SwapData.Mutable current = new SwapData.Mutable();
    private final List<SwapData> trackedSwaps = new CopyOnWriteArrayList<>();

    private int serverSlot;

    private final Lock swapLock = new ReentrantLock();

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
    public void onDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            trackedSwaps.clear();
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket(int slot))
        {
            serverSlot = slot;
        }

        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket packet
                && packet.getSyncId() == mc.player.currentScreenHandler.syncId
                && !InventoryUtil.isInInventoryScreen())
        {
            int slot = InventoryUtil.getPacketSlotIndex(packet.getSlot());
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
        if (mc.player != null && current.isSwapped())
        {
            event.cancel();
            event.setItemStack(mc.player.getInventory().getStack(serverSlot));
        }
    }

    @EventListener
    public void onRenderHotbarItem(RenderHotbarItemEvent event)
    {
        for (SwapData data : trackedSwaps)
        {
            if (data.getSwapTime() > 500L)
            {
                trackedSwaps.remove(data);
                continue;
            }

            if (data.getSlotTo() == event.getSeed() || data.getSlotFrom() == event.getSeed())
            {
                event.cancel();
                event.setStack(data.getPreHotbar().getStack(event.getSeed()));
                return;
            }
        }
    }

    public boolean isSilentSwapping()
    {
        return mc.player.getInventory().selectedSlot != serverSlot;
    }

    public boolean startSwap(int itemSlot)
    {
        return startSwap(itemSlot, anticheat.getSilentSwapType());
    }

    public boolean startSwap(int itemSlot, SilentSwapType swapType)
    {
        PlayerInventory playerInventory = mc.player.getInventory();
        if (swapType == SilentSwapType.HOTBAR && !PlayerInventory.isValidHotbarIndex(itemSlot))
        {
            return false;
        }

        if (playerInventory.selectedSlot == itemSlot)
        {
            return true;
        }

        HotbarCache swapCache = new HotbarCache(playerInventory);

        int fromSlot = playerInventory.selectedSlot;

        swapLock.lock();
        try
        {
            if (current.isSwapped())
            {
                return false;
            }

            current.setSlotTo(itemSlot);
            current.setSlotFrom(fromSlot);
            current.setSwapped(true);

            switch (swapType)
            {
                case HOTBAR -> Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(itemSlot));
                case INVENTORY ->
                {
                    internalSwapSlot(itemSlot, fromSlot);
                    serverSlot = itemSlot;
                }
            }

            trackedSwaps.add(new SwapData(swapCache, itemSlot, fromSlot));
        }
        finally
        {
            swapLock.unlock();
        }

        return true;
    }

    public void endSwap()
    {
        endSwap(anticheat.getSilentSwapType());
    }

    public void endSwap(SilentSwapType swapType)
    {
        PlayerInventory playerInventory = mc.player.getInventory();

        swapLock.lock();
        try
        {
            if (!current.isSwapped())
            {
                return;
            }

            switch (swapType)
            {
                case HOTBAR ->
                {
                    if (isSilentSwapping())
                    {
                        Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(playerInventory.selectedSlot));
                    }
                }
                case INVENTORY ->
                {
                    internalSwapSlot(current.getSlotTo(), current.getSlotFrom());
                    serverSlot = playerInventory.selectedSlot;
                }
            }

            current.reset();
        }
        finally
        {
            swapLock.unlock();
        }
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

    private void internalSwapSlot(int slot1, int slot2)
    {
        ScreenHandler screenHandler = mc.player.currentScreenHandler;

        ItemStack stack1 = screenHandler.slots.get(slot1).getStack();
        ItemStack stack2 = screenHandler.slots.get(slot2).getStack();

        final Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
        int2ObjectMap.put(slot1, stack2.copy());
        int2ObjectMap.put(slot2, stack1.copy());

        int slot = InventoryUtil.getPacketSlotIndex(slot1);
        Managers.NETWORK.sendPacket(new ClickSlotC2SPacket(screenHandler.syncId,
                screenHandler.getRevision(),
                slot,
                slot2,
                SlotActionType.SWAP,
                screenHandler.getCursorStack().copy(),
                int2ObjectMap));
    }
}