package net.shoreline.client.impl.module.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.util.Hand;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2SPacket;
import net.shoreline.client.util.network.InteractType;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoMountModule extends ToggleModule
{
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to mount entities", 0.1f, 4.0f, 6.0f));
    Config<Boolean> forceConfig = register(new BooleanConfig("ForceMount", "Forces the mounting without packets", false));
    Config<Boolean> horseConfig = register(new BooleanConfig("Horse", "Mounts horses", true));
    Config<Boolean> donkeyConfig = register(new BooleanConfig("Donkey", "Mounts donkeys", true));
    Config<Boolean> muleConfig = register(new BooleanConfig("Mule", "Mounts mules", true));
    Config<Boolean> llamaConfig = register(new BooleanConfig("Llama", "Mounts llamas", false));

    public AutoMountModule()
    {
        super("AutoMount", "Mounts nearby entities", ModuleCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (mc.player.isRiding())
        {
            return;
        }
        for (Entity entity : mc.world.getEntities())
        {
            double dist = mc.player.getEyePos().distanceTo(entity.getPos());
            if (dist > rangeConfig.getValue())
            {
                continue;
            }
            if (checkMount(entity))
            {
                mc.interactionManager.interactEntity(mc.player, entity, Hand.MAIN_HAND);
                return;
            }
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (event.getPacket() instanceof IPlayerInteractEntityC2SPacket packet && forceConfig.getValue()
                && packet.getType() == InteractType.INTERACT_AT && checkMount(packet.getEntity()))
        {
            event.cancel();
        }
    }

    private boolean checkMount(Entity entity)
    {
        return horseConfig.getValue() && (entity instanceof HorseEntity horseEntity && !horseEntity.isBaby() || entity instanceof SkeletonHorseEntity)
                || donkeyConfig.getValue() && entity instanceof DonkeyEntity || muleConfig.getValue() && entity instanceof MuleEntity
                || llamaConfig.getValue() && entity instanceof LlamaEntity llamaEntity && !llamaEntity.isBaby();
    }
}
