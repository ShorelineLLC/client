package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.module.RotationModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.combat.AutoCrystalModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorClientPlayerInteractionManager;
import net.shoreline.client.util.player.EnchantmentUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.eventbus.event.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * @author linus
 * @since 1.0
 */
public class SpeedmineModule extends RotationModule
{
    public static SpeedmineModule INSTANCE;

    Config<SpeedmineMode> modeConfig = register(new EnumConfig<>("Mode", "The mining mode for speedmine", SpeedmineMode.PACKET, SpeedmineMode.values()));
    Config<Float> mineSpeedConfig = register(new NumberConfig<>("Speed", "The speed to mine blocks", 0.0f, 0.7f, 0.9f, () -> modeConfig.getValue() == SpeedmineMode.DAMAGE));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "Range for mine", 1.0f, 4.5f, 6.0f, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Swap> swapConfig = register(new EnumConfig<>("AutoSwap", "Swaps to the best tool once the mining is complete", Swap.SILENT, Swap.values(), () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotates when mining the block", true, () -> modeConfig.getValue() == SpeedmineMode.PACKET));
    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Uses grim block breaking speeds", false));
    private BlockPos mining;
    private BlockState state;
    private Direction direction;
    private float damage;
    private float lastDamage;

    public SpeedmineModule()
    {
        super("Speedmine", "Mines faster", ModuleCategory.WORLD, 900);
        INSTANCE = this;
    }

    public static SpeedmineModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public String getModuleData()
    {
        DecimalFormat decimal = new DecimalFormat("0.0");
        return decimal.format(damage);
    }

    @Override
    public void onDisable()
    {
        if (mining != null)
        {
            Managers.INVENTORY.syncToClient();
        }
        mining = null;
        state = null;
        direction = null;
        damage = 0.0f;
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE || modeConfig.getValue() != SpeedmineMode.DAMAGE)
        {
            return;
        }
        AccessorClientPlayerInteractionManager interactionManager =
                (AccessorClientPlayerInteractionManager) mc.interactionManager;
        if (interactionManager.hookGetCurrentBreakingProgress() >= mineSpeedConfig.getValue())
        {
            interactionManager.hookSetCurrentBreakingProgress(1.0f);
        }
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (modeConfig.getValue() != SpeedmineMode.PACKET || mc.player.isCreative())
        {
            return;
        }
        if (mining == null)
        {
            damage = 0.0f;
            return;
        }
        state = mc.world.getBlockState(mining);
        int slot = AutoToolModule.getInstance().getBestTool(state);
        double dist = mc.player.squaredDistanceTo(mining.toCenterPos());
        if (dist > ((NumberConfig<?>) rangeConfig).getValueSq()
                || state.isAir() || damage > 3.0f)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, mining, Direction.DOWN));
            mining = null;
            state = null;
            direction = null;
            damage = 0.0f;
        }
        else if (damage >= 1.0f && !AutoCrystalModule.getInstance().isAttacking()
                && !AutoCrystalModule.getInstance().isPlacing() && !mc.player.isUsingItem())
        {
            if (isRotationBlocked())
            {
                return;
            }
            if (swapConfig.getValue() != Swap.OFF)
            {
                swapTo(slot);
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mining, direction));
                swapSync(slot);
            }
            damage = 0.0f;
            mining = null;
            state = null;
            direction = null;
        }
        else
        {
            float delta = calcBlockBreakingDelta(state, mc.world, mining);
            lastDamage = damage;
            damage += delta;
            if (delta + damage >= 1.0f && rotateConfig.getValue()
                    && !AutoCrystalModule.getInstance().isAttacking()
                    && !AutoCrystalModule.getInstance().isPlacing())
            {
                float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), mining.toCenterPos());
                setRotation(rotations[0], rotations[1]);
            }
        }
    }

    /**
     * @param event
     */
    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        //
        if (event.getPacket() instanceof PlayerActionC2SPacket packet
                && packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                && modeConfig.getValue() == SpeedmineMode.DAMAGE && grimConfig.getValue())
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().up(500), packet.getDirection()));
        }
    }

    public BlockPos getBlockTarget()
    {
        return mining;
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event)
    {
        if (modeConfig.getValue() != SpeedmineMode.PACKET)
        {
            return;
        }
        if (mc.player == null || mc.world == null
                || mc.player.isCreative() || mining != null && event.getPos() == mining)
        {
            return;
        }
        if (mining != null)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                    mining, Direction.DOWN));
        }
        mining = event.getPos();
        direction = event.getDirection();
        damage = 0.0f;
        if (mining != null && direction != null)
        {
            int slot = AutoToolModule.getInstance().getBestTool(event.getState());
            if (grimConfig.getValue())
            {
                Managers.INVENTORY.setSlot(slot);
            }
            event.cancel();
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                    mining, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, mining, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, mining, direction));
            if (grimConfig.getValue())
            {
                Managers.INVENTORY.syncToClient();
            }
        }
    }

    private void swapTo(int slot)
    {
        switch (swapConfig.getValue())
        {
            case NORMAL -> Managers.INVENTORY.setClientSlot(slot);
            case SILENT -> Managers.INVENTORY.setSlot(slot);
            case SILENT_ALT -> Managers.INVENTORY.setSlotAlt(slot);
        }
    }

    private void swapSync(int slot)
    {
        switch (swapConfig.getValue())
        {
            case SILENT -> Managers.INVENTORY.syncToClient();
            case SILENT_ALT -> Managers.INVENTORY.setSlotAlt(slot);
        }
    }

    float calcBlockBreakingDelta(BlockState state, BlockView world,
                                 BlockPos pos)
    {
        if (swapConfig.getValue() == Swap.OFF)
        {
            return state.calcBlockBreakingDelta(mc.player, mc.world, pos);
        }
        float f = state.getHardness(world, pos);
        if (f == -1.0f)
        {
            return 0.0f;
        }
        else
        {
            int i = canHarvest(state) ? 30 : 100;
            return getBlockBreakingSpeed(state) / f / (float) i;
        }
    }

    private float getBlockBreakingSpeed(BlockState block)
    {
        int tool = AutoToolModule.getInstance().getBestTool(block);
        float f = mc.player.getInventory().getStack(tool).getMiningSpeedMultiplier(block);
        if (f > 1.0F)
        {
            ItemStack stack = mc.player.getInventory().getStack(tool);
            int i = EnchantmentUtil.getLevel(stack, Enchantments.EFFICIENCY);
            if (i > 0 && !stack.isEmpty())
            {
                f += (float) (i * i + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(mc.player))
        {
            f *= 1.0f + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2f;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE))
        {
            float g = switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier())
            {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1e-4f;
            };
            f *= g;
        }
        if (mc.player.isSubmergedIn(FluidTags.WATER) && EnchantmentUtil.getLevel(mc.player.getEquippedStack(EquipmentSlot.FEET), Enchantments.AQUA_AFFINITY) <= 0)
        {
            f /= 5.0f;
        }
        if (!mc.player.isOnGround())
        {
            f /= 5.0f;
        }
        return f;
    }

    private boolean canHarvest(BlockState state)
    {
        if (state.isToolRequired())
        {
            int tool = AutoToolModule.getInstance().getBestTool(state);
            return mc.player.getInventory().getStack(tool).isSuitableFor(state);
        }
        return true;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mining == null || state == null || mc.player.isCreative()
                || modeConfig.getValue() != SpeedmineMode.PACKET)
        {
            return;
        }
        VoxelShape outlineShape = state.getOutlineShape(mc.world, mining);
        if (outlineShape.isEmpty())
        {
            return;
        }
        RenderBuffers.preRender();
        Box render1 = outlineShape.getBoundingBox();
        Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
        Vec3d center = render.getCenter();
        float scale = MathHelper.clamp(damage + (damage - lastDamage) * event.getTickDelta(), 0.0f, 1.0f);
        double dx = (render1.maxX - render1.minX) / 2.0;
        double dy = (render1.maxY - render1.minY) / 2.0;
        double dz = (render1.maxZ - render1.minZ) / 2.0;
        final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
        RenderManager.renderBox(event.getMatrices(), scaled,
                damage > 0.95f ? ColorUtil.withAlpha(Color.GREEN.getRGB(), 60) : ColorUtil.withAlpha(Color.RED.getRGB(), 60));
        RenderManager.renderBoundingBox(event.getMatrices(), scaled,
                2.5f, damage > 0.95f ? ColorUtil.withAlpha(Color.GREEN.getRGB(), 145) : ColorUtil.withAlpha(Color.RED.getRGB(), 145));
        RenderBuffers.postRender();
    }

    public enum SpeedmineMode
    {
        PACKET,
        DAMAGE
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }
}
