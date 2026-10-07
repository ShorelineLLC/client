package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.LoadingEvent;
import net.shoreline.client.impl.event.particle.TotemParticleEvent;
import net.shoreline.client.impl.event.render.*;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

// TODO: Make settings better
public class TintModule extends Toggleable
{
    private final Identifier shineTexture;

    Config<Color> tintColor = new ColorConfig.Builder("Tint")
            .setDescription("The color of the world tint")
            .setDefaultValue(Color.RED).build();
    Config<Boolean> lightConfig = new BooleanConfig.Builder("Light")
            .setDescription("Change the color of world light")
            .setDefaultValue(false).build();
    Config<Boolean> foliageConfig = new BooleanConfig.Builder("Foliage")
            .setDescription("Change the color of foliage")
            .setDefaultValue(false).build();
    Config<Boolean> waterConfig = new BooleanConfig.Builder("Water")
            .setDescription("Change the color of water")
            .setDefaultValue(false).build();
    Config<Boolean> lavaConfig = new BooleanConfig.Builder("Lava")
            .setDescription("Change the color of lava")
            .setDefaultValue(false).build();
    Config<Boolean> glintConfig = new BooleanConfig.Builder("Glint")
            .setDescription("Change the color of enchantment glint")
            .setDefaultValue(false).build();
    Config<Boolean> totemsConfig = new BooleanConfig.Builder("TotemEffects")
            .setDescription("Change the color of totem particles")
            .setDefaultValue(false).build();

    public TintModule()
    {
        super("Tint", new String[] {"Ambience"}, "Change the world color tint", GuiCategory.RENDER);

        this.shineTexture = Identifier.of(ShorelineMod.MOD_ID, "textures/shine.png");
    }

    @Override
    public void onToggle()
    {
        reloadWorld();
    }

    @EventListener
    public void onFinishedLoading(LoadingEvent.Finished event)
    {
        for (Config<?> config : getConfigs())
        {
            config.addObserver(v -> reloadWorld());
        }

        lavaConfig.addObserver(v -> Managers.RESOURCE_PACK.toggleResourcePack("lava", v));
    }

    @EventListener
    public void onLightTint(WorldTintEvent.Light event)
    {
        if (lightConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    @EventListener
    public void onFoliageTint(WorldTintEvent.Foliage event)
    {
        if (foliageConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    @EventListener
    public void onWaterTint(WorldTintEvent.Water event)
    {
        if (waterConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    @EventListener
    public void onLavaTint(WorldTintEvent.Lava event)
    {
        if (lavaConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    @EventListener
    public void onBlockLight(BlockLightEvent event)
    {
        if (lightConfig.getValue())
        {
            event.cancel();
            event.setBlockLight(0);
        }
    }

    @EventListener
    public void onEntityLightBlock(EntityLightEvent.Block event)
    {
        if (lightConfig.getValue())
        {
            event.cancel();
            event.setLight(0);
        }
    }

    @EventListener
    public void onEntityLightSky(EntityLightEvent.Skylight event)
    {
        if (lightConfig.getValue())
        {
            event.cancel();
            event.setLight(15);
        }
    }

    @EventListener
    public void onLightData(LightDataEvent event)
    {
        if (lightConfig.getValue())
        {
            event.cancel();
            event.setSl(15);
        }
    }

    @EventListener
    public void onLuminance(LuminanceEvent event)
    {
        if (lightConfig.getValue())
        {
            event.cancel();
            event.setLuminance(0);
        }
    }

    @EventListener
    public void onTotemParticle(TotemParticleEvent event)
    {
        if (totemsConfig.getValue())
        {
            event.cancel();
            event.setColor(tintColor.getValue());
        }
    }

    @EventListener
    public void onGlintTexturePre(GlintTextureEvent.Pre event)
    {
        if (glintConfig.getValue())
        {
            mc.getTextureManager().getTexture(shineTexture).setFilter(true, false);
            RenderSystem.setShaderTexture(0, shineTexture);
            RenderSystem.setShaderColor((float) tintColor.getValue().getRed() / 255.0f,
                    (float) tintColor.getValue().getGreen() / 255.0f,
                    (float) tintColor.getValue().getBlue() / 255.0f, 1.0f);
        }
    }

    @EventListener
    public void onGlintTexturePost(GlintTextureEvent.Post event)
    {
        if (glintConfig.getValue())
        {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void reloadWorld()
    {
        if (mc.worldRenderer != null && mc.player != null)
        {
            int x = (int) mc.player.getX();
            int y = (int) mc.player.getY();
            int z = (int) mc.player.getZ();
            int d = mc.options.getViewDistance().getValue() * 16;
            mc.worldRenderer.scheduleBlockRenders(x - d, y - d, z - d, x + d, y + d, z + d);
        }
    }
}
