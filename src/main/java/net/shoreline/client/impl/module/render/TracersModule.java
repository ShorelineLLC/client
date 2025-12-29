package net.shoreline.client.impl.module.render;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.imixin.ICamera;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.render.Interpolation;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.Optional;

public class TracersModule extends Toggleable
{
    Config<Boolean> onscreenConfig = new BooleanConfig.Builder("OnScreen")
            .setDescription("Shows tracers for visible entities")
            .setDefaultValue(false).build();
    Config<Boolean> playersConfig = new BooleanConfig.Builder("Players")
            .setDescription("Draws a line to other players")
            .setDefaultValue(true).build();
    Config<Color> playerColor = new ColorConfig.Builder("PlayersColor")
            .setGlobalColor().setDescription("The color for players")
            .setVisible(() -> playersConfig.getValue()).build();
    Config<Boolean> passiveConfig = new BooleanConfig.Builder("Passive")
            .setDescription("Draws a line to passives")
            .setDefaultValue(true).build();
    Config<Color> passivesColor = new ColorConfig.Builder("PassivesColor")
            .setGlobalColor().setDescription("The color for passives")
            .setVisible(() -> passiveConfig.getValue()).build();
    Config<Boolean> hostilesConfig = new BooleanConfig.Builder("Hostiles")
            .setDescription("Draws a line to hostiles")
            .setDefaultValue(true).build();
    Config<Color> hostilesColor = new ColorConfig.Builder("HostilesColor")
            .setGlobalColor().setDescription("The color for hostiles")
            .setVisible(() -> hostilesConfig.getValue()).build();
    Config<Boolean> crystalsConfig = new BooleanConfig.Builder("Crystals")
            .setDescription("Draws a line to crystals")
            .setDefaultValue(true).build();
    Config<Color> crystalsColor = new ColorConfig.Builder("CrystalsColor")
            .setGlobalColor().setDescription("The color for crystals")
            .setVisible(() -> crystalsConfig.getValue()).build();
    Config<Boolean> itemsConfig = new BooleanConfig.Builder("Items")
            .setDescription("Draws a line to items")
            .setDefaultValue(true).build();
    Config<Color> itemsColor = new ColorConfig.Builder("ItemsColor")
            .setGlobalColor().setDescription("The color for items")
            .setVisible(() -> itemsConfig.getValue()).build();

    public TracersModule()
    {
        super("Tracers", "Draws a line to nearby entities", GuiCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (mc.getCameraEntity() == null || !(mc.getCameraEntity() instanceof PlayerEntity playerEntity) || mc.options.hudHidden)
        {
            return;
        }

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d playerPos = Interpolation.getRenderPosition(playerEntity, event.getTickDelta());
        double eyeHeight = MathHelper.lerp(event.getTickDelta(), ((ICamera) camera).getLastCameraY(), ((ICamera) camera).getCameraY());
        double x1 = playerPos.getX();
        double y1 = playerPos.getY() + eyeHeight;
        double z1 = playerPos.getZ();
        float pitch = playerEntity.getPitch();
        float yaw = playerEntity.getYaw();

        if (FreecamModule.INSTANCE.isEnabled())
        {
            Vec3d pos1 = FreecamModule.INSTANCE.getPosition();
            Vec3d pos2 = Interpolation.getRenderPosition(pos1, FreecamModule.INSTANCE.getLastPosition(), event.getTickDelta());
            x1 = pos1.x - pos2.x;
            y1 = pos1.y - pos2.y;
            z1 = pos1.z - pos2.z;
            yaw = FreecamModule.INSTANCE.getYaw();
            pitch = FreecamModule.INSTANCE.getPitch();
        }

        Vec3d pos = new Vec3d(0.0, 0.0, 1.0)
                .rotateX(-(float) Math.toRadians(pitch))
                .rotateY(-(float) Math.toRadians(yaw))
                .add(x1, y1, z1);

        for (Entity entity : mc.world.getEntities())
        {
            if (entity == null || !entity.isAlive() || entity == mc.player)
            {
                continue;
            }

            if (!onscreenConfig.getValue() && Managers.RENDER.isVisible(entity.getBoundingBox()))
            {
                continue;
            }

            Optional<Color> color = getTracerColor(entity);
            if (color.isPresent())
            {
                Vec3d entityPos = Interpolation.getRenderPosition(entity, event.getTickDelta());
                Managers.RENDER.renderLine(event.getMatrixStack(), pos, entityPos, color.get().getRGB());
            }
        }
    }

    private Optional<Color> getTracerColor(Entity entity)
    {
        if (entity instanceof PlayerEntity player && playersConfig.getValue())
        {
            return Optional.of(Managers.SOCIAL.isFriend(player) ? SocialsModule.INSTANCE.getFriendsColor() : playerColor.getValue());
        } else if (EntityUtil.isHostile(entity) && hostilesConfig.getValue())
        {
            return Optional.of(hostilesColor.getValue());
        } else if (EntityUtil.isPassive(entity) && passiveConfig.getValue())
        {
            return Optional.of(passivesColor.getValue());
        } else if (entity instanceof EndCrystalEntity && crystalsConfig.getValue())
        {
            return Optional.of(crystalsColor.getValue());
        } else if (entity instanceof ItemEntity && itemsConfig.getValue())
        {
            return Optional.of(itemsColor.getValue());
        }

        return Optional.empty();
    }
}
