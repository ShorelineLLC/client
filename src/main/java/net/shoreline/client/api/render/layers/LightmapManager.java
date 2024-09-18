package net.shoreline.client.api.render.layers;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import net.shoreline.client.impl.event.render.LightmapInitEvent;
import net.shoreline.client.impl.event.render.LightmapTickEvent;
import net.shoreline.client.impl.event.render.LightmapUpdateEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import org.joml.Vector3f;

public class LightmapManager implements AutoCloseable, Globals
{
    private NativeImageBackedTexture texture;
    private NativeImage image;
    private Identifier textureIdentifier;
    private boolean dirty;
    private float flickerIntensity;
    private GameRenderer renderer;

    public LightmapManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onLightmapInit(LightmapInitEvent event)
    {
        renderer = mc.gameRenderer;
        texture = new NativeImageBackedTexture(16, 16, false);
        textureIdentifier = mc.getTextureManager().registerDynamicTexture("shoreline_light_map", texture);
        image = texture.getImage();

        for (int i = 0; i < 16; ++i)
        {
            for (int j = 0; j < 16; ++j)
            {
                image.setColor(j, i, -1);
            }
        }

        texture.upload();
    }

    @EventListener
    public void onLightmapTick(LightmapTickEvent event)
    {
        flickerIntensity += (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
        flickerIntensity *= 0.9F;
        dirty = true;
    }

    @EventListener
    public void onLightmapUpdate(LightmapUpdateEvent event)
    {
        if (renderer == null || texture == null || image == null)
        {
            return;
        }
        if (dirty)
        {
            dirty = false;
            ClientWorld clientWorld = mc.world;
            if (clientWorld != null) 
            {
                float f = clientWorld.getSkyBrightness(1.0f);
                float g;
                if (clientWorld.getLightningTicksLeft() > 0) 
                {
                    g = 1.0f;
                } 
                else 
                {
                    g = f * 0.95f + 0.05f;
                }

                float h = ((Double)mc.options.getDarknessEffectScale().getValue()).floatValue();
                float i = getDarknessFactor(event.getTickDelta()) * h;
                float j = getDarkness(mc.player, i, event.getTickDelta()) * h;
                float k = mc.player.getUnderwaterVisibility();
                float l;
                if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION))
                {
                    l = GameRenderer.getNightVisionStrength(mc.player, event.getTickDelta());
                }
                else if (k > 0.0f && mc.player.hasStatusEffect(StatusEffects.CONDUIT_POWER))
                {
                    l = k;
                }
                else
                {
                    l = 0.0f;
                }

                Vector3f vector3f = (new Vector3f(f, f, 1.0f)).lerp(new Vector3f(1.0f, 1.0f, 1.0f), 0.35f);
                float m = flickerIntensity + 1.5f;
                Vector3f vector3f2 = new Vector3f();

                for (int n = 0; n < 16; ++n)
                {
                    for (int o = 0; o < 16; ++o)
                    {
                        float p = getBrightness(clientWorld.getDimension(), n) * g;
                        float q = getBrightness(clientWorld.getDimension(), o) * m;
                        float s = q * ((q * 0.6f + 0.4F) * 0.6f + 0.4F);
                        float t = q * (q * q * 0.6f + 0.4F);
                        vector3f2.set(q, s, t);
                        boolean bl = clientWorld.getDimensionEffects().shouldBrightenLighting();
                        float u;
                        Vector3f vector3f4;
                        if (bl)
                        {
                            vector3f2.lerp(new Vector3f(0.99F, 1.12F, 1.0f), 0.25f);
                            clamp(vector3f2);
                        } 
                        else 
                        {
                            Vector3f vector3f3 = (new Vector3f(vector3f)).mul(p);
                            vector3f2.add(vector3f3);
                            vector3f2.lerp(new Vector3f(0.75f, 0.75f, 0.75f), 0.04F);
                            if (renderer.getSkyDarkness(event.getTickDelta()) > 0.0f) 
                            {
                                u = renderer.getSkyDarkness(event.getTickDelta());
                                vector3f4 = (new Vector3f(vector3f2)).mul(0.7F, 0.6f, 0.6f);
                                vector3f2.lerp(vector3f4, u);
                            }
                        }

                        float v;
                        if (l > 0.0f)
                        {
                            v = Math.max(vector3f2.x(), Math.max(vector3f2.y(), vector3f2.z()));
                            if (v < 1.0f)
                            {
                                u = 1.0f / v;
                                vector3f4 = (new Vector3f(vector3f2)).mul(u);
                                vector3f2.lerp(vector3f4, l);
                            }
                        }

                        if (!bl)
                        {
                            if (j > 0.0f)
                            {
                                vector3f2.add(-j, -j, -j);
                            }

                            clamp(vector3f2);
                        }

                        v = ((Double)mc.options.getGamma().getValue()).floatValue();
                        Vector3f vector3f5 = new Vector3f(easeOutQuart(vector3f2.x), easeOutQuart(vector3f2.y), easeOutQuart(vector3f2.z));
                        vector3f2.lerp(vector3f5, Math.max(0.0f, v - i));
                        vector3f2.lerp(new Vector3f(0.75f, 0.75f, 0.75f), 0.04F);
                        clamp(vector3f2);
                        vector3f2.mul(255.0f);
                        int x = (int)vector3f2.x();
                        int y = (int)vector3f2.y();
                        int z = (int)vector3f2.z();
                        image.setColor(o, n, -16777216 | z << 16 | y << 8 | x);
                    }
                }

                texture.upload();
            }
        }
    }

    public void close()
    {
        texture.close();
    }

    public void disable()
    {
        RenderSystem.setShaderTexture(2, 0);
    }

    public void enable()
    {
        if (textureIdentifier == null)
        {
            return;
        }
        RenderSystem.setShaderTexture(2, textureIdentifier);
        mc.getTextureManager().bindTexture(textureIdentifier);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
    }

    private float getDarknessFactor(float delta)
    {
        StatusEffectInstance statusEffectInstance = mc.player.getStatusEffect(StatusEffects.DARKNESS);
        return statusEffectInstance != null ? statusEffectInstance.getFadeFactor(mc.player, delta) : 0.0f;
    }

    private float getDarkness(LivingEntity entity, float factor, float delta)
    {
        float f = 0.45f * factor;
        return Math.max(0.0f, MathHelper.cos(((float)entity.age - delta) * 3.1415927F * 0.025f) * f);
    }

    private static void clamp(Vector3f vec)
    {
        vec.set(MathHelper.clamp(vec.x, 0.0f, 1.0f), MathHelper.clamp(vec.y, 0.0f, 1.0f), MathHelper.clamp(vec.z, 0.0f, 1.0f));
    }

    private float easeOutQuart(float x)
    {
        float f = 1.0f - x;
        return 1.0f - f * f * f * f;
    }

    public static float getBrightness(DimensionType type, int lightLevel)
    {
        float f = (float)lightLevel / 15.0f;
        float g = f / (4.0f - 3.0f * f);
        return MathHelper.lerp(type.ambientLight(), g, 1.0f);
    }

    public static int pack(int block, int sky)
    {
        return block << 4 | sky << 20;
    }
}
