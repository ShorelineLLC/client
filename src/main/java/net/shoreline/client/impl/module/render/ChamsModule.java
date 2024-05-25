package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.SquidEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.render.entity.RenderCrystalEvent;
import net.shoreline.client.impl.event.render.entity.RenderEntityEvent;
import net.shoreline.client.impl.event.render.item.RenderArmEvent;
import net.shoreline.client.util.world.EntityUtil;
import org.joml.Quaternionf;

import java.awt.*;

/**
 * @author linus
 * @since 1.0
 */
public class ChamsModule extends ToggleModule {

    Config<ChamsMode> modeConfig = register(new EnumConfig<>("Mode", "The rendering mode for the chams", ChamsMode.NORMAL, ChamsMode.values()));
    Config<Boolean> handsConfig = register(new BooleanConfig("Hands", "Render chams on first-person hands", true));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Render chams on the player", true));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render chams on other players", true));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render chams on monsters", true));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render chams on animals", true));
    Config<Boolean> otherConfig = register(new BooleanConfig("Others", "Render chams on crystals", true));
    Config<Boolean> invisiblesConfig = register(new BooleanConfig("Invisibles", "Render chams on invisible entities", true));
    Config<Color> colorConfig = register(new ColorConfig("Color", "The color of the chams", new Color(255, 0, 0, 60)));

    private static final float SINE_45_DEGREES = (float) Math.sin(0.7853981633974483);

    public ChamsModule() {
        super("Chams", "Renders entity models through walls", ModuleCategory.RENDER);
    }

    private static float getYaw(Direction direction) {
        switch (direction) {
            case SOUTH: {
                return 90.0f;
            }
            case WEST: {
                return 0.0f;
            }
            case NORTH: {
                return 270.0f;
            }
            case EAST: {
                return 180.0f;
            }
        }
        return 0.0f;
    }

    @EventListener
    public void onRenderEntity(RenderEntityEvent event) {
        if (!checkChams(event.entity)) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer = tessellator.getBuffer();
        // BufferBuilder vertexConsumer = (BufferBuilder) event.vertexConsumerProvider.getBuffer(event.layer);
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.lineWidth(2.0f);
        vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        Color color = colorConfig.getValue();
        float n;
        Direction direction;
        event.matrixStack.push();
        RenderSystem.setShaderColor(color.getRed() / 255.0f, color.getGreen() / 255.0f,
                color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        event.model.handSwingProgress = event.entity.getHandSwingProgress(event.g);
        event.model.riding = ((Entity) event.entity).hasVehicle();
        event.model.child = ((LivingEntity) event.entity).isBaby();
        float h = MathHelper.lerpAngleDegrees(event.g, ((LivingEntity) event.entity).prevBodyYaw, ((LivingEntity) event.entity).bodyYaw);
        float j = MathHelper.lerpAngleDegrees(event.g, ((LivingEntity) event.entity).prevHeadYaw, ((LivingEntity) event.entity).headYaw);
        float k = j - h;
        if (((Entity) event.entity).hasVehicle() && ((Entity) event.entity).getVehicle() instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity) ((Entity) event.entity).getVehicle();
            h = MathHelper.lerpAngleDegrees(event.g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
            k = j - h;
            float l = MathHelper.wrapDegrees(k);
            if (l < -85.0f) {
                l = -85.0f;
            }
            if (l >= 85.0f) {
                l = 85.0f;
            }
            h = j - l;
            if (l * l > 2500.0f) {
                h += l * 0.2f;
            }
            k = j - h;
        }
        float m = MathHelper.lerp(event.g, ((LivingEntity) event.entity).prevPitch, ((Entity) event.entity).getPitch());
        if (LivingEntityRenderer.shouldFlipUpsideDown(event.entity)) {
            m *= -1.0f;
            k *= -1.0f;
        }
        if (((Entity) event.entity).isInPose(EntityPose.SLEEPING) && (direction = ((LivingEntity) event.entity).getSleepingDirection()) != null) {
            n = ((Entity) event.entity).getEyeHeight(EntityPose.STANDING) - 0.1f;
            event.matrixStack.translate((float) (-direction.getOffsetX()) * n, 0.0f, (float) (-direction.getOffsetZ()) * n);
        }
        float l = getAnimationProgress(event.entity, event.g);
        if (event.entity instanceof PlayerEntity) {
            setupPlayerTransforms((AbstractClientPlayerEntity) event.entity, event.matrixStack, l, h, event.g);
        } else {
            setupTransforms(event.entity, event.matrixStack, l, h, event.g);
        }
        event.matrixStack.scale(-1.0f, -1.0f, 1.0f);
        event.matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        event.matrixStack.translate(0.0f, -1.501f, 0.0f);
        n = 0.0f;
        float o = 0.0f;
        if (!((Entity) event.entity).hasVehicle() && ((LivingEntity) event.entity).isAlive()) {
            n = ((LivingEntity) event.entity).limbAnimator.getSpeed(event.g);
            o = ((LivingEntity) event.entity).limbAnimator.getPos(event.g);
            if (((LivingEntity) event.entity).isBaby()) {
                o *= 3.0f;
            }
            if (n > 1.0f) {
                n = 1.0f;
            }
        }
        event.model.animateModel(event.entity, o, n, event.g);
        event.model.setAngles(event.entity, o, n, l, k, m);
        boolean bl = !event.entity.isInvisible();
        boolean bl2 = !bl && !((Entity) event.entity).isInvisibleTo(mc.player);
        int p = LivingEntityRenderer.getOverlay(event.entity, 0);
        event.model.render(event.matrixStack, vertexConsumer, event.i, p, 1.0f, 1.0f, 1.0f, 1.0f);
        tessellator.draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        if (!((Entity) event.entity).isSpectator()) {
            for (Object featureRenderer : event.features) {
                ((FeatureRenderer) featureRenderer).render(event.matrixStack, event.vertexConsumerProvider, event.i,
                        event.entity, o, n, event.g, l, k, m);
            }
        }
        event.matrixStack.pop();
        event.cancel();
    }

    protected void setupPlayerTransforms(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h) {
        float i = abstractClientPlayerEntity.getLeaningPitch(h);
        float j = abstractClientPlayerEntity.getPitch(h);
        if (abstractClientPlayerEntity.isFallFlying()) {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            float k = (float)abstractClientPlayerEntity.getRoll() + h;
            float l = MathHelper.clamp(k * k / 100.0f, 0.0f, 1.0f);
            if (!abstractClientPlayerEntity.isUsingRiptide()) {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(l * (-90.0f - j)));
            }
            Vec3d vec3d = abstractClientPlayerEntity.getRotationVec(h);
            Vec3d vec3d2 = abstractClientPlayerEntity.lerpVelocity(h);
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double m = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double n = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)(Math.signum(n) * Math.acos(m))));
            }
        } else if (i > 0.0f) {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            float k = abstractClientPlayerEntity.isTouchingWater() ? -90.0f - j : -90.0f;
            float l = MathHelper.lerp(i, 0.0f, k);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(l));
            if (abstractClientPlayerEntity.isInSwimmingPose()) {
                matrixStack.translate(0.0f, -1.0f, 0.3f);
            }
        } else {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
        }
    }

    protected void setupTransforms(LivingEntity entity, MatrixStack matrices, float animationProgress,
                                   float bodyYaw, float tickDelta) {
        if (entity.isFrozen()) {
            bodyYaw += (float) (Math.cos((double) ((LivingEntity) entity).age * 3.25) * Math.PI * (double) 0.4f);
        }
        if (!((Entity) entity).isInPose(EntityPose.SLEEPING)) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - bodyYaw));
        }
        if (((LivingEntity) entity).deathTime > 0) {
            float f = ((float) ((LivingEntity) entity).deathTime + tickDelta - 1.0f) / 20.0f * 1.6f;
            if ((f = MathHelper.sqrt(f)) > 1.0f) {
                f = 1.0f;
            }
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 90.0f));
        } else if (((LivingEntity) entity).isUsingRiptide()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f - ((Entity) entity).getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float) ((LivingEntity) entity).age + tickDelta) * -75.0f));
        } else if (((Entity) entity).isInPose(EntityPose.SLEEPING)) {
            Direction direction = ((LivingEntity) entity).getSleepingDirection();
            float g = direction != null ? getYaw(direction) : bodyYaw;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0f));
        } else if (LivingEntityRenderer.shouldFlipUpsideDown(entity)) {
            matrices.translate(0.0f, ((Entity) entity).getHeight() + 0.1f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        }
    }

    private float getAnimationProgress(LivingEntity entity, float f) {
        if (entity instanceof SquidEntity) {
            return MathHelper.lerp(f, ((SquidEntity)entity).prevTentacleAngle, ((SquidEntity)entity).tentacleAngle);
        }
        return entity instanceof WolfEntity wolf ? wolf.getTailAngle() : entity.age + f;
    }

    @EventListener
    public void onRenderCrystal(RenderCrystalEvent event) {
        if (!otherConfig.getValue()) {
            return;
        }
        RenderSystem.enableBlend();
        // RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        event.matrixStack.push();
        float h = EndCrystalEntityRenderer.getYOffset(event.endCrystalEntity, event.g);
        float j = ((float) event.endCrystalEntity.endCrystalAge + event.g) * 3.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.lineWidth(2.0f);
        vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        event.matrixStack.push();
        Color color = colorConfig.getValue();
        RenderSystem.setShaderColor(color.getRed() / 255.0f, color.getGreen() / 255.0f,
                color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        event.matrixStack.scale(2.0f, 2.0f, 2.0f);
        event.matrixStack.translate(0.0f, -0.5f, 0.0f);
        int k = OverlayTexture.DEFAULT_UV;
        event.matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        event.matrixStack.translate(0.0f, 1.5f + h / 2.0f, 0.0f);
        event.matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        event.frame.render(event.matrixStack, vertexConsumer, event.i, k);
        float l = 0.875f;
        event.matrixStack.scale(0.875f, 0.875f, 0.875f);
        event.matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        event.matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        event.frame.render(event.matrixStack, vertexConsumer, event.i, k);
        event.matrixStack.scale(0.875f, 0.875f, 0.875f);
        event.matrixStack.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
        event.matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        event.core.render(event.matrixStack, vertexConsumer, event.i, k);
        event.matrixStack.pop();
        event.matrixStack.pop();
        tessellator.draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        event.cancel();
    }

    @EventListener
    public void onRenderArm(RenderArmEvent event) {
        if (handsConfig.getValue()) {
            RenderSystem.enableBlend();
            // RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.defaultBlendFunc();
            // RenderSystem.disableCull();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexConsumer = tessellator.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionProgram);
            RenderSystem.lineWidth(2.0f);
            vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            event.matrices.push();
            Color color = colorConfig.getValue();
            RenderSystem.setShaderColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f,
                    MathHelper.clamp((color.getAlpha() + 40.0f) / 255.0f, 0.0f, 1.0f));
            boolean bl = event.arm != Arm.LEFT;
            float f = bl ? 1.0f : -1.0f;
            float g = MathHelper.sqrt(event.swingProgress);
            float h = -0.3f * MathHelper.sin(g * (float)Math.PI);
            float i = 0.4f * MathHelper.sin(g * ((float)Math.PI * 2));
            float j = -0.4f * MathHelper.sin(event.swingProgress * (float)Math.PI);
            event.matrices.translate(f * (h + 0.64000005f), i + -0.6f + event.equipProgress * -0.6f, j + -0.71999997f);
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 45.0f));
            float k = MathHelper.sin(event.swingProgress * event.swingProgress * (float)Math.PI);
            float l = MathHelper.sin(g * (float)Math.PI);
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * l * 70.0f));
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * k * -20.0f));
            event.matrices.translate(f * -1.0f, 3.6f, 3.5f);
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 120.0f));
            event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0f));
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -135.0f));
            event.matrices.translate(f * 5.6f, 0.0f, 0.0f);
            event.playerEntityRenderer.setModelPose(mc.player);
            event.playerEntityRenderer.getModel().handSwingProgress = 0.0f;
            event.playerEntityRenderer.getModel().sneaking = false;
            event.playerEntityRenderer.getModel().leaningPitch = 0.0f;
            event.playerEntityRenderer.getModel().setAngles(mc.player, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
            if (event.arm == Arm.RIGHT) {
                event.playerEntityRenderer.getModel().rightArm.pitch = 0.0f;
                event.playerEntityRenderer.getModel().rightArm.render(event.matrices, vertexConsumer, event.light, OverlayTexture.DEFAULT_UV);
                event.playerEntityRenderer.getModel().rightSleeve.pitch = 0.0f;
                event.playerEntityRenderer.getModel().rightSleeve.render(event.matrices, vertexConsumer, event.light, OverlayTexture.DEFAULT_UV);
            } else {
                event.playerEntityRenderer.getModel().leftArm.pitch = 0.0f;
                event.playerEntityRenderer.getModel().leftArm.render(event.matrices, vertexConsumer, event.light, OverlayTexture.DEFAULT_UV);
                event.playerEntityRenderer.getModel().leftSleeve.pitch = 0.0f;
                event.playerEntityRenderer.getModel().leftSleeve.render(event.matrices, vertexConsumer, event.light, OverlayTexture.DEFAULT_UV);
            }
            tessellator.draw();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            // RenderSystem.enableCull();
            event.matrices.pop();
            event.cancel();
        }
    }

    private boolean checkChams(LivingEntity entity) {
        if (entity instanceof PlayerEntity && playersConfig.getValue()) {
            return selfConfig.getValue() || entity != mc.player;
        }
        return (!entity.isInvisible() || invisiblesConfig.getValue())
                && (EntityUtil.isMonster(entity) && monstersConfig.getValue()
                || (EntityUtil.isNeutral(entity)
                || EntityUtil.isPassive(entity)) && animalsConfig.getValue());
    }

    public enum ChamsMode {
        NORMAL,
        // WIREFRAME
    }
}
