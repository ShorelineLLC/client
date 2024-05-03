package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.RotationModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.world.EndCrystalUtil;

import java.util.List;

/**
 * @author Shoreline
 * @since 1.0
 */
public class AutoMineModule extends RotationModule {

    Config<Boolean> multitaskConfig = new BooleanConfig("Multitask", "Allows mining while using items", false);
    Config<Boolean> autoConfig = new BooleanConfig("Auto", "Automatically mines nearby players feet", false);
    Config<Boolean> autoRemineConfig = new BooleanConfig("AutoRemine", "Automatically remines mined blocks", true, () -> autoConfig.getValue());
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Only mines on visible faces", false, () -> autoConfig.getValue());
    Config<Float> enemyRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for targets", 1.0f, 5.0f, 10.0f, () -> autoConfig.getValue());
    // Config<Boolean> doubleBreakConfig = new BooleanConfig("DoubleBreak", "Allows you to mine two blocks at once", false);
    Config<Float> rangeConfig = new NumberConfig<>("Range", "The range to mine blocks", 0.1f, 4.0f, 5.0f);
    Config<Float> speedConfig = new NumberConfig<>("Speed", "The speed to mine blocks", 0.1f, 1.0f, 1.0f);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates when mining the block", true);
    Config<Boolean> switchResetConfig = new BooleanConfig("SwitchReset", "Resets mining after switching items", false);
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "Uses grim block breaking speeds", false);
    Config<Boolean> instantConfig = new BooleanConfig("Instant", "Instant remines mined blocks", true);
    //
    private MiningData miningData;
    private long lastBreak;
    private boolean manualOverride;

    public AutoMineModule() {
        super("AutoMine", "Automatically mines blocks", ModuleCategory.WORLD, 900);
    }

    @Override
    public String getModuleData() {
        if (miningData != null)
        {
            return String.format("%.1f", Math.min(miningData.getBlockDamage(), miningData.getSpeed()));
        }
        return super.getModuleData();
    }

    @Override
    protected void onDisable()
    {
        if (miningData != null && miningData.isStarted())
        {
            abortMining(miningData);
        }
        miningData = null;
        manualOverride = false;
        Managers.INVENTORY.syncToClient();
    }

    @EventListener
    public void onPlayerTick(final PlayerTickEvent event)
    {
        if (autoConfig.getValue() && !manualOverride && (miningData == null || mc.world.isAir(miningData.getPos())))
        {
            PlayerEntity playerTarget = null;
            double minDistance = Float.MAX_VALUE;
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity == mc.player || Managers.SOCIAL.isFriend(entity.getName())) {
                    continue;
                }
                double dist = mc.player.distanceTo(entity);
                if (dist > enemyRangeConfig.getValue()) {
                    continue;
                }
                if (dist < minDistance) {
                    minDistance = dist;
                    playerTarget = entity;
                }
            }
            if (playerTarget != null) {
                final BlockPos cityBlockPos = getAutoMinePosition(playerTarget);
                if (cityBlockPos != null)
                {
                    // If we are re-mining, bypass throttle check below
                    if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !mc.world.isAir(miningData.getPos()) && autoRemineConfig.getValue())
                    {
                        stopMining(miningData);
                    }
                    else if (!mc.world.isAir(cityBlockPos) && !isBlockDelayNotBalanced())
                    {
                        if (miningData != null && miningData.getBlockDamage() > 0.0f)
                        {
                            abortMining(miningData);
                        }
                        miningData = new AutoMiningData(cityBlockPos,
                                strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(cityBlockPos) : Direction.UP,
                                speedConfig.getValue());
                        startMining(miningData);
                    }
                }
            }

        }
        if (miningData != null)
        {
            final double distance = miningData.getPos().getSquaredDistance(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
            if (distance > ((NumberConfig<Float>) rangeConfig).getValueSq())
            {
                abortMining(miningData);
                miningData = null;
                return;
            }
            if (miningData.getState().isAir())
            {
                // Once we broke the block that overrode that the auto city, we can allow the module
                // to auto mine "city" blocks
                if (manualOverride)
                {
                    manualOverride = false;
                    miningData = null;
                    return;
                }
                if (instantConfig.getValue())
                {
                    if (miningData instanceof AutoMiningData && !autoRemineConfig.getValue()) {
                        miningData = null;
                        return;
                    }
                    miningData.setInstantRemine();
                    miningData.setDamage(1.0f);
                }
                else
                {
                    miningData.resetDamage();
                }
                return;
            }
            final float damageDelta = Modules.SPEEDMINE.calcBlockBreakingDelta(
                    miningData.getState(), mc.world, miningData.getPos());
            if (miningData.damage(damageDelta) >= miningData.getSpeed() || miningData.isInstantRemine())
            {
                if (mc.player.isUsingItem() && !multitaskConfig.getValue())
                {
                    return;
                }
                stopMining(miningData);
            }
        }
    }

    @EventListener
    public void onAttackBlock(final AttackBlockEvent event)
    {
        // Do not try to break unbreakable blocks
        if (event.getState().getBlock().getHardness() == -1.0f || mc.player.isCreative())
        {
            return;
        }
        event.cancel();
        mc.player.swingHand(Hand.MAIN_HAND);
        if (miningData != null)
        {
            if (miningData.getPos().equals(event.getPos()))
            {
                return;
            }
            abortMining(miningData);
        }
        else if (autoConfig.getValue())
        {
            // Only count as an override if AutoCity is doing something
            if (miningData instanceof AutoMiningData)
            {
                abortMining(miningData);
                manualOverride = true;
            }
        }
        miningData = new MiningData(event.getPos(), event.getDirection(), speedConfig.getValue());
        startMining(miningData);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchResetConfig.getValue()) {
            miningData.resetDamage();
        }
    }

    @EventListener
    public void onRenderWorld(final RenderWorldEvent event)
    {
        MiningData data = miningData;
        if (data != null && !mc.player.isCreative()) {
            BlockPos mining = data.getPos();
            VoxelShape outlineShape = VoxelShapes.fullCube();
            if (!data.isInstantRemine()) {
                outlineShape = data.getState().getOutlineShape(mc.world, mining);
                outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
            }
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                    mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                    mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
            Vec3d center = render.getCenter();
            float scale = MathHelper.clamp(data.getBlockDamage() / speedConfig.getValue(), 0, 1.0f);
            double dx = (render1.maxX - render1.minX) / 2.0;
            double dy = (render1.maxY - render1.minY) / 2.0;
            double dz = (render1.maxZ - render1.minZ) / 2.0;
            final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
            RenderManager.renderBox(event.getMatrices(), scaled,
                    data.getBlockDamage() > (0.95f * speedConfig.getValue()) ? 0x6000ff00 : 0x60ff0000);
            RenderManager.renderBoundingBox(event.getMatrices(), scaled,
                    2.5f, data.getBlockDamage() > (0.95f * speedConfig.getValue()) ? 0x6000ff00 : 0x60ff0000);
        }
    }

    private BlockPos getAutoMinePosition(PlayerEntity entity) {
        List<BlockPos> entityIntersections = Modules.SURROUND.getSurroundEntities(entity);
        for (BlockPos blockPos : entityIntersections) {
            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq()) {
                continue;
            }
            if (!mc.world.getBlockState(blockPos).isReplaceable()) {
                return blockPos;
            }
        }
        List<BlockPos> surroundBlocks = Modules.SURROUND.getEntitySurroundNoSupport(entity);
        BlockPos minePos = null;
        double bestDamage = 0.0f;
        for (BlockPos blockPos : surroundBlocks) {
            double dist = mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig<Float>) rangeConfig).getValueSq()) {
                continue;
            }
            if (mc.world.isAir(blockPos) && !autoRemineConfig.getValue()) {
                continue;
            }
            double damage = EndCrystalUtil.getDamageTo(entity, blockPos.toCenterPos().subtract(0.0, -0.5, 0.0), true);
            if (damage > bestDamage) {
                bestDamage = damage;
                minePos = blockPos;
            }
        }
        return minePos;
    }

    private void startMining(MiningData data) {
        if (data.getState().isAir()) {
            return;
        }
        boolean canSwap = data.getSlot() != -1;
        if (canSwap) {
            Managers.INVENTORY.setSlot(data.getSlot());
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        if (canSwap) {
            Managers.INVENTORY.syncToClient();
        }
        data.setStarted();
    }

    private void abortMining(MiningData data) {
        if (!data.isStarted() || data.getState().isAir() || data.isInstantRemine() || data.getBlockDamage() >= 1.0f) {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        Managers.INVENTORY.syncToClient();
    }

    private void stopMining(MiningData data) {
        if (!data.isStarted() || data.getState().isAir()) {
            return;
        }
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
        boolean canSwap = data.getSlot() != -1;
        if (canSwap) {
            Managers.INVENTORY.setSlot(data.getSlot());
        }
        if (rotateConfig.getValue()) {
            float rotations[] = RotationUtil.getRotationsTo(mc.player.getEyePos(), data.getPos().toCenterPos());
            setRotationSilent(rotations[0], rotations[1]);
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        if (grimConfig.getValue()) {
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos().up(500), data.getDirection(), id));
        }
        data.setBroken();
        lastBreak = System.currentTimeMillis();
        if (canSwap) {
            Managers.INVENTORY.syncToClient();
        }
        if (rotateConfig.getValue()) {
            Managers.ROTATION.setRotationSilentSync(true);
        }
    }

    // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L80
    public boolean isBlockDelayNotBalanced() {
        return System.currentTimeMillis() - lastBreak <= 280 && grimConfig.getValue();
    }

    public static class AutoMiningData extends MiningData {

        public AutoMiningData(BlockPos pos, Direction direction, float speed) {
            super(pos, direction, speed);
        }
    }

    public static class MiningData {

        private final BlockPos pos;
        private final Direction direction;
        private final float speed;
        private float blockDamage;
        private boolean instantRemine;
        private boolean started;
        private int breakPackets;

        public MiningData(BlockPos pos, Direction direction, float speed) {
            this.pos = pos;
            this.direction = direction;
            this.speed = speed;
        }

        public boolean isInstantRemine() {
            return instantRemine;
        }

        public void setInstantRemine() {
            this.instantRemine = true;
        }

        public float damage(final float dmg)
        {
            blockDamage += dmg;
            return blockDamage;
        }

        public void setDamage(float blockDamage) {
            this.blockDamage = blockDamage;
        }

        public void resetDamage() {
            instantRemine = false;
            blockDamage = 0.0f;
        }

        public int getBreakPackets() {
            return breakPackets;
        }

        public void setBroken() {
            breakPackets++;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Direction getDirection() {
            return direction;
        }

        public float getSpeed() {
            return speed;
        }

        public int getSlot() {
            return Modules.AUTO_TOOL.getBestToolNoFallback(getState());
        }

        public BlockState getState() {
            return mc.world.getBlockState(pos);
        }

        public boolean isStarted() {
            return started;
        }

        public void setStarted() {
            this.started = true;
        }

        public float getBlockDamage() {
            return blockDamage;
        }
    }
}