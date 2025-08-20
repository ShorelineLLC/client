package net.shoreline.client.impl.module.misc;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IMinecraftClient;
import net.shoreline.eventbus.annotation.EventListener;

public class AutoFishModule extends Toggleable
{
    Config<Boolean> inventoryConfig = new BooleanConfig.Builder("Inventory")
            .setDescription("Allows you to fish while in the inventory")
            .setDefaultValue(true).build();
    Config<Integer> castingDelay = new NumberConfig.Builder<Integer>("CastDelay")
            .setMin(10).setMax(25).setDefaultValue(15)
            .setDescription("The delay between fishing rod cast").build();
    Config<Float> maxSoundRange = new NumberConfig.Builder<Float>("MaxSoundDist")
            .setMin(0.1f).setMax(5.0f).setDefaultValue(2.0f)
            .setDescription("The maximum distance from the splash sound").build();

    private boolean autoReel;
    private int autoReelTicks;
    private int autoCastTicks;

    public AutoFishModule()
    {
        super("AutoFish", "Automatically reels in your fishing rod", GuiCategory.MISCELLANEOUS);
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull() || mc.player.getMainHandStack().getItem() != Items.FISHING_ROD)
        {
            return;
        }

        if (event.getPacket() instanceof PlaySoundS2CPacket packet
                && packet.getSound().value() == SoundEvents.ENTITY_FISHING_BOBBER_SPLASH)
        {
            FishingBobberEntity fishHook = mc.player.fishHook;
            if (fishHook == null || fishHook.getPlayerOwner() != mc.player)
            {
                return;
            }

            double dist = fishHook.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ());
            if (dist <= maxSoundRange.getValue())
            {
                autoReel = true;
                autoReelTicks = 4;
            }
        }
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull() || mc.player.getMainHandStack().getItem() != Items.FISHING_ROD)
        {
            return;
        }

        if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !inventoryConfig.getValue())
        {
            return;
        }

        FishingBobberEntity fishHook = mc.player.fishHook;
        if ((fishHook == null || fishHook.getHookedEntity() != null) && autoCastTicks <= 0)
        {
            ((IMinecraftClient) mc).hookDoItemUse();
            autoCastTicks = castingDelay.getValue();
            return;
        }

        if (autoReel)
        {
            if (autoReelTicks <= 0)
            {
                ((IMinecraftClient) mc).hookDoItemUse();
                autoReel = false;
                return;
            }

            autoReelTicks--;
        }

        autoCastTicks--;
    }
}
