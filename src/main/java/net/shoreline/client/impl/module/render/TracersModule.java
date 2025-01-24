package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorCamera;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;
import org.joml.Matrix4f;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class TracersModule extends ToggleModule
{
    Config<RenderMode> modeConfig = register(new EnumConfig<>("Mode", "Render tracers to entities not visible on the screen", RenderMode.NORMAL, RenderMode.values()));
    Config<Target> targetConfig = register(new EnumConfig<>("Target", "The body part of the entity to target", Target.FEET, Target.values()));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the tracer", 1.0f, 1.0f, 5.0f));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render tracers to player", true));
    Config<Color> playersColorConfig = register(new ColorConfig("PlayersColor", "The render color for players", new Color(200, 60, 60), false, () -> playersConfig.getValue()));
    Config<Boolean> friendsConfig = register(new BooleanConfig("Friends", "Render tracers to players that are friended", false, () -> playersConfig.getValue()));
    Config<Boolean> invisiblesConfig = register(new BooleanConfig("Invisibles", "Render tracers to invisible entities", false));
    Config<Color> invisiblesColorConfig = register(new ColorConfig("InvisiblesColor", "The render color for invisibles", new Color(200, 100, 0), false, () -> invisiblesConfig.getValue()));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render tracers to monsters", false));
    Config<Color> monstersColorConfig = register(new ColorConfig("MonstersColor", "The render color for monsters", new Color(200, 60, 60), false, () -> monstersConfig.getValue()));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render tracers to animals", false));
    Config<Color> animalsColorConfig = register(new ColorConfig("AnimalsColor", "The render color for animals", new Color(0, 200, 0), false, () -> animalsConfig.getValue()));
    Config<Boolean> vehiclesConfig = register(new BooleanConfig("Vehicles", "Render tracers to vehicles", false));
    Config<Color> vehiclesColorConfig = register(new ColorConfig("VehiclesColor", "The render color for vehicles", new Color(200, 100, 0), false, () -> vehiclesConfig.getValue()));
    Config<Boolean> itemsConfig = register(new BooleanConfig("Items", "Render tracers to items", false));
    Config<Color> itemsColorConfig = register(new ColorConfig("ItemsColor", "The render color for items", new Color(255, 255, 255), false, () -> itemsConfig.getValue()));

    public TracersModule()
    {
        super("Tracers", "Draws a tracer to all entities in render distance", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.player == null || mc.getCameraEntity() == null || !(mc.getCameraEntity() instanceof PlayerEntity playerEntity) || mc.options.hudHidden)
        {
            return;
        }
        MatrixStack matrixStack = new MatrixStack();
        double d = mc.options.getFov().getValue();
        matrixStack.multiplyPositionMatrix(mc.gameRenderer.getBasicProjectionMatrix(d));
        Matrix4f prevProjectionMatrix = RenderSystem.getProjectionMatrix();
        RenderSystem.setProjectionMatrix(matrixStack.peek().getPositionMatrix(), VertexSorter.BY_DISTANCE);
        RenderBuffers.preRender();
        Vec3d playerPos = Interpolation.getRenderPosition(playerEntity, event.getTickDelta());
        // interp on camera y pos
        Camera camera = mc.gameRenderer.getCamera();
        double eyeHeight = MathHelper.lerp(event.getTickDelta(), ((AccessorCamera) camera).getLastCameraY(), ((AccessorCamera) camera).getCameraY());
        double x1 = playerEntity.getX() - playerPos.getX();
        double y1 = playerEntity.getY() - playerPos.getY() + eyeHeight;
        double z1 = playerEntity.getZ() - playerPos.getZ();
        float pitch = playerEntity.getPitch();
        float yaw = playerEntity.getYaw();
        if (FreecamModule.getInstance().isEnabled())
        {
            Vec3d pos1 = FreecamModule.getInstance().getCameraPosition();
            Vec3d pos2 = Interpolation.getRenderPosition(pos1, FreecamModule.getInstance().getLastCameraPosition(), event.getTickDelta());
            float rotations[] = FreecamModule.getInstance().getCameraRotations();
            x1 = pos1.x - pos2.x;
            y1 = pos1.y - pos2.y;
            z1 = pos1.z - pos2.z;
            yaw = rotations[0];
            pitch = rotations[1];
        }
        Vec3d pos = new Vec3d(0.0, 0.0, 1.0)
                .rotateX(-(float) Math.toRadians(pitch))
                .rotateY(-(float) Math.toRadians(yaw))
                .add(new Vec3d(x1, y1, z1));
        for (Entity entity : mc.world.getEntities())
        {
            boolean shouldDraw = switch (modeConfig.getValue())
            {
                case NORMAL -> true;
                case ON_SCREEN -> RenderManager.isFrustumVisible(entity.getBoundingBox());
                case OFF_SCREEN -> !RenderManager.isFrustumVisible(entity.getBoundingBox());
            };
            if (entity == null || !entity.isAlive() || entity == mc.player || !shouldDraw || entity instanceof PlayerEntity && Managers.SOCIAL.isFriend(entity.getDisplayName()) && !friendsConfig.getValue())
            {
                continue;
            }
            Color color = getTracerColor(entity);
            if (color != null)
            {
                Vec3d entityPos = Interpolation.getRenderPosition(entity, event.getTickDelta());
                double x2 = entity.getX() - entityPos.getX();
                double y2 = entity.getY() - entityPos.getY();
                double z2 = entity.getZ() - entityPos.getZ();
                RenderManager.renderLine(event.getMatrices(), pos, new Vec3d(x2, y2, z2).add(0.0, getTargetY(entity), 0.0), widthConfig.getValue(), color.getRGB());
            }
        }
        RenderBuffers.postRender();
        RenderSystem.setProjectionMatrix(prevProjectionMatrix, VertexSorter.BY_DISTANCE);
    }

    private Color getTracerColor(Entity entity)
    {
        if (entity.isInvisible() && invisiblesConfig.getValue())
        {
            return invisiblesColorConfig.getValue();
        }
        else if (entity instanceof PlayerEntity player && playersConfig.getValue())
        {
            if (Managers.SOCIAL.isFriend(player.getName()))
            {
                return SocialsModule.getInstance().getFriendColor();
            }
            return playersColorConfig.getValue();
        }
        else if (EntityUtil.isMonster(entity) && monstersConfig.getValue())
        {
            return monstersColorConfig.getValue();
        }
        else if ((EntityUtil.isPassive(entity) || EntityUtil.isNeutral(entity))
                && animalsConfig.getValue())
        {
            return animalsColorConfig.getValue();
        }
        else if (EntityUtil.isVehicle(entity) && vehiclesConfig.getValue())
        {
            return vehiclesColorConfig.getValue();
        }
        else if (entity instanceof ItemEntity && itemsConfig.getValue())
        {
            return itemsColorConfig.getValue();
        }
        return null;
    }

    private double getTargetY(Entity entity)
    {
        return switch (targetConfig.getValue())
        {
            case FEET -> 0.0;
            case TORSO -> entity.getHeight() / 2.0;
            case HEAD -> entity.getEyeHeight(entity.getPose());
        };
    }

    public enum RenderMode
    {
        ON_SCREEN,
        OFF_SCREEN,
        NORMAL
    }

    public enum Target
    {
        FEET,
        TORSO,
        HEAD
    }
}
