package net.shoreline.client.impl.module.world;

import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.ItemUseEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorMinecraftClient;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author xgraza, linus
 * @since 1.0
 */
public final class AirPlaceModule extends ToggleModule
{
    private static AirPlaceModule INSTANCE;

    Config<Boolean> manualConfig = register(new BooleanConfig("Click", "Allow manual air place", true));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Place on air on grim", false));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to air place", 1.0f, 4.0f, 6.0f, NumberDisplay.DEFAULT));
    Config<Boolean> fluidsConfig = register(new BooleanConfig("Fluids", "Place against fluids", false));

    private int airPlaceTicks;

    public AirPlaceModule()
    {
        super("AirPlace", "Allows you to place blocks in the air", ModuleCategory.WORLD);
        INSTANCE = this;
    }

    public static AirPlaceModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        airPlaceTicks = 0;
    }

    @EventListener
    public void onPlayerTick(final TickEvent event)
    {
        if (airPlaceTicks > 0)
        {
            airPlaceTicks--;
        }

        if (mc.player == null || mc.interactionManager == null || !manualConfig.getValue()
                || event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }

        if (mc.crosshairTarget instanceof BlockHitResult result && !mc.world.isAir(result.getBlockPos()))
        {
            return;
        }

        final ItemStack stack = mc.player.getMainHandStack();
        if ((stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) || !mc.options.useKey.isPressed())
        {
            return;
        }
        final HitResult result = mc.player.raycast(rangeConfig.getValue(), 1.0f, fluidsConfig.getValue());
        if (((AccessorMinecraftClient) mc).hookGetItemUseCooldown() == 0 && airPlaceTicks == 0 && !mc.player.isUsingItem()
                && result instanceof BlockHitResult blockHitResult)
        {
            final BlockPos blockPos = BlockPos.ofFloored(blockHitResult.getPos());
            if (!mc.world.isAir(blockPos) || isEntityInBlockPos(blockPos))
            {
                return;
            }
            ((AccessorMinecraftClient) mc).hookSetItemUseCooldown(4);
            airPlaceTicks = 4;
            if (grimConfig.getValue())
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, blockHitResult);
                mc.player.swingHand(Hand.MAIN_HAND, false);
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.OFF_HAND));
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            }
            else
            {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    @EventListener
    public void onItemUse(final ItemUseEvent event)
    {
        if (airPlaceTicks > 0 && manualConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderWorld(final RenderWorldEvent event)
    {
        if (mc.player == null || !manualConfig.getValue())
        {
            return;
        }

        if (mc.crosshairTarget instanceof BlockHitResult result && !mc.world.isAir(result.getBlockPos()))
        {
            return;
        }

        final ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem))
        {
            return;
        }
        final HitResult result = mc.player.raycast(rangeConfig.getValue(), 1.0f, fluidsConfig.getValue());
        if (!(result instanceof BlockHitResult blockHitResult))
        {
            return;
        }
        final BlockPos blockPos = BlockPos.ofFloored(blockHitResult.getPos());
        if (!mc.world.isAir(blockPos) || isEntityInBlockPos(blockPos))
        {
            return;
        }
        RenderBuffers.preRender();
        RenderManager.renderBoundingBox(event.getMatrices(), blockPos, 1.5f, ColorsModule.getInstance().getRGB(145));
        RenderBuffers.postRender();
    }

    private boolean isEntityInBlockPos(final BlockPos blockPos)
    {
        for (Entity entity : mc.world.getEntities())
        {
            if (entity.getBoundingBox().intersects(new Box(blockPos)))
            {
                return true;
            }
        }
        return false;
    }
}
