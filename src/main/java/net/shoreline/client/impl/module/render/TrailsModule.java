package net.shoreline.client.impl.module.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Interpolation;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TrailsModule extends ListeningToggleable
{
    Config<Float> trailTime = new NumberConfig.Builder<Float>("MaxTrail")
            .setMin(0.1f).setMax(5.0f).setDefaultValue(1.0f).setFormat("s")
            .setDescription("The time before removing a trail").build();
    Config<Boolean> playersConfig = new BooleanConfig.Builder("Players")
            .setDescription("Shows trails for players")
            .setDefaultValue(true).build();
    Config<Boolean> selfConfig = new BooleanConfig.Builder("Self")
            .setDescription("Shows trails for self")
            .setDefaultValue(true).build();
    Config<Boolean> pearlsConfig = new BooleanConfig.Builder("Pearls")
            .setDescription("Shows trails for pearls")
            .setDefaultValue(false).build();
    Config<Boolean> xpConfig = new BooleanConfig.Builder("XP")
            .setDescription("Shows trails for xp bottles")
            .setDefaultValue(false).build();
    Config<Boolean> arrowsConfig = new BooleanConfig.Builder("Arrows")
            .setDescription("Shows trails for arrows")
            .setDefaultValue(false).build();

    private final ConcurrentMap<Integer, List<TimedPosition>> positions = new ConcurrentHashMap<>();

    public TrailsModule()
    {
        super("Trails", new String[] {"Breadcrumbs"}, "Shows entity trails", GuiCategory.RENDER);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        for (Entity entity : mc.world.getEntities())
        {
            if (!shouldRenderTrail(entity))
            {
                continue;
            }

            Vec3d pos = Interpolation.getRenderPosition(entity, mc.getRenderTickCounter().getTickDelta(true));
            if (positions.containsKey(entity.getId()))
            {
                positions.get(entity.getId()).add(new TimedPosition(pos, System.currentTimeMillis()));
            } else
            {
                List<TimedPosition> timedPositions = new CopyOnWriteArrayList<>();
                timedPositions.add(new TimedPosition(pos, System.currentTimeMillis()));
                positions.put(entity.getId(), timedPositions);
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        for (Map.Entry<Integer, List<TimedPosition>> entry : positions.entrySet())
        {
            List<TimedPosition> timedPositions = entry.getValue();
            for (int i = 0; i < timedPositions.size(); i++)
            {
                TimedPosition timedPosition = timedPositions.get(i);

                long timeSince = System.currentTimeMillis() - timedPosition.time();
                float factor = 1.0f - MathHelper.clamp(timeSince / (trailTime.getValue() * 1000.0f), 0.0f, 1.0f);
                int color = ColorUtil.withTransparency(ThemeModule.INSTANCE.getPrimaryColor(), factor);

                if (i > 1)
                {
                    Vec3d vec3d = timedPositions.get(i - 1).pos();
                    Vec3d vec3d2 = timedPosition.pos();
                    Managers.RENDER.renderLine(event.getMatrixStack(), vec3d, vec3d2, color);
                }
            }
        }
    }

    private boolean shouldRenderTrail(Entity entity)
    {
        if (entity instanceof PlayerEntity)
        {
            return entity != mc.player && playersConfig.getValue() || selfConfig.getValue();
        }

        return entity instanceof EnderPearlEntity && pearlsConfig.getValue()
                || entity instanceof ArrowEntity && arrowsConfig.getValue()
                || entity instanceof ExperienceBottleEntity && xpConfig.getValue();
    }

    private record TimedPosition(Vec3d pos, long time) {}
}
