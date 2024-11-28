package net.shoreline.client.impl.manager.player;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.chat.ChatUtil;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xgraza & linus
 * @since 1.0
 */
public class InventoryManager implements Globals
{

    // The serverside selected hotbar slot.
    private int serverSlot;

    /**
     *
     */
    public InventoryManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onPacketOutBound(final PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket packet)
        {
            final int packetSlot = packet.getSelectedSlot();
            if (!PlayerInventory.isValidHotbarIndex(packetSlot) || serverSlot == packetSlot)
            {
                event.setCanceled(true);
                return;
            }
            serverSlot = packetSlot;
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket packet)
        {
            serverSlot = packet.getSlot();
        }
    }

    @EventListener
    public void onDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            syncToClient();
        }
    }

    /**
     * Sets the server slot via a {@link UpdateSelectedSlotC2SPacket}
     *
     * @param barSlot the player hotbar slot 0-8
     * @apiNote Method will not do anything if the slot provided is already the server slot
     * @see InventoryManager#setSlotForced(int)
     */
    public void setSlot(final int barSlot)
    {
        if (serverSlot != barSlot && PlayerInventory.isValidHotbarIndex(barSlot))
        {
            setSlotForced(barSlot);
        }
    }

    /**
     * Sets the server slot via a click slot
     *
     * @param barSlot the player hotbar slot 0-8
     */
    public void setSlotAlt(final int barSlot)
    {
        if (PlayerInventory.isValidHotbarIndex(barSlot))
        {
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                    barSlot + 36, serverSlot, SlotActionType.SWAP, mc.player);
        }
    }

    /**
     * Sets the server & client slot
     *
     * @param barSlot the player hotbar slot 0-8
     * @apiNote Method will not do anything if the slot provided is already the server slot
     * @see InventoryManager#setSlotForced(int)
     * @see InventoryManager#setSlot(int)
     */
    public void setClientSlot(final int barSlot)
    {
        if (mc.player.getInventory().selectedSlot != barSlot
                && PlayerInventory.isValidHotbarIndex(barSlot))
        {
            mc.player.getInventory().selectedSlot = barSlot;
            setSlotForced(barSlot);
        }
    }

    /**
     * Sends a {@link UpdateSelectedSlotC2SPacket} without any slot checks
     *
     * @param barSlot the player hotbar slot 0-8
     */
    public void setSlotForced(final int barSlot)
    {
        Managers.NETWORK.sendPacket(new UpdateSelectedSlotC2SPacket(barSlot));
    }

    /**
     * Syncs the server slot to the client slot
     */
    public void syncToClient()
    {
        if (isDesynced())
        {
            setSlotForced(mc.player.getInventory().selectedSlot);
            // send packet to sync inventory
//            if (!Managers.NETWORK.isCrystalPvpCC())
//            {
//                Managers.NETWORK.sendPacket(new ClickSlotC2SPacket(0, 0, findEmptySlot(), 0,
//                        SlotActionType.QUICK_CRAFT, ItemStack.EMPTY, new Int2ObjectOpenHashMap<>()));
//            }
        }
    }

    public boolean isDesynced()
    {
        return mc.player.getInventory().selectedSlot != serverSlot;
    }

    //
    public void closeScreen()
    {
        Managers.NETWORK.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
    }

    /**
     * @param slot
     */
    public int pickupSlot(final int slot)
    {
        return click(slot, 0, SlotActionType.PICKUP);
    }

    public void quickMove(final int slot)
    {
        click(slot, 0, SlotActionType.QUICK_MOVE);
    }

    /**
     * @param slot
     */
    public void throwSlot(final int slot)
    {
        click(slot, 0, SlotActionType.THROW);
    }

    public int findEmptySlot()
    {
        for (int i = 9; i < 36; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty())
            {
                return i;
            }
        }
        return -999; // throw
    }

    /**
     * @param slot
     * @param button
     * @param type
     */
    public int click(int slot, int button, SlotActionType type)
    {
        if (slot < 0)
        {
            return -1;
        }
        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        DefaultedList<Slot> defaultedList = screenHandler.slots;
        int i = defaultedList.size();
        ArrayList<ItemStack> list = Lists.newArrayListWithCapacity(i);
        for (Slot slot1 : defaultedList)
        {
            list.add(slot1.getStack().copy());
        }
        screenHandler.onSlotClick(slot, button, type, mc.player);
        Int2ObjectOpenHashMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
        for (int j = 0; j < i; ++j)
        {
            ItemStack itemStack2;
            ItemStack itemStack = list.get(j);
            if (ItemStack.areEqual(itemStack, itemStack2 = defaultedList.get(j).getStack())) continue;
            int2ObjectMap.put(j, itemStack2.copy());
        }
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(screenHandler.syncId, screenHandler.getRevision(), slot, button, type, screenHandler.getCursorStack().copy(), int2ObjectMap));
        return screenHandler.getRevision();
    }

    public int click2(int slot, int button, SlotActionType type)
    {
        if (slot < 0)
        {
            return -1;
        }
        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        DefaultedList<Slot> defaultedList = screenHandler.slots;
        int i = defaultedList.size();
        ArrayList<ItemStack> list = Lists.newArrayListWithCapacity(i);
        for (Slot slot1 : defaultedList)
        {
            list.add(slot1.getStack().copy());
        }
        // screenHandler.onSlotClick(slot, button, type, mc.player);
        Int2ObjectOpenHashMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();
        for (int j = 0; j < i; ++j)
        {
            ItemStack itemStack2;
            ItemStack itemStack = list.get(j);
            if (ItemStack.areEqual(itemStack, itemStack2 = defaultedList.get(j).getStack())) continue;
            int2ObjectMap.put(j, itemStack2.copy());
        }
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(screenHandler.syncId, screenHandler.getRevision(), slot, button, type, screenHandler.getCursorStack().copy(), int2ObjectMap));
        return screenHandler.getRevision();
    }

    /**
     * @return
     */
    public int getServerSlot()
    {
        return serverSlot;
    }

    public int getClientSlot()
    {
        return mc.player.getInventory().selectedSlot;
    }

    /**
     * @return
     */
    public ItemStack getServerItem()
    {
        if (mc.player != null && getServerSlot() != -1)
        {
            return mc.player.getInventory().getStack(getServerSlot());
        }
        return null;
    }
}
