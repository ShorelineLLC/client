package net.shoreline.client.impl.module.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.util.math.TrajectoryUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.List;

public class TrajectoriesModule extends RenderModule
{
    Config<Color> color = new ColorConfig.Builder("Color")
            .setTransparency(true).setDescription("The color for the gradient")
            .setDefaultValue(Color.GREEN).build();

    public TrajectoriesModule()
    {
        super("Trajectories", "Shows trajectories of held items", GuiCategory.RENDER);
    }

    @EventListener
    public void onRender(RenderWorldEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        for (Entity entity : mc.world.getEntities())
        {
            if (entity instanceof EnderPearlEntity enderPearlEntity)
            {
                if (!mc.world.getWorldBorder().contains(enderPearlEntity.getPos()))
                {
                    continue;
                }

                List<Vec3d> trajectory = TrajectoryUtil.getPearlTrajectory(entity, 300);
                if (trajectory.isEmpty())
                {
                    continue;
                }

                Vec3d lastPos = trajectory.getFirst();
                for (Vec3d pos : trajectory)
                {
                    Managers.RENDER.renderLine(event.getMatrixStack(), lastPos, pos, color.getValue().getRGB());
                    lastPos = pos;
                }
            }
        }
    }
}
