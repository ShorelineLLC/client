package net.shoreline.client.impl.module.combat;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.BlockPlacerModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.SkipRegister;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.world.EntityUtil;
import net.shoreline.client.util.world.ExplosionUtil;

import java.util.ArrayList;
import java.util.List;

@SkipRegister
public class AutoAnchorModule extends BlockPlacerModule {

    Config<Float> targetRangeConfig = new NumberConfig<>("EnemyRange", "Range to search for potential enemies", 1.0f, 10.0f, 13.0f);
    Config<Boolean> swingConfig = new BooleanConfig("Swing", "Swing hand when exploding anchors", true);
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotate before exploding", false);
    Config<Boolean> playersConfig = new BooleanConfig("Players", "Target players", true);
    Config<Boolean> monstersConfig = new BooleanConfig("Monsters", "Target monsters", false);
    Config<Boolean> neutralsConfig = new BooleanConfig("Neutrals", "Target neutrals", false);
    Config<Boolean> animalsConfig = new BooleanConfig("Animals", "Target animals", false);
    Config<Float> rangeConfig = new NumberConfig<>("Range", "Range to explode anchors", 0.1f, 4.0f, 6.0f);
    Config<Boolean> placeConfig = new BooleanConfig("Place", "Places anchors to damage enemies", true);
    Config<Float> explodeSpeedConfig = new NumberConfig<>("ExplodeSpeed", "Speed to place anchors", 0.1f, 18.0f, 20.0f, () -> placeConfig.getValue());
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Interacts with only visible directions when placing crystals", false, () -> placeConfig.getValue());
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "Places using grim instant rotations", false, () -> rotateConfig.getValue() && placeConfig.getValue());
    Config<Float> minDamageConfig = new NumberConfig<>("MinDamage", "Minimum damage required to consider exploding anchors", 1.0f, 4.0f, 10.0f);
    Config<Boolean> safetyConfig = new BooleanConfig("Safety", "Accounts for total player safety when exploding anchors", true);
    Config<Float> maxLocalDamageConfig = new NumberConfig<>("MaxLocalDamage", "The maximum player damage", 4.0f, 12.0f, 20.0f);
    Config<Boolean> blockDestructionConfig = new BooleanConfig("BlockDestruction", "Accounts for explosion block destruction when calculating damages", false);
    //
    private BlockPos anchorPos;
    private final Timer explodeTimer = new CacheTimer();

    public AutoAnchorModule() {
        super("AutoAnchor", "Automatically places and explodes respawn anchors", ModuleCategory.COMBAT);
    }

    @Override
    public void onDisable() {
        anchorPos = null;
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        ArrayList<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        List<BlockPos> blocks = getSphere(mc.player.getPos());
        anchorPos = calculateAnchorExplosion(blocks, entities);
        if (anchorPos != null) {
            if (rotateConfig.getValue()) {
                float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), anchorPos.toCenterPos());
                setRotation(rotations[0], rotations[1]);
            }
            if (explodeTimer.passed(1000.0f - explodeSpeedConfig.getValue() * 50.0f)) {
                BlockState state = mc.world.getBlockState(anchorPos);
                if (state.getBlock() instanceof RespawnAnchorBlock) {
                    setAnchor(anchorPos);
                } else {
                    placeAnchor(anchorPos);
                }
                explodeTimer.reset();
            }
        }
    }

    private void setAnchor(BlockPos pos) {
        int slot = getBlockItemSlot(Blocks.GLOWSTONE);
        if (slot == -1) {
            return;
        }
        Managers.INVENTORY.setSlot(slot);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(pos.toCenterPos(),
                strictDirectionConfig.getValue() ? Managers.INTERACT.getPlaceDirectionGrim(pos) : Direction.UP, pos, true));
        if (swingConfig.getValue()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        Managers.INVENTORY.syncToClient();
    }

    private void placeAnchor(BlockPos pos) {
        int slot = getBlockItemSlot(Blocks.RESPAWN_ANCHOR);
        if (slot == -1) {
            return;
        }
        Managers.INVENTORY.setSlot(slot);
        Managers.INTERACT.placeBlock(pos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, (state, angles) ->
        {
            if (rotateConfig.getValue())
            {
                if (state)
                {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1], grimConfig.getValue());
                }
                else
                {
                    Managers.ROTATION.setRotationSilentSync(grimConfig.getValue());
                }
            }
        });
        Managers.INVENTORY.syncToClient();
    }

    // Most of the below code is stolen from the ca
    private BlockPos calculateAnchorExplosion(List<BlockPos> placeBlocks, List<Entity> entities) {
        if (placeBlocks.isEmpty() || entities.isEmpty()) {
            return null;
        }
        BlockPos data = null;
        double dmg = 0.0f;
        for (BlockPos pos : placeBlocks) {
            BlockState state = mc.world.getBlockState(pos);
            if (!rangeCheck(pos) || !state.isReplaceable() && !(state.getBlock() instanceof RespawnAnchorBlock)) {
                continue;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player,
                    pos.toCenterPos(), blockDestructionConfig.getValue(), 10.0f); // Anchor explosions power = 10
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer) {
                continue;
            }
            for (Entity entity : entities) {
                if (entity == null || !entity.isAlive() || entity == mc.player
                        || !isValidTarget(entity)
                        || Managers.SOCIAL.isFriend(entity.getName())) {
                    continue;
                }
                double blockDist = pos.getSquaredDistance(entity.getPos());
                if (blockDist > 144.0f) {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue()) {
                    continue;
                }
                double damage = ExplosionUtil.getDamageTo(entity,
                        pos.toCenterPos(), blockDestructionConfig.getValue(), 10.0f);
                if (data == null || damage > dmg) {
                    data = pos;
                    dmg = damage;
                }
            }
        }
        if (data == null || dmg < minDamageConfig.getValue()) {
            return null;
        }
        return data;
    }

    private boolean playerDamageCheck(double playerDamage) {
        if (!mc.player.isCreative()) {
            float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            if (safetyConfig.getValue() && playerDamage >= health + 0.5f) {
                return true;
            }
            return playerDamage > maxLocalDamageConfig.getValue();
        }
        return false;
    }

    private boolean rangeCheck(BlockPos pos) {
        double dist = mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos());
        return dist > ((NumberConfig) rangeConfig).getValueSq();
    }

    private List<BlockPos> getSphere(Vec3d origin) {
        List<BlockPos> sphere = new ArrayList<>();
        double rad = Math.ceil(rangeConfig.getValue());
        for (double x = -rad; x <= rad; ++x) {
            for (double y = -rad; y <= rad; ++y) {
                for (double z = -rad; z <= rad; ++z) {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    private boolean isValidTarget(Entity e) {
        return e instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(e) && monstersConfig.getValue()
                || EntityUtil.isNeutral(e) && neutralsConfig.getValue()
                || EntityUtil.isPassive(e) && animalsConfig.getValue();
    }
}
