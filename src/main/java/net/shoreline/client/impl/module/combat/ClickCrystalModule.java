package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.world.AddEntityEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.impl.module.RotationModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ClickCrystalModule extends RotationModule
{

    Config<Float> breakDelayConfig = register(new NumberConfig<>("SpawnDelay", "Speed to break crystals after spawning", 0.0f, 0.0f, 20.0f));
    Config<Float> randomDelayConfig = register(new NumberConfig<>("RandomDelay", "Randomized break delay", 0.0f, 0.0f, 5.0f));
    Config<Boolean> rotateConfig = register(new BooleanConfig("Rotate", "Rotate before breaking", false));
    Config<Boolean> randomRotateConfig = register(new BooleanConfig("RotateJitter", "Slightly randomizes rotations", false, () -> rotateConfig.getValue()));
    private final Set<BlockPos> placedCrystals = new HashSet<>();
    private final Map<EndCrystalEntity, Long> spawnedCrystals = new LinkedHashMap<>();
    private float randomDelay = -1;

    public ClickCrystalModule()
    {
        super("ClickCrystal", "Automatically breaks placed crystals", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (spawnedCrystals.isEmpty())
        {
            return;
        }
        Map.Entry<EndCrystalEntity, Long> e = spawnedCrystals.entrySet().iterator().next();
        EndCrystalEntity crystalEntity = e.getKey();
        Long time = e.getValue();
        if (randomDelay == -1)
        {
            randomDelay = randomDelayConfig.getValue() == 0.0f ? 0.0f : RANDOM.nextFloat(randomDelayConfig.getValue() * 25.0f);
        }
        float breakDelay = breakDelayConfig.getValue() * 50.0f + randomDelay;
        double dist = mc.player.getEyePos().squaredDistanceTo(crystalEntity.getPos());
        if (dist <= 12.25 && System.currentTimeMillis() - time >= breakDelay)
        {
            if (rotateConfig.getValue())
            {
                Vec3d rotatePos = crystalEntity.getPos();
                if (randomRotateConfig.getValue())
                {
                    Box bb = crystalEntity.getBoundingBox();
                    rotatePos = new Vec3d(RANDOM.nextDouble(bb.minX, bb.maxX), RANDOM.nextDouble(bb.minY, bb.maxY), RANDOM.nextDouble(bb.minZ, bb.maxZ));
                }
                float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), rotatePos);
                setRotation(rotations[0], rotations[1]);
            }
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(crystalEntity, mc.player.isSneaking()));
            mc.player.swingHand(Hand.MAIN_HAND);
            randomDelay = -1;
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet && !event.isClientPacket() && mc.player.getStackInHand(packet.getHand()).getItem() instanceof EndCrystalItem)
        {
            placedCrystals.add(packet.getBlockHitResult().getBlockPos());
        }
    }

    @EventListener
    public void onAddEntity(AddEntityEvent event)
    {
        if (event.getEntity() instanceof EndCrystalEntity crystalEntity)
        {
            BlockPos base = crystalEntity.getBlockPos().down();
            if (placedCrystals.contains(base))
            {
                spawnedCrystals.put(crystalEntity, System.currentTimeMillis());
                placedCrystals.remove(base);
            }
        }
    }

    @EventListener
    public void onRemoveEntity(RemoveEntityEvent event)
    {
        if (event.getEntity() instanceof EndCrystalEntity crystalEntity)
        {
            spawnedCrystals.remove(crystalEntity);
        }
    }
}
