package net.shoreline.client.impl.manager.player;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.ItemDesyncEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.impl.module.combat.ReplenishModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorBundlePacket;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author xgraza & linus
 * @since 1.0
 */
public class InventoryManager implements Globals
{
    private final List<PreSwapData> swapData = new CopyOnWriteArrayList<>();

    // The serverside selected hotbar slot.
    private int slot;

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
            if (!PlayerInventory.isValidHotbarIndex(packetSlot) || slot == packetSlot)
            {
                event.setCanceled(true);
                return;
            }
            slot = packetSlot;
        }
    }

    @EventListener
    public void onPacketInbound(final PacketEvent.Inbound event)
    {
        if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket packet)
        {
            slot = packet.getSlot();
        }
        
        if (ReplenishModule.getInstance().isInInventoryScreen() || !AnticheatModule.getInstance().isGrim())
        {
            return;
        }

        // retarded packets from grim we can ignore
        if (event.getPacket() instanceof BundleS2CPacket packet)
        {
            List<Packet<?>> allowedBundle = new ArrayList<>();
            for (Packet<?> packet1 : packet.getPackets())
            {
                if (packet1 instanceof ScreenHandlerSlotUpdateS2CPacket)
                {
                    continue;
                }
                allowedBundle.add(packet1);
            }
            ((AccessorBundlePacket) packet).setIterable(allowedBundle);
        }

        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket packet)
        {
            int slot = packet.getSlot() - 36;
            if (slot < 0 || slot > 8)
            {
                return;
            }

            if (packet.getStack().isEmpty())
            {
                return;
            }

            for (PreSwapData data : swapData)
            {
                if (data.getSlot() != slot && data.getStarting() != slot)
                {
                    continue;
                }

                ItemStack preStack = data.getPreHolding(slot);
                if (!isEqual(preStack, packet.getStack()))
                {
                    event.cancel();
                    break;
                }
            }
        }
    }

    @EventListener
    public void onItemDesync(ItemDesyncEvent event)
    {
        if (isDesynced())
        {
            event.cancel();
            event.setStack(getServerItem());
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

    @EventListener
    public void onTick(TickEvent event)
    {
        swapData.removeIf(PreSwapData::isPassedClearTime);
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
        if (slot != barSlot && PlayerInventory.isValidHotbarIndex(barSlot))
        {
            setSlotForced(barSlot);

            final ItemStack[] hotbarCopy = new ItemStack[9];
            for (int i = 0; i < 9; i++)
            {
                hotbarCopy[i] = mc.player.getInventory().getStack(i);
            }
            swapData.add(new PreSwapData(hotbarCopy, slot, barSlot));
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
                    barSlot + 36, slot, SlotActionType.SWAP, mc.player);
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

            for (PreSwapData swapData : swapData)
            {
                swapData.beginClear();
            }
        }
    }

    public boolean isDesynced()
    {
        return mc.player.getInventory().selectedSlot != slot;
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
        return slot;
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

    private boolean isEqual(ItemStack stack1, ItemStack stack2)
    {
        return stack1.getItem().equals(stack2.getItem()) && stack1.getName().equals(stack2.getName());
    }

    public static class PreSwapData
    {
        private final ItemStack[] preHotbar;

        private final int starting;
        private final int swapTo;

        private Timer clearTime;

        public PreSwapData(ItemStack[] preHotbar, int start, int swapTo)
        {
            this.preHotbar = preHotbar;
            this.starting = start;
            this.swapTo = swapTo;
        }

        public void beginClear()
        {
            clearTime = new CacheTimer();
            clearTime.reset();
        }

        public boolean isPassedClearTime()
        {
            return clearTime != null && clearTime.passed(300);
        }

        public ItemStack getPreHolding(int i)
        {
            return preHotbar[i];
        }

        public int getStarting()
        {
            return starting;
        }

        public int getSlot()
        {
            return swapTo;
        }
    }
}