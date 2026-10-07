package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.imixin.IClientPlayerInteractionManager;
import net.shoreline.client.impl.inventory.ItemSlot;
import net.shoreline.client.util.entity.PlayerUtil;
import net.shoreline.client.util.item.EnchantUtil;
import net.shoreline.client.util.item.ItemUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoToolModule extends Toggleable
{
    public static AutoToolModule INSTANCE;

    Config<Boolean> swapBack = new BooleanConfig.Builder("SwapBack")
            .setDescription("Swaps back to your previously held item")
            .setDefaultValue(false).build();

    private int prevSlot = -1;

    public AutoToolModule()
    {
        super("AutoTool", "Automatically switches to a tool before mining", GuiCategory.WORLD);
        INSTANCE = this;
    }

    @EventListener
    public void onTickPre(TickEvent.Pre event)
    {
        if (checkNull() || mc.interactionManager == null || !PlayerUtil.isInSurvival(mc.player))
        {
            return;
        }

        if (mc.interactionManager.isBreakingBlock())
        {
            ItemSlot blockSlot = getBestTool(((IClientPlayerInteractionManager) mc.interactionManager).getCurrentBreakingPos());
            int holding = mc.player.getInventory().selectedSlot;
            if (blockSlot != null && blockSlot.getSlot() != holding)
            {
                prevSlot = holding;
                mc.player.getInventory().setSelectedSlot(blockSlot.getSlot());
            }
        } else if (swapBack.getValue() && prevSlot != -1)
        {
            mc.player.getInventory().setSelectedSlot(prevSlot);
            prevSlot = -1;
        }
    }

    public ItemSlot getBestTool(BlockPos breakingPos)
    {
        final BlockState state = mc.world.getBlockState(breakingPos);
        if (state.isOf(Blocks.COBWEB))
        {
            for (int i = 0; i < 9; i++)
            {
                final ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty() || !ItemUtil.isSword(stack.getItem()))
                {
                    continue;
                }

                return new ItemSlot(i, stack);
            }
        }

        int slot = -1;
        ItemStack toolStack = null;

        float bestTool = 0.0f;
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !ItemUtil.isTool(stack.getItem()))
            {
                continue;
            }
            float speed = stack.getMiningSpeedMultiplier(state);
            final int efficiency = EnchantUtil.getLevel(Enchantments.EFFICIENCY, stack);
            if (efficiency > 0)
            {
                speed += efficiency * efficiency + 1.0f;
            }
            if (speed > bestTool)
            {
                bestTool = speed;
                toolStack = stack.copy();
                slot = i;
            }
        }

        if (slot == -1 || toolStack == null)
        {
            return null;
        }

        return new ItemSlot(slot, toolStack);
    }
}