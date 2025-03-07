package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linus
 * @since 1.0
 */
public class BreadcrumbsModule extends ToggleModule
{
    private final Map<Integer, List<TimedPosition>> positions = new ConcurrentHashMap<>();
    Config<Boolean> infiniteConfig = register(new BooleanConfig("Infinite", "Renders breadcrumbs for all positions since toggle", true));
    Config<Float> maxTimeConfig = register(new NumberConfig<>("MaxPosition", "The maximum time for a given position", 1.0f, 2.0f, 20.0f));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 1000, 5000, () -> false));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the path", 1.0f, 1.0f, 5.0f));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Renders breadcrumbs on player", true));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Renders breadcrumbs on other players", false));
    Config<Boolean> pearlsConfig = register(new BooleanConfig("Pearls", "Renders breadcrumbs on thrown pearls", false));
    Config<Boolean> arrowsConfig = register(new BooleanConfig("Arrows", "Renders breadcrumbs on arrows", false));
    Config<Boolean> xpBottlesConfig = register(new BooleanConfig("XPBottles", "Renders breadcrumbs on thrown experience bottles", false));

    public BreadcrumbsModule()
    {
        super("Breadcrumbs", "Renders a line connecting all previous positions", ModuleCategory.RENDER);
    }

    @Override
    public void onDisable()
    {
        positions.clear();
    }

    @EventListener
    public void onPlayerUpdate(PlayerTickEvent event)
    {
        for (Entity entity : mc.world.getEntities())
        {
            if (!checkEntity(entity))
            {
                continue;
            }
            final Vec3d pos = Interpolation.getInterpolatedPosition(entity, mc.getRenderTickCounter().getTickDelta(true));
            if (positions.containsKey(entity.getId()))
            {
                positions.get(entity.getId()).add(new TimedPosition(pos, System.currentTimeMillis()));
            }
            else
            {
                List<TimedPosition> timedPositions = new CopyOnWriteArrayList<>();
                timedPositions.add(new TimedPosition(pos, System.currentTimeMillis()));
                positions.put(entity.getId(), timedPositions);
            }
        }
        if (!infiniteConfig.getValue())
        {
            for (Map.Entry<Integer, List<TimedPosition>> entry : positions.entrySet())
            {
                for (TimedPosition timedPosition : entry.getValue())
                {
                    if (System.currentTimeMillis() - timedPosition.time() > maxTimeConfig.getValue() * 1000.0f)
                    {
                        positions.get(entry.getKey()).remove(timedPosition);
                    }
                }
            }
        }
    }

    @EventListener
    public void onEntityDeath(RemoveEntityEvent event)
    {
        if (infiniteConfig.getValue())
        {
            positions.remove(event.getEntity().getId());
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        event.getMatrices().push();
        RenderBuffers.preRender();
        RenderSystem.lineWidth(widthConfig.getValue());
        RenderBuffers.LINES.begin(event.getMatrices());
        for (Map.Entry<Integer, List<TimedPosition>> entry : positions.entrySet())
        {
            List<TimedPosition> timedPositions = entry.getValue();
            for (int i = 0; i < timedPositions.size(); i++)
            {
                TimedPosition timedPosition = timedPositions.get(i);
                if (!infiniteConfig.getValue())
                {
                    float fade = 1.0f - MathHelper.clamp((System.currentTimeMillis() - timedPosition.time()) / (float) fadeTimeConfig.getValue(), 0.0f, 1.0f);
                    RenderBuffers.LINES.color(ColorsModule.getInstance().getRGB((int) (fade * 255.0f)));
                }
                else
                {
                    RenderBuffers.LINES.color(ColorsModule.getInstance().getRGB());
                }
                if (i > 1)
                {
                    Vec3d vec3d = timedPositions.get(i - 1).pos();
                    Vec3d vec3d2 = timedPosition.pos();
                    RenderBuffers.LINES.vertexLine(vec3d.x, vec3d.y, vec3d.z, vec3d2.x, vec3d2.y, vec3d2.z);
                }
            }
        }
        RenderBuffers.LINES.end();
        RenderBuffers.postRender();
        event.getMatrices().pop();
    }

    public boolean checkEntity(Entity entity)
    {
        if (entity instanceof PlayerEntity)
        {
            return playersConfig.getValue() || entity == mc.player && selfConfig.getValue();
        }
        return entity instanceof EnderPearlEntity && pearlsConfig.getValue()
                || entity instanceof ArrowEntity && arrowsConfig.getValue()
                || entity instanceof ExperienceBottleEntity && xpBottlesConfig.getValue();
    }

    private record TimedPosition(Vec3d pos, long time) {}
}