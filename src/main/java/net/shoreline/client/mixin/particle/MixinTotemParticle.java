package net.shoreline.client.mixin.particle;

import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.impl.event.particle.TotemParticleEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(TotemParticle.class)
public abstract class MixinTotemParticle extends MixinParticle
{
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void hookInit(ClientWorld world,
                          double x,
                          double y,
                          double z,
                          double velocityX,
                          double velocityY,
                          double velocityZ,
                          SpriteProvider spriteProvider,
                          CallbackInfo ci)
    {
        TotemParticleEvent totemParticleEvent = new TotemParticleEvent();
        EventBus.INSTANCE.dispatch(totemParticleEvent);
        if (totemParticleEvent.isCanceled())
        {
            Color color = totemParticleEvent.getColor();

            if (random.nextInt(4) == 0)
            {
                float r2 = (color.getRed() * 0.8f) / 255.0f;
                float g2 = (color.getGreen() * 0.8f) / 255.0f;
                float b2 = (color.getBlue() * 0.8f) / 255.0f;
                this.setColor(r2, g2, b2);
            } else
            {
                float r1 = MathHelper.clamp((color.getRed() / 255.0f) + random.nextFloat() * 0.1f, 0.0f, 1.0f);
                float g1 = MathHelper.clamp((color.getGreen() / 255.0f) + random.nextFloat() * 0.1f, 0.0f, 1.0f);
                float b1 = MathHelper.clamp((color.getBlue() / 255.0f) + random.nextFloat() * 0.1f, 0.0f, 1.0f);
                this.setColor(r1, g1, b1);
            }
        }
    }
}
