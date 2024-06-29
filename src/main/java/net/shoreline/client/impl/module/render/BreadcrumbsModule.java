package net.shoreline.client.impl.module.render;

import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linus
 * @since 1.0
 */
public class BreadcrumbsModule extends ToggleModule
{
    private final List<TimedPosition> positions = new CopyOnWriteArrayList<>();
    Config<Boolean> infiniteConfig = register(new BooleanConfig("Infinite", "Renders breadcrumbs for all positions since toggle", true));
    Config<Float> maxTimeConfig = register(new NumberConfig<>("MaxPosition", "The maximum time for a given position", 1.0f, 2.0f, 20.0f));
    Config<Boolean> fadeConfig = register(new BooleanConfig("Fade", "Fades the line render", false, () -> !infiniteConfig.getValue()));

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
        positions.add(new TimedPosition(new Vec3d(mc.player.getX(), mc.player.getBoundingBox().minY, mc.player.getZ()), System.currentTimeMillis()));
        if (!infiniteConfig.getValue())
        {
            positions.removeIf(timedPosition -> System.currentTimeMillis() - timedPosition.time() > maxTimeConfig.getValue() * 1000.0f);
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        event.getMatrices().push();
        RenderBuffers.preRender();
        RenderBuffers.LINES.begin(event.getMatrices());
        for (int i = 0; i < positions.size(); i++)
        {
            if (fadeConfig.getValue() && !infiniteConfig.getValue())
            {
                RenderBuffers.LINES.color(ColorsModule.getInstance().getRGB((int) (((float) i / positions.size()) * 255.0f)));
            }
            else
            {
                RenderBuffers.LINES.color(ColorsModule.getInstance().getRGB());
            }
            if (i > 1)
            {
                Vec3d vec3d = positions.get(i - 1).pos();
                RenderBuffers.LINES.vertex(vec3d.x, vec3d.y, vec3d.z);
            }
            Vec3d vec3d = positions.get(i).pos();
            RenderBuffers.LINES.vertex(vec3d.x, vec3d.y, vec3d.z);
        }
        RenderBuffers.LINES.end();
        RenderBuffers.postRender();
        event.getMatrices().pop();
    }

    private record TimedPosition(Vec3d pos, long time) {}
}