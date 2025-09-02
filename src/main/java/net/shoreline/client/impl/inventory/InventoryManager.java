package net.shoreline.client.impl.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.entity.player.InsertStackEvent;
import net.shoreline.client.impl.event.item.ItemUseEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.SetHandEvent;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class InventoryManager extends GenericFeature
{
    private final AnticheatModule anticheat = AnticheatModule.INSTANCE;

    private final SwapData.Mutable current = new SwapData.Mutable();
    private final SwapData.Mutable multitick = new SwapData.Mutable();

    private final List<SwapData> trackedHotbar = new CopyOnWriteArrayList<>();
    private final List<SwapData> trackedInventory = new CopyOnWriteArrayList<>();

    private boolean usingItem;
    private int serverSlot;

    public InventoryManager()
    {
        super("Inventory");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        trackedInventory.clear();
        trackedHotbar.clear();
    }

    @EventListener
    public void onDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            trackedInventory.clear();
            trackedHotbar.clear();
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

        if (event.getPacket() instanceof PlayerActionC2SPacket packet &&
                packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && multitick.isSwapped() && usingItem)
        {
            int slot = multitick.getSlotTo();
            if (serverSlot != slot)
            {
                Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            }

            usingItem = false;
        }

        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket packet && !InventoryUtil.isInInventoryScreen())
        {
            int slot = packet.getSlot();
            if (packet.getStack().isEmpty() || !PlayerInventory.isValidHotbarIndex(slot))
            {
                return;
            }

            for (SwapData data : trackedHotbar)
            {
                if (data.getSlotTo() != slot && data.getSlotFrom() != slot)
                {
                    continue;
                }

                ItemStack preStack = data.getPreHotbar().getStack(slot);
                if (!ItemStack.areItemsEqual(preStack, packet.getStack()))
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
    public void onSetHand(SetHandEvent event)
    {
        if (multitick.isSwapped() && isSilentSwapping())
        {
            Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            usingItem = true;
        }
    }

    @EventListener
    public void onItemUse(ItemUseEvent event)
    {
        if (mc.player != null && current.isSwapped())
        {
            event.cancel();
            event.setItemStack(current.getItemStack(mc.player.getInventory()));
        }
    }

    @EventListener
    public void onInsertStack(InsertStackEvent event)
    {
        if (mc.isInSingleplayer() || trackedInventory.isEmpty() || !PlayerInventory.isValidHotbarIndex(event.getSlot()))
        {
            return;
        }

        for (SwapData data : trackedInventory)
        {
            if (data.getSwapTime() > 500L)
            {
                trackedInventory.remove(data);
                continue;
            }

            if (data.getSlotTo() == event.getSlot() || data.getSlotFrom() == event.getSlot())
            {
                event.cancel();
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

        if (playerInventory.selectedSlot == itemSlot || current.isSwapped())
        {
            return true;
        }

        HotbarCache swapCache = new HotbarCache(playerInventory);

        int fromSlot = multitick.isSwapped() && !mc.player.isUsingItem() ? multitick.getSlotTo() : playerInventory.selectedSlot;
        current.setSwapped(true);
        current.setSlotTo(itemSlot);
        current.setSlotFrom(fromSlot);

        SwapData data = new SwapData(swapCache, itemSlot, fromSlot);
        switch (swapType)
        {
            case HOTBAR ->
            {
                Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(itemSlot));
                trackedHotbar.add(data);
            }
            case INVENTORY ->
            {
                internalSwapSlot(itemSlot, fromSlot);
                trackedInventory.add(data);
            }
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
                    int returnSlot = multitick.isSwapped() && !mc.player.isUsingItem() ? multitick.getSlotTo() : playerInventory.selectedSlot;
                    Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(returnSlot));
                }
            }

            case INVENTORY -> internalSwapSlot(current.getSlotTo(), current.getSlotFrom());
        }

        current.reset();
    }

    public boolean startMultitickSwap(int itemSlot)
    {
        if (current.isSwapped() || usingItem || !PlayerInventory.isValidHotbarIndex(itemSlot))
        {
            return false;
        }

        multitick.setSwapped(true);
        multitick.setSlotTo(itemSlot);
        Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(itemSlot));
        return true;
    }

    public void endMultitickSwap()
    {
        if (!multitick.isSwapped())
        {
            return;
        }

        usingItem = false;
        if (isSilentSwapping())
        {
            Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }

        multitick.reset();
    }

    public void clickSwap(int fromSlot, int toSlot, Item item)
    {
        ScreenHandler handler = mc.player.currentScreenHandler;
        int slot = InventoryUtil.getPacketSlotIndex(handler, fromSlot);

        if (!handler.getCursorStack().getItem().equals(item))
        {
            mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        }

        if (handler.getCursorStack().getItem().equals(item))
        {
            mc.interactionManager.clickSlot(handler.syncId, toSlot, 0, SlotActionType.PICKUP, mc.player);
        }

        if (!handler.getCursorStack().isEmpty())
        {
            mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    private void internalSwapSlot(int slot1, int slot2)
    {
        ScreenHandler screenHandler = mc.player.playerScreenHandler;

        ItemStack stack1 = screenHandler.slots.get(slot1).getStack();
        ItemStack stack2 = screenHandler.slots.get(slot2).getStack();

        final Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
        int2ObjectMap.put(slot1, stack2.copy());
        int2ObjectMap.put(slot2, stack1.copy());

        int slot = InventoryUtil.getPacketSlotIndex(screenHandler, slot1);
        Managers.NETWORK.sendPacket(new ClickSlotC2SPacket(screenHandler.syncId,
                screenHandler.getRevision(),
                slot,
                slot2,
                SlotActionType.SWAP,
                screenHandler.getCursorStack().copy(),
                int2ObjectMap));
    }

    public boolean isHolding(Item item, Hand hand)
    {
        return getServerStack().getItem().equals(item) || mc.player.getStackInHand(hand).getItem().equals(item);
    }

    public ItemStack getServerStack()
    {
        return mc.player.getInventory().getStack(serverSlot);
    }
}