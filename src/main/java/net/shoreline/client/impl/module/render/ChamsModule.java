package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.api.render.chams.ChamsModelRenderer;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.entity.RenderCrystalEvent;
import net.shoreline.client.impl.event.render.entity.RenderEntityEvent;
import net.shoreline.client.util.world.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;

public class ChamsModule extends ToggleModule
{
    Config<ChamsMode> modeConfig = register(new EnumConfig<>("Mode", "The rendering mode for the chams", ChamsMode.FILL, ChamsMode.values()));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the render", 1.0f, 1.5f, 5.0f, () -> modeConfig.getValue() != ChamsMode.FILL));
    Config<Boolean> wallsConfig = register(new BooleanConfig("Walls", "Renders chams through walls", true));
    // Config<Boolean> shineConfig = register(new BooleanConfig("Shine", "Adds enchantment glint", false));
    Config<Boolean> textureConfig = register(new BooleanConfig("Texture", "Renders the entity model texture", false));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Render chams on the player", true));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render chams on other players", true));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render chams on monsters", true));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render chams on animals", true));
    Config<Boolean> crystalsConfig = register(new BooleanConfig("Crystals", "Render chams on crystals", true));
    Config<Color> colorConfig = register(new ColorConfig("Color", "The color of the chams", new Color(255, 0, 0, 60)));

    public ChamsModule()
    {
        super("Chams", "Renders entity models through walls", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        RenderBuffers.preRender();
        if (!wallsConfig.getValue())
        {
            RenderSystem.enableDepthTest();
        }
        for (Entity entity : mc.world.getEntities())
        {
            double x = Math.abs(mc.gameRenderer.getCamera().getPos().x - entity.getX());
            double z = Math.abs(mc.gameRenderer.getCamera().getPos().z - entity.getZ());
            double d = (mc.options.getViewDistance().getValue() + 1) * 16;
            if (x > d || z > d)
            {
                continue;
            }
            if (!RenderManager.isFrustumVisible(entity.getBoundingBox()))
            {
                continue;
            }
            if (entity instanceof LivingEntity livingEntity && checkChams(livingEntity) || entity instanceof EndCrystalEntity && crystalsConfig.getValue())
            {
                if (!wallsConfig.getValue())
                {
                    RenderSystem.depthMask(false);
                }
                ChamsModelRenderer.render(event.getMatrices(), entity, event.getTickDelta(), colorConfig.getValue().getRGB(),
                        widthConfig.getValue(), modeConfig.getValue() != ChamsMode.FILL, modeConfig.getValue() != ChamsMode.WIREFRAME, false);
            }
        }

        RenderBuffers.postRender();
        if (!wallsConfig.getValue())
        {
            RenderSystem.depthMask(true);
        }
    }

    @EventListener
    public void onRenderCrystal(RenderCrystalEvent event)
    {
        if (!textureConfig.getValue() && crystalsConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderEntity(RenderEntityEvent event)
    {
        if (textureConfig.getValue() || !checkChams(event.entity))
        {
            return;
        }
        event.cancel();
    }

    private boolean checkChams(LivingEntity entity)
    {
        if (entity instanceof PlayerEntity)
        {
            if (entity == mc.player)
            {
                return selfConfig.getValue() && (!mc.options.getPerspective().isFirstPerson() || FreecamModule.getInstance().isEnabled());
            }
            else
            {
                return playersConfig.getValue();
            }
        }
        return (EntityUtil.isMonster(entity) && monstersConfig.getValue()
                || (EntityUtil.isNeutral(entity)
                || EntityUtil.isPassive(entity)) && animalsConfig.getValue());
    }

    public enum ChamsMode
    {
        FILL,
        WIREFRAME,
        WIRE_FILL
    }
}
