package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.*;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

/**
 * @author hockeyl8
 * @since 1.0
 */
public class BetterAnvilModule extends ToggleModule
{
    Config<Boolean> autoRenameConfig = register(new BooleanConfig("AutoRename", "Automatically renames items.", true));
    Config<Selection> autoRenameSelectionConfig = register(new EnumConfig<>("Selection", "The selection of items to rename", Selection.ALL, Selection.values(), () -> autoRenameConfig.getValue()));
    Config<List<Item>> autoRenameWhitelistConfig = register(new ItemListConfig<>("Whitelist", "The items to rename.", Items.SHULKER_BOX));
    Config<List<Item>> autoRenameBlacklistConfig = register(new ItemListConfig<>("Blacklist", "The items to not rename.", Items.EXPERIENCE_BOTTLE));
    Config<String> autoRenameTextConfig = register(new StringConfig("Text", "The text to rename the items to.", "ShorelineClient.net", () -> autoRenameConfig.getValue()));
    Config<Integer> autoRenameDelayConfig = register(new NumberConfig<>("Delay", "The delay between renaming items.", 0, 10, 20, () -> autoRenameConfig.getValue()));
    Config<Boolean> debugConfig = register(new BooleanConfig("Debug", "Prints debug information to chat.", false, () -> false));

    private final CacheTimer delayTimer = new CacheTimer();

    public BetterAnvilModule()
    {
        super("BetterAnvil", "Makes anvils better.", ModuleCategory.MISCELLANEOUS);
    }

    @Override
    public void onDisable()
    {
        delayTimer.reset();
    }

    @EventListener
    public void onTick(final TickEvent event)
    {
        if (mc.player == null || mc.world == null || mc.interactionManager == null ||  !(mc.currentScreen instanceof AnvilScreen anvilScreen))
        {
            return;
        }
        if (!autoRenameConfig.getValue() && !delayTimer.passed(autoRenameDelayConfig.getValue()))
        {
            return;
        }
        if (mc.player.experienceLevel <= 0 && !mc.player.isCreative())
        {
            if (debugConfig.getValue())
            {
                sendModuleMessage("Not enough experience levels!");
            }
            return;
        }
        final AnvilScreenHandler screenHandler = anvilScreen.getScreenHandler();
        if (!screenHandler.getSlot(1).getStack().isEmpty())
        {
            moveToEmptySlot(screenHandler, 1);
            return;
        }
        if (!screenHandler.getSlot(0).getStack().isEmpty())
        {
            moveToEmptySlot(screenHandler, 0);
            return;
        }
        if (!screenHandler.getSlot(2).getStack().isEmpty())
        {
            moveToEmptySlot(screenHandler, 2);
            return;
        }
        for (int i = 3; i < 36 + 3; i++)
        {
            final ItemStack itemStack = screenHandler.getSlot(i).getStack();
            if (!itemStack.isEmpty() && !equalsName(itemStack, autoRenameTextConfig.getName()))
            {
                if (autoRenameSelectionConfig.getValue() == Selection.BLACKLIST && autoRenameBlacklistConfig.getValue().contains(itemStack.getItem())
                        || autoRenameSelectionConfig.getValue() == Selection.WHITELIST && !autoRenameWhitelistConfig.getValue().contains(itemStack.getItem()))
                {
                    continue;
                }

                final String name = (!autoRenameTextConfig.getValue().trim().isEmpty() ? autoRenameTextConfig.getValue() : "");
                mc.interactionManager.clickSlot(screenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(screenHandler.syncId, screenHandler.getSlot(0).id, 0, SlotActionType.PICKUP, mc.player);
                ((AnvilScreen) mc.currentScreen).nameField.setText(name);
                if (debugConfig.getValue())
                {
                    sendModuleMessage("Successfully renamed item in slot: " + i + ".");
                }
                mc.interactionManager.clickSlot(screenHandler.syncId, screenHandler.getSlot(2).id, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(screenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                break;
            }
        }
        delayTimer.reset();
    }

    private void moveToEmptySlot(AnvilScreenHandler screenHandler, int slot) {
        for (int i = 3; i < 36 + 3; i++)
        {
            final ItemStack itemStack = screenHandler.getSlot(i).getStack();
            if (itemStack.isEmpty())
            {
                mc.interactionManager.clickSlot(screenHandler.syncId, screenHandler.getSlot(slot).id, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(screenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
        mc.interactionManager.clickSlot(screenHandler.syncId, screenHandler.getSlot(slot).id, 0, SlotActionType.THROW, mc.player);
    }

    private boolean equalsName(ItemStack itemStack, String itemName)
    {
        if (itemName.trim().isEmpty())
        {
            return !itemStack.hasCustomName();
        }
        else
        {
            return itemStack.getName().getString().equals(itemName);
        }
    }

    private enum Selection
    {
        ALL,
        WHITELIST,
        BLACKLIST
    }
}
