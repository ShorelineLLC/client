package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.component.DataComponentTypes;
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
public final class AutoAnvilRenameModule extends ToggleModule
{
    Config<Selection> autoRenameSelectionConfig = register(new EnumConfig<>("Selection", "The selection of items to rename", Selection.ALL, Selection.values()));
    Config<List<Item>> autoRenameWhitelistConfig = register(new ItemListConfig<>("Whitelist", "The items to rename.", Items.SHULKER_BOX, Items.WHITE_SHULKER_BOX,
            Items.LIGHT_GRAY_SHULKER_BOX, Items.GRAY_SHULKER_BOX, Items.BLACK_SHULKER_BOX, Items.BROWN_SHULKER_BOX, Items.RED_SHULKER_BOX, Items.ORANGE_SHULKER_BOX,
            Items.YELLOW_SHULKER_BOX, Items.LIME_SHULKER_BOX, Items.GREEN_SHULKER_BOX, Items.CYAN_SHULKER_BOX, Items.LIGHT_BLUE_SHULKER_BOX, Items.BLUE_SHULKER_BOX,
            Items.PURPLE_SHULKER_BOX, Items.PINK_SHULKER_BOX));
    Config<List<Item>> autoRenameBlacklistConfig = register(new ItemListConfig<>("Blacklist", "The items to not rename.", Items.EXPERIENCE_BOTTLE));
    Config<String> autoRenameTextConfig = register(new StringConfig("Text", "The text to rename the items to.", "ShorelineClient.net"));
    Config<Integer> autoRenameDelayConfig = register(new NumberConfig<>("Delay", "The delay between renaming items.", 0, 10, 20));
    Config<Boolean> debugConfig = register(new BooleanConfig("Debug", "Prints debug information to chat.", false, () -> false));

    private final CacheTimer delayTimer = new CacheTimer();

    public AutoAnvilRenameModule()
    {
        super("AutoAnvilRename", "Automatically renames items in anvils.", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onTick(final TickEvent event)
    {
        if (mc.player == null || mc.world == null || mc.interactionManager == null ||  !(mc.currentScreen instanceof AnvilScreen anvilScreen))
        {
            return;
        }
        if (!delayTimer.passed(autoRenameDelayConfig.getValue()))
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

    private void moveToEmptySlot(final AnvilScreenHandler screenHandler, final int slot)
    {
        if (mc.interactionManager == null)
        {
            return;
        }
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

    private boolean equalsName(final ItemStack itemStack, final String itemName)
    {
        if (itemName.trim().isEmpty())
        {
            return itemStack.get(DataComponentTypes.CUSTOM_NAME) == null;
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
