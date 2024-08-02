package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
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

/**
 *
 * @see ChamsModelRenderer
 */
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
        float n;
        Direction direction;
        event.matrixStack.push();
        event.model.handSwingProgress = event.entity.getHandSwingProgress(event.g);
        event.model.riding = event.entity.hasVehicle();
        event.model.child = event.entity.isBaby();
        float h = MathHelper.lerpAngleDegrees(event.g, event.entity.prevBodyYaw, event.entity.bodyYaw);
        float j = MathHelper.lerpAngleDegrees(event.g, event.entity.prevHeadYaw, event.entity.headYaw);
        float k = j - h;
        if (event.entity.hasVehicle() && event.entity.getVehicle() instanceof LivingEntity livingEntity2)
        {
            h = MathHelper.lerpAngleDegrees(event.g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
            k = j - h;
            float l = MathHelper.wrapDegrees(k);
            if (l < -85.0f)
            {
                l = -85.0f;
            }
            if (l >= 85.0f)
            {
                l = 85.0f;
            }
            h = j - l;
            if (l * l > 2500.0f)
            {
                h += l * 0.2f;
            }
            k = j - h;
        }
        float m = MathHelper.lerp(event.g, event.entity.prevPitch, event.entity.getPitch());
        if (LivingEntityRenderer.shouldFlipUpsideDown(event.entity))
        {
            m *= -1.0f;
            k *= -1.0f;
        }
        if (event.entity.isInPose(EntityPose.SLEEPING) && (direction = event.entity.getSleepingDirection()) != null)
        {
            n = event.entity.getEyeHeight(EntityPose.STANDING) - 0.1f;
            event.matrixStack.translate((float) (-direction.getOffsetX()) * n, 0.0f, (float) (-direction.getOffsetZ()) * n);
        }
        float l = getAnimationProgress(event.entity, event.g);
        if (event.entity instanceof PlayerEntity)
        {
            setupPlayerTransforms((AbstractClientPlayerEntity) event.entity, event.matrixStack, l, h, event.g);
        }
        else
        {
            setupTransforms(event.entity, event.matrixStack, l, h, event.g);
        }
        event.matrixStack.scale(-1.0f, -1.0f, 1.0f);
        event.matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        event.matrixStack.translate(0.0f, -1.501f, 0.0f);
        n = 0.0f;
        float o = 0.0f;
        if (!event.entity.hasVehicle() && event.entity.isAlive())
        {
            n = event.entity.limbAnimator.getSpeed(event.g);
            o = event.entity.limbAnimator.getPos(event.g);
            if (event.entity.isBaby())
            {
                o *= 3.0f;
            }
            if (n > 1.0f)
            {
                n = 1.0f;
            }
        }
        event.model.animateModel(event.entity, o, n, event.g);
        event.model.setAngles(event.entity, o, n, l, k, m);
        if (!event.entity.isSpectator())
        {
            for (Object featureRenderer : event.features)
            {
                ((FeatureRenderer) featureRenderer).render(event.matrixStack, event.vertexConsumerProvider, event.i,
                        event.entity, o, n, event.g, l, k, m);
            }
        }
        event.matrixStack.pop();
    }

    protected void setupPlayerTransforms(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h)
    {
        float i = abstractClientPlayerEntity.getLeaningPitch(h);
        float j = abstractClientPlayerEntity.getPitch(h);
        if (abstractClientPlayerEntity.isFallFlying())
        {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            float k = (float) abstractClientPlayerEntity.getRoll() + h;
            float l = MathHelper.clamp(k * k / 100.0f, 0.0f, 1.0f);
            if (!abstractClientPlayerEntity.isUsingRiptide())
            {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(l * (-90.0f - j)));
            }
            Vec3d vec3d = abstractClientPlayerEntity.getRotationVec(h);
            Vec3d vec3d2 = abstractClientPlayerEntity.lerpVelocity(h);
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0)
            {
                double m = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double n = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) (Math.signum(n) * Math.acos(m))));
            }
        }
        else if (i > 0.0f)
        {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            float k = abstractClientPlayerEntity.isTouchingWater() ? -90.0f - j : -90.0f;
            float l = MathHelper.lerp(i, 0.0f, k);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(l));
            if (abstractClientPlayerEntity.isInSwimmingPose())
            {
                matrixStack.translate(0.0f, -1.0f, 0.3f);
            }
        }
        else
        {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
        }
    }

    protected void setupTransforms(LivingEntity entity, MatrixStack matrices, float animationProgress,
                                   float bodyYaw, float tickDelta)
    {
        if (entity.isFrozen())
        {
            bodyYaw += (float) (Math.cos((double) entity.age * 3.25) * Math.PI * (double) 0.4f);
        }
        if (!entity.isInPose(EntityPose.SLEEPING))
        {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
        }
        if (entity.deathTime > 0)
        {
            float f = ((float) entity.deathTime + tickDelta - 1.0f) / 20.0f * 1.6f;
            if ((f = MathHelper.sqrt(f)) > 1.0f)
            {
                f = 1.0f;
            }
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 90.0f));
        }
        else if (entity.isUsingRiptide())
        {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f - entity.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float) entity.age + tickDelta) * -75.0f));
        }
        else if (entity.isInPose(EntityPose.SLEEPING))
        {
            Direction direction = entity.getSleepingDirection();
            float g = direction != null ? getYaw(direction) : bodyYaw;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0f));
        }
        else if (LivingEntityRenderer.shouldFlipUpsideDown(entity))
        {
            matrices.translate(0.0f, entity.getHeight() + 0.1f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        }
    }

    private float getYaw(Direction direction)
    {
        switch (direction)
        {
            case SOUTH:
            {
                return 90.0f;
            }
            case WEST:
            {
                return 0.0f;
            }
            case NORTH:
            {
                return 270.0f;
            }
            case EAST:
            {
                return 180.0f;
            }
        }
        return 0.0f;
    }

    private float getAnimationProgress(LivingEntity entity, float f)
    {
        if (entity instanceof SquidEntity)
        {
            return MathHelper.lerp(f, ((SquidEntity) entity).prevTentacleAngle, ((SquidEntity) entity).tentacleAngle);
        }
        return entity instanceof WolfEntity wolf ? wolf.getTailAngle() : entity.age + f;
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