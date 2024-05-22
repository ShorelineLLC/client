package net.shoreline.client.impl.module.render;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.world.EntityUtil;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class TracersModule extends ToggleModule {

    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render tracers to player", true));
    Config<Color> playersColorConfig = register(new ColorConfig("PlayersColor", "The render color for players", new Color(200, 60, 60), false, () -> playersConfig.getValue()));
    Config<Boolean> invisiblesConfig = register(new BooleanConfig("Invisibles", "Render tracers to invisible entities", true));
    Config<Color> invisiblesColorConfig = register(new ColorConfig("InvisiblesColor", "The render color for invisibles", new Color(200, 100, 0), false, () -> invisiblesConfig.getValue()));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render tracers to monsters", true));
    Config<Color> monstersColorConfig = register(new ColorConfig("MonstersColor", "The render color for monsters", new Color(200, 60, 60), false, () -> monstersConfig.getValue()));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render tracers to animals", true));
    Config<Color> animalsColorConfig = register(new ColorConfig("AnimalsColor", "The render color for animals", new Color(0, 200, 0), false, () -> animalsConfig.getValue()));
    Config<Boolean> vehiclesConfig = register(new BooleanConfig("Vehicles", "Render tracers to vehicles", false));
    Config<Color> vehiclesColorConfig = register(new ColorConfig("VehiclesColor", "The render color for vehicles", new Color(200, 100, 0), false, () -> vehiclesConfig.getValue()));
    Config<Boolean> itemsConfig = register(new BooleanConfig("Items", "Render tracers to items", false));
    Config<Color> itemsColorConfig = register(new ColorConfig("ItemsColor", "The render color for items", new Color(255, 255, 255), false, () -> itemsConfig.getValue()));
    Config<Target> targetConfig = register(new EnumConfig<>("Target", "The body part of the entity to target", Target.FEET, Target.values()));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the tracer", 1.0f, 1.5f, 10.0f));

    public TracersModule() {
        super("Tracers", "Draws a tracer to all entities in render distance",
                ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        if (mc.player == null) {
            return;
        }
        RenderBuffers.preRender();
        boolean prevBobView = mc.options.getBobView().getValue();
        mc.options.getBobView().setValue(false);
        Camera cameraPos = mc.gameRenderer.getCamera();
        Vec3d pos = new Vec3d(0.0, 0.0, 1.0)
                .rotateX(-(float) Math.toRadians(cameraPos.getPitch()))
                .rotateY(-(float) Math.toRadians(cameraPos.getYaw()))
                .add(mc.cameraEntity.getEyePos());
        for (Entity entity : mc.world.getEntities()) {
            if (entity == null || !entity.isAlive() || entity == mc.player) {
                continue;
            }
            Color color = getTracerColor(entity);
            if (color != null) {
                // Vec3d entityPos = Interpolation.getRenderPosition(entity, event.getTickDelta()).add(0.0, getTargetY(entity), 0.0);
                // RenderManager.renderLine(event.getMatrices(), pos, entityPos, widthConfig.getValue(), color.getRGB());
            }
        }
        mc.options.getBobView().setValue(prevBobView);
        RenderBuffers.postRender();
    }

    private Color getTracerColor(Entity entity) {
        if (entity.isInvisible() && invisiblesConfig.getValue()) {
            return invisiblesColorConfig.getValue();
        } else if (entity instanceof PlayerEntity player && playersConfig.getValue()) {
            if (Managers.SOCIAL.isFriend(player.getName())) {
                return new Color(85, 200, 200, 255);
            }
            return playersColorConfig.getValue();
        } else if (EntityUtil.isMonster(entity) && monstersConfig.getValue()) {
            return monstersColorConfig.getValue();
        } else if ((EntityUtil.isPassive(entity) || EntityUtil.isNeutral(entity))
                && animalsConfig.getValue()) {
            return animalsColorConfig.getValue();
        } else if (EntityUtil.isVehicle(entity) && vehiclesConfig.getValue()) {
            return vehiclesColorConfig.getValue();
        } else if (entity instanceof ItemEntity && itemsConfig.getValue()) {
            return itemsColorConfig.getValue();
        }
        return null;
    }

    private double getTargetY(Entity entity) {
        return switch (targetConfig.getValue()) {
            case FEET -> 0.0;
            case TORSO -> entity.getHeight() / 2.0;
            case HEAD -> entity.getStandingEyeHeight();
        };
    }

    public enum Target {
        FEET,
        TORSO,
        HEAD
    }
}
