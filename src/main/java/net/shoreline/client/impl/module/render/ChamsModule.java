package net.shoreline.client.impl.module.render;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import net.minecraft.block.CaveVines;
import net.minecraft.block.CaveVinesHeadBlock;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModels;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.*;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.combat.TotemPopEvent;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.client.impl.imixin.ILimbAnimator;
import net.shoreline.client.impl.imixin.ILivingEntity;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.ChamsRenderer;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ChamsModule extends RenderModule
{
    private static ChamsModule INSTANCE;
    public Config<ChamsMode> mode = new EnumConfig.Builder<ChamsMode>("Mode")
            .setValues(ChamsMode.values())
            .setDefaultValue(ChamsMode.CHAMS).build();
    public Config<Float> range = new NumberConfig.Builder<Float>("Range")
            .setMin(0.f).setDefaultValue(30.0f).setMax(250.f)
            .setDescription("If entity is within this range we apply chams").build();
    public Config<Boolean> extraLayer = new BooleanConfig.Builder("ExtraLayer")
            .setDefaultValue(true).build();
    public Config<Boolean> renderPlayers = new BooleanConfig.Builder("Players")
            .setDescription("Render players").setDefaultValue(true).build();
    public Config<Boolean> renderHostiles = new BooleanConfig.Builder("Hostiles")
            .setDescription("Render hostiles").setDefaultValue(false).build();
    public Config<Boolean> renderPassives = new BooleanConfig.Builder("Passives")
            .setDescription("Render passives").setDefaultValue(false).build();
    public Config<Boolean> renderCrystals = new BooleanConfig.Builder("Crystals")
            .setDescription("Render crystals").setDefaultValue(false).build();
    public Config<Boolean> renderPops = new BooleanConfig.Builder("Pops")
            .setDescription("Render pops").setDefaultValue(false).build();
    public Config<Void> renderConfig = new ConfigGroup.Builder("Target")
            .addAll(renderPlayers, renderHostiles, renderPassives, renderCrystals).build();
    public Config<Boolean> throughWalls = new BooleanConfig.Builder("ThroughWalls")
            .setDefaultValue(true).build();
    public Config<Float> scale = new NumberConfig.Builder<Float>("Scale")
            .setMin(0.1f).setMax(2.0f).setDefaultValue(1.0f)
            .setVisible(() -> mode.getValue() == ChamsMode.SHINE).build();
    public Config<Float> speed = new NumberConfig.Builder<Float>("Speed")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(0.5f)
            .setVisible(() -> mode.getValue() == ChamsMode.SHINE).build();
    public Config<Boolean> model = new BooleanConfig.Builder("Model")
            .setVisible(() -> mode.getValue() == ChamsMode.SHINE)
            .setDefaultValue(false).build();
    public Config<Float> opacityConfig = new NumberConfig.Builder<Float>("Opacity")
            .setMin(0.0f).setMax(1.0f).setDefaultValue(1.0f)
            .setVisible(() -> mode.getValue() == ChamsMode.SHINE && model.getValue()
                    || mode.getValue() == ChamsMode.X_Q_Z).build();
    public Config<Color> color = new ColorConfig.Builder("Color")
            .setRgb(0xFFFFFFFF).setTransparency(true).build();

    private final Map<PopEntity, Animation> pops = new ConcurrentHashMap<>();

    public ChamsModule()
    {
        super("Chams", "Renders entity models through walls", GuiCategory.RENDER);
        INSTANCE = this;
    }

    @EventListener
    public void onPop(TotemPopEvent event)
    {
        if (!renderPops.getValue() || event.getEntity() == mc.player)
        {
            return;
        }

        pops.put(new PopEntity((PlayerEntity) event.getEntity()), new Animation(true, 500));
    }

    @EventListener
    public void onRenderWorld(RenderEntityWorldEvent.Post event)
    {
        for (Map.Entry<PopEntity, Animation> entry : pops.entrySet())
        {
            PopEntity entity    = entry.getKey();
            Animation animation = entry.getValue();
            animation.setState(false);

            ChamsRenderer.render(ChamsRenderer.BOTH, entity, event.getTickDelta(), ColorUtil.withTransparency(color.getValue(), 0.5f), (float) animation.getFactor());
        }

        pops.entrySet().removeIf(entry ->
                entry.getValue().getFactor() < 0.01);
    }

    public float getOpacity()
    {
        if (mode.getValue() == ChamsMode.X_Q_Z)
        {
            return opacityConfig.getValue();
        }
        else if (mode.getValue() == ChamsMode.SHINE && model.getValue())
        {
            return opacityConfig.getValue();
        }

        return 0.0f;
    }

    public boolean isValid(Entity entity)
    {
        if (!Managers.RENDER.isVisible(entity.getBoundingBox())
                || MathHelper.square(range.getValue()) < entity.squaredDistanceTo(getCameraPos()))
        {
            return false;
        }

        return switch (entity)
        {
            case PlayerEntity player when renderPlayers.getValue() -> true;
            case Monster monster when renderHostiles.getValue() -> true;
            case AnimalEntity animalEntity when renderPassives.getValue() -> true;
            default -> entity instanceof EndCrystalEntity && renderCrystals.getValue();
        };
    }

    public static ChamsModule getInstance()
    {
        return INSTANCE;
    }

    public Color getColor(Entity entity)
    {
        return SocialsModule.INSTANCE.getEntityColor(entity, color.getValue());
    }

    public float getSpeed()
    {
        return speed.getValue();
    }

    public float getScale()
    {
        return scale.getValue();
    }

    public static class PopEntity extends OtherClientPlayerEntity
    {
        public PopEntity(PlayerEntity player)
        {
            super(mc.world, new GameProfile(UUID.fromString("bee73d65-2fcd-4fbd-a259-468e65274338"), player.getName().getString()));
            this.copyPositionAndRotation(player);
            this.prevYaw = getYaw();
            this.prevPitch = getPitch();
            this.headYaw = player.getHeadYaw();
            this.prevHeadYaw = headYaw;
            this.bodyYaw = player.bodyYaw;
            this.prevBodyYaw = bodyYaw;
            this.getAttributes().setFrom(player.getAttributes());
            this.setPose(player.getPose());
            this.limbAnimator.setSpeed(player.limbAnimator.getSpeed());
            ((ILimbAnimator) this.limbAnimator).setPos(player.limbAnimator.getPos());
            ((ILivingEntity) this).setLeaningPitch(player.getLeaningPitch(1));
            ((ILivingEntity) this).setLastLeaningPitch(player.getLeaningPitch(1));
        }
    }

    @Getter
    public enum ChamsMode
    {
        NONE(ChamsRenderer.NONE),
        X_Q_Z(ChamsRenderer.NONE),
        CHAMS(ChamsRenderer.CHAMS),
        WIRECHAMS(ChamsRenderer.BOTH),
        SHINE(ChamsRenderer.NONE);

        public final ChamsRenderer renderer;

        ChamsMode(ChamsRenderer renderer)
        {
            this.renderer = renderer;
        }
    }
}
