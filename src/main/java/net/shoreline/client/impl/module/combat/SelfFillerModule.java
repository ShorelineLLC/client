package net.shoreline.client.impl.module.combat;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.combat.util.PhaseUtil;
import net.shoreline.client.impl.module.impl.ObsidianPlacerModule;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class SelfFillerModule extends ObsidianPlacerModule
{
    Config<BlockMode> blockMode = new EnumConfig.Builder<BlockMode>("Block")
            .setValues(BlockMode.values())
            .setDescription("The block to use for filling")
            .setDefaultValue(BlockMode.OBSIDIAN).build();
    Config<Boolean> autoDisable = new BooleanConfig.Builder("AutoDisable")
            .setDescription("Automatically disables after filling")
            .setDefaultValue(true).build();

    private double prevY;

    public SelfFillerModule()
    {
        super("SelfFiller", new String[] {"SelfFill", "BlockLag", "Burrow"}, "Fill the players feet", GuiCategory.COMBAT);
    }

    @Override
    public void onEnable()
    {
        if (!checkNull())
        {
            prevY = mc.player.getY();
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        double dy = Math.abs(mc.player.getY() - prevY);
        if (dy > 0.5 || !mc.player.isOnGround())
        {
            disable();
            return;
        }

        if (PhaseUtil.isInsideBlock(mc.player))
        {
            if (autoDisable.getValue())
            {
                disable();
            }

            return;
        }

        BlockPos blockPos = EntityUtil.getRoundedBlockPos(mc.player);
        if (blockMode.getValue() == BlockMode.WEB)
        {
            int slot = InventoryUtil.getHotbarItem(Items.COBWEB).getSlot();
            if (slot == -1)
            {
                return;
            }

            runSingleBlockPlacement(blockPos, Blocks.COBWEB, slot);

        } else
        {
            // 3arthqu4ke kys
            if (mc.isInSingleplayer())
            {
                fakePlace(blockPos, getCurrentObbyBlock().getDefaultState());
                if (autoDisable.getValue())
                {
                    disable();
                }

                return;
            }

            int slot = blockMode.getValue() == BlockMode.ENDER_CHEST ?
                    InventoryUtil.getHotbarItem(Items.ENDER_CHEST).getSlot() :
                    InventoryUtil.getHotbarItem(Items.OBSIDIAN).getSlot();

            if (slot == -1)
            {
                disable();
                return;
            }

            if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, 2.34, 0.0)))
            {
                disable();
                return;
            }

            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.42, z, true, false));
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.75, z, true, false));
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.01, z, true, false));
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.16, z, true, false));
            mc.player.setPosition(x, y + 1.16, z);

            if (blockMode.getValue() == BlockMode.ENDER_CHEST)
            {
                runSingleBlockPlacement(blockPos, Blocks.ENDER_CHEST, slot);
            } else
            {
                runSingleBlockPlacement(blockPos, Blocks.OBSIDIAN, slot);
            }

            mc.player.setPosition(x, y, z);
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 2.34, z, false, false));
        }

        if (autoDisable.getValue())
        {
            disable();
        }
    }

    public enum BlockMode
    {
        OBSIDIAN,
        ENDER_CHEST,
        WEB
    }
}
