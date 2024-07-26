package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
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
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.entity.RenderCrystalEvent;
import net.shoreline.client.impl.event.render.entity.RenderEntityEvent;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.EntityUtil;
import net.shoreline.client.util.world.FakePlayerEntity;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ChamsModule extends ToggleModule
{
    Config<ChamsMode> modeConfig = register(new EnumConfig<>("Mode", "The rendering mode for the chams", ChamsMode.FILL, ChamsMode.values()));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the render", 1.0f, 1.5f, 5.0f, () -> modeConfig.getValue() != ChamsMode.FILL));
    Config<Boolean> wallsConfig = register(new BooleanConfig("ThroughWalls", "Renders chams through walls", true));
    // Config<Boolean> shineConfig = register(new BooleanConfig("Shine", "Adds enchantment glint", false));
    Config<Boolean> textureConfig = register(new BooleanConfig("Texture", "Renders the entity model texture", false));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render chams on other players", true));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Render chams on the player", true, () -> playersConfig.getValue()));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render chams on monsters", true));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render chams on animals", true));
    Config<Boolean> crystalsConfig = register(new BooleanConfig("Crystals", "Render chams on crystals", true));
    Config<Boolean> popsConfig = register(new BooleanConfig("Pops", "Render chams on totem pops", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 1000, 3000, () -> false));
    Config<Color> colorConfig = register(new ColorConfig("Color", "The color of the chams", new Color(255, 0, 0, 60)));

    private final Map<FakePlayerEntity, Animation> fadeList = new HashMap<>();

    public ChamsModule()
    {
        super("Chams", "Renders entity models through walls", ModuleCategory.RENDER);
    }

    @Override
    public void onDisable()
    {
        fadeList.clear();
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
                int color = colorConfig.getValue().getRGB();
                int lineColor = ColorUtil.withAlpha(color, 145);
                ChamsModelRenderer.render(event.getMatrices(), entity, event.getTickDelta(), color, lineColor,
                        widthConfig.getValue(), modeConfig.getValue() != ChamsMode.FILL, modeConfig.getValue() != ChamsMode.WIREFRAME, false);
            }
        }
        for (Map.Entry<FakePlayerEntity, Animation> set : fadeList.entrySet())
        {
            set.getValue().setState(false);
            Color color = colorConfig.getValue();
            int boxAlpha = (int) (color.getAlpha() * set.getValue().getFactor());
            int lineAlpha = (int) (145 * set.getValue().getFactor());
            int boxColor = ColorUtil.withAlpha(color.getRGB(), boxAlpha);
            int lineColor = ColorUtil.withAlpha(color.getRGB(), lineAlpha);
            ChamsModelRenderer.render(event.getMatrices(), set.getKey(), event.getTickDelta(), boxColor, lineColor,
                    widthConfig.getValue(), modeConfig.getValue() != ChamsMode.FILL, modeConfig.getValue() != ChamsMode.WIREFRAME, false);
        }

        fadeList.entrySet().removeIf(e ->
                e.getValue().getFactor() == 0.0);

        RenderBuffers.postRender();
        if (!wallsConfig.getValue())
        {
            RenderSystem.depthMask(true);
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof EntityStatusS2CPacket packet
                && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING && popsConfig.getValue())
        {
            Entity entity = packet.getEntity(mc.world);
            if (!(entity instanceof PlayerEntity player))
            {
                return;
            }
            Animation animation = new Animation(true, fadeTimeConfig.getValue());
            fadeList.put(new FakePlayerEntity(player), animation);
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
