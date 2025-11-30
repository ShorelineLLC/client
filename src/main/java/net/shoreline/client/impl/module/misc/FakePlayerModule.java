package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2S;
import net.shoreline.client.impl.rotation.ClientRotationEvent;
import net.shoreline.client.util.entity.DamageableFakePlayer;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.List;

public class FakePlayerModule extends Toggleable
{
    Config<Boolean> crawlingPose = new BooleanConfig.Builder("Crawling")
            .setDescription("Sets the player in the crawling pose")
            .setDefaultValue(false).build();
    Config<Boolean> record = new BooleanConfig.Builder("Record")
            .setDescription("Will record your movement, toggle this and press play to play the recording")
            .setDefaultValue(false).build();
    Config<Boolean> play = new BooleanConfig.Builder("Play")
            .setDefaultValue(false)
            .setDescription("Will play the current recording").build();
    Config<Void> movement = new ConfigGroup.Builder("Movement")
            .addAll(record, play).build();

    private DamageableFakePlayer fakePlayer;
    private boolean clear;
    private int index;

    private final List<PositionShot> positions = new ArrayList<>();
    private final Timer gappleTimer = new NanoTimer();

    public FakePlayerModule()
    {
        super("FakePlayer", "Spawns a fake player", GuiCategory.MISCELLANEOUS);
    }

    @Override
    public void onEnable()
    {
        if (!checkNull())
        {
            fakePlayer = new DamageableFakePlayer(mc.player, "FakePlayer");
            fakePlayer.spawnPlayer();
        }
    }

    @Override
    public void onDisable()
    {
        if (!checkNull() && fakePlayer != null && !fakePlayer.isRemoved())
        {
            fakePlayer.despawnPlayer();
        }
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull() || fakePlayer == null)
        {
            return;
        }

        fakePlayer.setPose(crawlingPose.getValue() ? EntityPose.SWIMMING : EntityPose.STANDING);
        fakePlayer.baseTick();
        if (gappleTimer.hasPassed(1600))
        {
            fakePlayer.simulateGappleEat();
            gappleTimer.reset();
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof IPlayerInteractEntityC2S packet
                && packet.getInteractType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK)
        {
            final Entity attacked = packet.getEntity(mc.world);
            if (attacked != fakePlayer)
            {
                return;
            }

            fakePlayer.simulateAttackFrom(mc.world, mc.player);
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull() || fakePlayer == null)
        {
            return;
        }

        if (event.getPacket() instanceof ExplosionS2CPacket packet)
        {
            fakePlayer.simulateExplosionFrom(mc.world, packet.center());
        }
    }

    @EventListener
    public void onPosition(ClientRotationEvent event)
    {
        if (play.getValue())
        {
            record.setValue(false);
            if (positions.isEmpty())
            {
                play.setValue(false);
                return;
            }

            if (index >= positions.size())
            {
                index = 0;
            }

            PositionShot shot = positions.get(index++);
            fakePlayer.updateTrackedPositionAndAngles(shot.x, shot.y, shot.z, shot.yaw, shot.pitch, 2);
            fakePlayer.setHeadYaw(shot.headYaw);
            fakePlayer.setVelocity(shot.velocity);
        }
        else if (record.getValue())
        {
            if (!clear)
            {
                clear = true;
                positions.clear();
            }

            play.setValue(false);
            fakePlayer.setVelocity(new Vec3d(0, 0, 0));
            snapPosition();
        }
        else if (clear)
        {
            clear = false;
        }
    }

    public void snapPosition()
    {
        double x       = mc.player.getX();
        double y       = mc.player.getY();
        double z       = mc.player.getZ();
        float yaw      = mc.player.getYaw();
        float headYaw  = mc.player.getHeadYaw();
        float bodyYaw  = mc.player.getBodyYaw();
        float pitch    = mc.player.getPitch();
        Vec3d velocity = mc.player.getVelocity();
        PositionShot shot = new PositionShot(x, y, z, yaw, headYaw, bodyYaw, pitch, velocity);
        positions.add(shot);
    }

    public record PositionShot(double x, double y, double z, float yaw, float headYaw, float bodyYaw, float pitch, Vec3d velocity) {}
}
