package net.shoreline.client.util.entity;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.impl.Managers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class FakePlayerEntity extends OtherClientPlayerEntity
{
    public static final UUID FAKE_UUID = UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7");

    public static final AtomicInteger CURRENT_ID = new AtomicInteger(1000000);

    private final PlayerEntity player;

    public FakePlayerEntity(PlayerEntity player, String name)
    {
        super(MinecraftClient.getInstance().world, new GameProfile(FAKE_UUID, name));
        this.player = player;
        this.age = 100;

        copyPositionAndRotation(player);
        this.headYaw = player.headYaw;
        this.bodyYaw = player.bodyYaw;
        this.limbAnimator.setSpeed(player.limbAnimator.getSpeed());

        Byte playerModel = player.getDataTracker().get(PlayerEntity.PLAYER_MODEL_PARTS);
        dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, playerModel);
        getAttributes().setFrom(player.getAttributes());
        setSneaking(player.isSneaking());
        setSwimming(player.isSwimming());
        setPose(player.getPose());
        setHealth(player.getHealth());

        getInventory().clone(player.getInventory());
        setId(CURRENT_ID.incrementAndGet());
    }

    public FakePlayerEntity(PlayerEntity player)
    {
        this(player, player.getName().getString());
    }

    @Override
    public boolean isAlive()
    {
        return true;
    }

    @Override
    public boolean isDead()
    {
        return false;
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
        Managers.TOTEM.clearTotems(this);
    }
}