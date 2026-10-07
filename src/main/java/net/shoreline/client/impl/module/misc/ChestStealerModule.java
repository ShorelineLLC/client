package net.shoreline.client.impl.module.misc;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.combat.PvpKit;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

public class ChestStealerModule extends Toggleable
{
    private static ChestStealerModule INSTANCE;
    Config<String> kit = new StringConfig.Builder("Kit")
            .setDescription("The kit to use")
            .setDefaultValue("").build();
    Config<RekitMode> mode = new EnumConfig.Builder<RekitMode>("Mode")
            .setValues(RekitMode.values())
            .setDefaultValue(RekitMode.BUTTON).build();
    Config<Integer> mismatchDelay = new NumberConfig.Builder<Integer>("MismatchDelay")
            .setMin(0).setDefaultValue(500).setMax(1000)
            .setDescription("The delay between updating mismatches")
            .setVisible(() -> mode.getValue() == RekitMode.AUTO)
            .build();
    Config<Integer> clickDelay = new NumberConfig.Builder<Integer>("ClickDelay")
            .setMin(0).setDefaultValue(100).setMax(1000)
            .setDescription("The click delay between 2 different clicks")
            .build();
    Config<Boolean> doubleClicks = new BooleanConfig.Builder("Double-Clicks")
            .setDescription("If we should double click.")
            .setDefaultValue(false).build();
    Config<Integer> sequenceDelay = new NumberConfig.Builder<Integer>("SequenceDelay")
            .setMin(0).setDefaultValue(50).setMax(1000)
            .setVisible(() -> !doubleClicks.getValue())
            .setDescription("The click delay between 2 sequenced clicks.").build();

    private final Map<Integer, Item> mismatchMap = new HashMap<>();
    private final Queue<Click> clicks = new LinkedList<>();
    private final Timer clickTimer = new NanoTimer();
    private final Timer sequenceTimer = new NanoTimer();
    private final Timer mismatchTimer = new NanoTimer();

    private int sequenced;

    public ChestStealerModule()
    {
        super("ChestStealer", "Allows you to take items out of containers", GuiCategory.MISCELLANEOUS);
        INSTANCE = this;
    }

    public static ChestStealerModule getInstance()
    {
        return INSTANCE;
    }

    public void reset()
    {
        clicks.clear();
        sequenced = -1;
        mismatchMap.clear();
        clickTimer.reset();
        sequenceTimer.reset();
        mismatchTimer.reset();
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            reset();
            return;
        }

        runClicks();
        if (mc.player.currentScreenHandler != null && (!mismatchMap.isEmpty() || mode.getValue() == RekitMode.AUTO))
        {
            int size = -1;
            Function<Integer, Item> getter = null;
            if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler containerScreen)
            {
                size = containerScreen.getInventory().size();
                getter = i -> containerScreen.getInventory().getStack(i).getItem();
            }
            else if (mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler shulkerBoxScreenHandler)
            {
                size = 27;
                getter = i -> shulkerBoxScreenHandler.getSlot(i).getStack().getItem();
            }

            if (size == -1)
            {
                reset();
                return;
            }

            if (mode.getValue() == RekitMode.AUTO && (mismatchMap.isEmpty() || mismatchTimer.hasPassed(mismatchDelay.getValue())))
            {
                reset();
                updateMismatch();
            }

            for (int i = 0; i < size; i++)
            {
                Item item = getter.apply(i);
                int mismatchedSlot = findMismatchedSlot(item, -1);
                if (mismatchedSlot == -1)
                {
                    continue;
                }

                ScreenHandler handler = mc.player.currentScreenHandler;
                int slot = InventoryUtil.getPacketSlotIndex(handler, i);
                if (handler.getCursorStack().getItem().equals(item))
                {
                    addClick(mismatchedSlot + size, -1);
                }
                else
                {
                    addClick(slot, mismatchedSlot + size);
                }

                mismatchMap.remove(mismatchedSlot);
            }
        }
    }

    public void addClick(int slot, int to)
    {
        Click click = new Click(slot, to);
        click.setDoubleClick(doubleClicks.getValue());
        clicks.add(click);
    }

    public void runClicks()
    {
        int syncId = mc.player.currentScreenHandler.syncId;
        if (sequenced != -1 && sequenceTimer.hasPassed(sequenceDelay.getValue()))
        {
            mc.interactionManager.clickSlot(syncId, sequenced, 0, SlotActionType.PICKUP, mc.player);
            sequenced = -1;
            return;
        }

        if (clickTimer.hasPassed(clickDelay.getValue()) && sequenced == -1)
        {
            Click click = clicks.poll();
            if (click != null)
            {
                click.runClick();
            }
        }
    }

    public void updateMismatch()
    {
        PvpKit first = null;
        for (PvpKit kit : Managers.KIT.getKits())
        {
            if (first == null)
            {
                first = kit;
            }

            if (kit.getName().equalsIgnoreCase(this.kit.getName()))
            {
                first = kit;
                break;
            }
        }

        if (first == null)
        {
            return;
        }

        mismatchMap.clear();
        for (int i = 0; i < 45; ++i)
        {
            int actualIndex = convertIndex(i);
            Item currentItem = mc.player.getInventory().getStack(i).getItem();
            Item kitItem = first.getStack(i);

            if (!currentItem.equals(kitItem) && !kitItem.equals(Items.AIR))
            {
                mismatchMap.put(actualIndex, kitItem);
            }
        }

        mismatchTimer.reset();
    }

    public int findMismatchedSlot(Item item, int blacklist)
    {
        for (Map.Entry<Integer, Item> entry : mismatchMap.entrySet())
        {
            if (entry.getValue().equals(item))
            {
                return entry.getKey();
            }
        }

        return -1;
    }

    public int convertIndex(int visualIndex)
    {
        if (visualIndex >= 0 && visualIndex <= 8)
        {
            return visualIndex + 27;
        }
        else if (visualIndex >= 9 && visualIndex <= 17
                || visualIndex >= 18 && visualIndex <= 26
                || visualIndex >= 27 && visualIndex <= 35)
        {
            return visualIndex - 9;
        }
        else
        {
            return visualIndex;
        }
    }

    public boolean isValidHandler(ScreenHandler handler)
    {
        if (mode.getValue() != RekitMode.BUTTON)
        {
            return false;
        }

        return handler instanceof GenericContainerScreenHandler || handler instanceof ShulkerBoxScreenHandler;
    }

    @Getter
    @Setter
    private class Click
    {
        private final int click;
        private final int sequence;
        private boolean doubleClick;

        public Click(int click, int sequence)
        {
            this.click = click;
            this.sequence = sequence;
            this.doubleClick = false;
        }

        public void runClick()
        {
            int syncId = mc.player.currentScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId, click, 0, SlotActionType.PICKUP, mc.player);
            if (sequence != -1)
            {
                if (doubleClicks.getValue())
                {
                    mc.interactionManager.clickSlot(syncId, sequence, 0, SlotActionType.PICKUP, mc.player);
                    return;
                }

                sequenced = sequence;
                sequenceTimer.reset();
            }

            clickTimer.reset();
        }
    }

    public enum RekitMode
    {
        BUTTON,
        AUTO
    }
}