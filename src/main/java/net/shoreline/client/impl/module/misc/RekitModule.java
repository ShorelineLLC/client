package net.shoreline.client.impl.module.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.StringConfig;
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
import java.util.Map;

public class RekitModule extends Toggleable
{
    private static RekitModule INSTANCE;
    Config<String> kit = new StringConfig.Builder("Kit")
            .setDescription("The kit to use")
            .setDefaultValue("").build();

    private final Map<Integer, Item> mismatchMap = new HashMap<>();
    private final Timer timer = new NanoTimer();
    private int iterations;

    public RekitModule()
    {
        super("Rekit", "Allows you to save your inventory to rekit later", GuiCategory.MISCELLANEOUS);
        INSTANCE = this;
    }

    public static RekitModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
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
            Shoreline.info("No valid kit found.");
            return;
        }

        mismatchMap.clear();
        for (int i = 0; i < 45; ++i)
        {
            Item item    = mc.player.getInventory().getStack(i).getItem();
            Item kitItem = first.getStack(i);

            if (!item.getName().getString().equalsIgnoreCase(kitItem.getName().getString()) && !kitItem.equals(Items.AIR))
            {
                mismatchMap.put(i, kitItem);
                Shoreline.info("Mismatch found: " + i + " - " + kitItem.getName().getString());
            }
        }
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull() || mismatchMap.isEmpty() || !timer.hasPassed(500))
        {
            return;
        }

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler containerScreen)
        {
            for (int i = 0; i < containerScreen.getInventory().size(); i++)
            {
                Item item = containerScreen.getInventory().getStack(i).getItem();
                int mismatchedSlot = findMismatchedSlot(item);
                if (mismatchedSlot == -1)
                {
                    continue;
                }


                mc.interactionManager.clickSlot(containerScreen.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(containerScreen.syncId, mismatchedSlot + containerScreen.getInventory().size(), 0, SlotActionType.PICKUP, mc.player);
                mismatchMap.remove(mismatchedSlot);
                timer.reset();
            }
        }
    }

    public int findMismatchedSlot(Item item)
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
}