package net.shoreline.client.impl.module.render;

import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.impl.TargetingModule;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Interpolation;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ESPModule extends TargetingModule
{
    public Config<Boolean> items = new BooleanConfig.Builder("Items")
            .setDefaultValue(true).setDescription("Target Items").build();
    public Config<Void> targetConfig = new ConfigGroup.Builder("Target")
            .addAll(targetPlayers, targetHostiles, targetPassives, items).build();
    public Config<Boolean> fillConfig = new BooleanConfig.Builder("Fill")
            .setDescription("Fills in the box")
            .setDefaultValue(true).build();
    public Config<Float> range = new NumberConfig.Builder<Float>("Range")
            .setMin(0.f).setDefaultValue(30.0f).setMax(250.f)
            .setDescription("If entity is within this range we render them").build();
    public Config<Color> color = new ColorConfig.Builder("Color")
            .setRgb(0xFFFFFFFF).setTransparency(true).build();

    private final Map<Entity, FadingBox> renderList = new HashMap<>();

    public ESPModule()
    {
        super("ESP", "Highlights entities", GuiCategory.RENDER);
    }

    @EventListener
    public void onRender(RenderWorldEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        Set<Entity> current = new HashSet<>();
        for (Entity entity : mc.world.getEntities())
        {
            if (!isValid(entity))
            {
                continue;
            }

            current.add(entity);
            if (!renderList.containsKey(entity))
            {
                FadingBox box = new FadingBox();
                box.getAnimation().setState(true);
                renderList.put(entity, box);
            }
        }

        renderList.keySet().removeIf(e -> !current.contains(e));
        for (Map.Entry<Entity, FadingBox> entry : renderList.entrySet())
        {
            Entity entity = entry.getKey();
            if (!Managers.RENDER.isVisible(entity.getBoundingBox()))
            {
                continue;
            }

            entry.getValue().setEntity(entity, event.getTickDelta());
            entry.getValue().render(event.getMatrixStack(), entity.isAlive(), color.getValue());
        }
    }

    @Override
    public boolean isValid(Entity entity)
    {
        if (MathHelper.square(range.getValue()) < entity.squaredDistanceTo(mc.player))
        {
            return false;
        }

        if (entity instanceof ExperienceBottleEntity
                || entity instanceof ItemEntity)
        {
            return items.getValue();
        }

        return super.isValid(entity);
    }

    @Getter
    private class FadingBox
    {
        private final Animation animation = new Animation(false, 500, Easing.LINEAR);
        private Box bb;

        public void setEntity(Entity entity, float tickDelta)
        {
            if (entity == null)
            {
                return;
            }

            Vec3d vec = Interpolation.getRenderPosition(entity, tickDelta);
            bb = entity.getDimensions(entity.getPose()).getBoxAt(vec);
        }

        public void render(MatrixStack matrices, boolean shouldRender, Color color)
        {
            animation.setState(shouldRender);
            if (bb == null || animation.getFactor() < 0.01)
            {
                bb = null;
                return;
            }

            if (fillConfig.getValue())
            {
                Managers.RENDER.renderBox(matrices, bb, ColorUtil.withTransparency(color.getRGB(), (float) ((color.getAlpha() / 255f) * animation.getFactor())));
            }

            Managers.RENDER.renderBoundingBox(matrices, bb, ColorUtil.withTransparency(new Color(color.getRGB(), false).getRGB(), (float) (0.8f * animation.getFactor())));
        }
    }
}
