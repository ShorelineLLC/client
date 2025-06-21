package net.shoreline.client.util.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FakePlayerEntity extends OtherClientPlayerEntity
{
    public static final AtomicInteger CURRENT_ID = new AtomicInteger(1000000);
    private final PlayerEntity player;

    public FakePlayerEntity(PlayerEntity player, String name)
    {
        super(MinecraftClient.getInstance().world,
                new GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), name));
        this.player = player;
        copyPositionAndRotation(player);
        headYaw = player.headYaw;
        bodyYaw = player.bodyYaw;
        // limbAnimator.pos = player.limbAnimator.getPos();
        limbAnimator.setSpeed(player.limbAnimator.getSpeed());
        Byte playerModel = player.getDataTracker()
                .get(PlayerEntity.PLAYER_MODEL_PARTS);
        dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);
        getAttributes().setFrom(player.getAttributes());
        setSneaking(player.isSneaking());
        setSwimming(player.isSwimming());
        setPose(player.getPose());
        setHealth(player.getHealth());
        setAbsorptionAmount(player.getAbsorptionAmount());
        getInventory().clone(player.getInventory());
        setId(CURRENT_ID.incrementAndGet());
        this.age = 100;
    }

    public FakePlayerEntity(PlayerEntity player)
    {
        this(player, player.getName().getString());
    }

    public void spawnPlayer()
    {
        unsetRemoved();
        MinecraftClient.getInstance().world.addEntity(this);
    }

    public void despawnPlayer()
    {
        MinecraftClient.getInstance().world.removeEntity(getId(), RemovalReason.DISCARDED);
        setRemoved(RemovalReason.DISCARDED);
    }

    @Override
    public boolean isDead()
    {
        return false;
    }

    public PlayerEntity getPlayer()
    {
        return player;
    }
}