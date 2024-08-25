package net.shoreline.client.impl.module;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;

import java.util.function.Predicate;

public class BlockPlacerModule extends CombatModule
{

    protected Config<Boolean> strictDirectionConfig = register(new BooleanConfig("StrictDirection", "Places on visible sides only", false));
    protected Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Places using grim instant rotations", false));

    public BlockPlacerModule(String name, String desc, ModuleCategory category)
    {
        super(name, desc, category);
        register(strictDirectionConfig, grimConfig);
    }

    public BlockPlacerModule(String name, String desc, ModuleCategory category, int rotationPriority)
    {
        super(name, desc, category, rotationPriority);
        register(strictDirectionConfig, grimConfig);
    }

    protected int getSlot(final Predicate<ItemStack> filter)
    {
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && filter.test(itemStack))
            {
                return i;
            }
        }
        return -1;
    }

    protected int getBlockItemSlot(final Block block)
    {
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() == block)
            {
                return i;
            }
        }
        return -1;
    }
}
