package net.shoreline.client.api.render.chams;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.model.StaticBipedEntityModel;
import net.shoreline.client.impl.module.misc.SwingModule;
import net.shoreline.client.impl.module.render.CrystalModelModule;
import net.shoreline.client.impl.module.render.FreecamModule;
import net.shoreline.client.init.Fonts;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorAnimalModel;
import net.shoreline.client.util.Globals;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import static net.minecraft.client.render.item.ItemRenderer.ITEM_ENCHANTMENT_GLINT;

public class ChamsModelRenderer implements Globals
{
    private static final float SINE_45_DEGREES = (float) Math.sin(0.7853981633974483);

    private static final MatrixStack matrices = new MatrixStack();
    private static final Vector4f pos1 = new Vector4f();
    private static final Vector4f pos2 = new Vector4f();
    private static final Vector4f pos3 = new Vector4f();
    private static final Vector4f pos4 = new Vector4f();

    public static void renderStaticPlayerModel(MatrixStack matrixStack, AbstractClientPlayerEntity entity, StaticBipedEntityModel playerModel, float tickDelta, int color, int lineColor, float lineWidth, boolean lines, boolean fill, boolean shine)
    {
        double offsetX = playerModel.getX();
        double offsetY = playerModel.getY();
        double offsetZ = playerModel.getZ();
        matrices.push();
        float animationProgress;
        EntityRenderer<?> entityRenderer = mc.getEntityRenderDispatcher().getRenderer(entity);
        if (entityRenderer instanceof PlayerEntityRenderer renderer)
        {
            animationProgress = renderer.getAnimationProgress(entity, tickDelta);
            setupPlayerTransforms(entity, matrices, animationProgress, entity.getBodyYaw(), tickDelta);
            matrices.scale(-1, -1, 1);
            renderer.scale(entity, matrices, tickDelta);
            matrices.translate(0, -1.5010000467300415, 0);

            render(matrixStack, playerModel.head, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            render(matrixStack, playerModel.body, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            render(matrixStack, playerModel.leftArm, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            render(matrixStack, playerModel.rightArm, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            render(matrixStack, playerModel.leftLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            render(matrixStack, playerModel.rightLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
        }
        matrices.pop();
    }

    public static void setupPlayerTransforms(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h)
    {
        float i = abstractClientPlayerEntity.getLeaningPitch(h);
        float j = abstractClientPlayerEntity.getPitch(h);
        if (abstractClientPlayerEntity.isFallFlying())
        {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            float k = (float) abstractClientPlayerEntity.getFallFlyingTicks() + h;
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

    public static void setupTransforms(LivingEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta)
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

    private static float getYaw(Direction direction)
    {
        return switch (direction)
        {
            case SOUTH -> 90.0f;
            case WEST -> 0.0f;
            case NORTH -> 270.0f;
            case EAST -> 180.0f;
            default -> 0.0f;
        };
    }

    public static void render(MatrixStack matrixStack, Entity entity, float tickDelta, int color, int lineColor, float lineWidth, boolean lines, boolean fill, boolean shine)
    {
        double offsetX = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        double offsetY = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        double offsetZ = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

        matrices.push();
        EntityRenderer<?> entityRenderer = mc.getEntityRenderDispatcher().getRenderer(entity);

        if (entityRenderer instanceof LivingEntityRenderer renderer)
        {
            LivingEntity livingEntity = (LivingEntity) entity;
            EntityModel<LivingEntity> model = renderer.getModel();

            if (entityRenderer instanceof PlayerEntityRenderer r)
            {
                PlayerEntityModel<AbstractClientPlayerEntity> playerModel = r.getModel();

                playerModel.sneaking = entity.isInSneakingPose();
                BipedEntityModel.ArmPose armPose = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity) entity, Hand.MAIN_HAND);
                BipedEntityModel.ArmPose armPose2 = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity) entity, Hand.OFF_HAND);

                if (armPose.isTwoHanded())
                {
                    armPose2 = livingEntity.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
                }

                if (livingEntity.getMainArm() == Arm.RIGHT)
                {
                    playerModel.rightArmPose = armPose;
                    playerModel.leftArmPose = armPose2;
                }
                else
                {
                    playerModel.rightArmPose = armPose2;
                    playerModel.leftArmPose = armPose;
                }
            }

            model.handSwingProgress = livingEntity.getHandSwingProgress(tickDelta);
            model.riding = livingEntity.hasVehicle();
            model.child = livingEntity.isBaby();

            boolean rotating = entity == mc.player && Managers.ROTATION.isRotating();
            float bodyYaw = rotating ? Managers.ROTATION.getRotationYaw() : MathHelper.lerpAngleDegrees(tickDelta, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
            float headYaw = rotating ? Managers.ROTATION.getRotationYaw() : MathHelper.lerpAngleDegrees(tickDelta, livingEntity.prevHeadYaw, livingEntity.headYaw);
            float yaw = headYaw - bodyYaw;

            float animationProgress;
            if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof LivingEntity livingEntity2)
            {
                bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
                yaw = headYaw - bodyYaw;
                animationProgress = MathHelper.wrapDegrees(yaw);

                if (animationProgress < -85)
                {
                    animationProgress = -85;
                }
                if (animationProgress >= 85)
                {
                    animationProgress = 85;
                }

                bodyYaw = headYaw - animationProgress;
                if (animationProgress * animationProgress > 2500)
                {
                    bodyYaw += animationProgress * 0.2;
                }

                yaw = headYaw - bodyYaw;
            }

            float pitch = rotating ? Managers.ROTATION.getRotationPitch() : MathHelper.lerp(tickDelta, livingEntity.prevPitch, livingEntity.getPitch());

            animationProgress = renderer.getAnimationProgress(livingEntity, tickDelta);
            float limbDistance = 0;
            float limbAngle = 0;

            if (!livingEntity.hasVehicle() && livingEntity.isAlive())
            {
                limbDistance = livingEntity.limbAnimator.getSpeed(tickDelta);
                limbAngle = livingEntity.limbAnimator.getPos(tickDelta);

                if (livingEntity.isBaby())
                {
                    limbAngle *= 3;
                }
                if (limbDistance > 1)
                {
                    limbDistance = 1;
                }
            }

            model.animateModel(livingEntity, limbAngle, limbDistance, tickDelta);
            model.setAngles(livingEntity, limbAngle, limbDistance, animationProgress, yaw, pitch);

            renderer.setupTransforms(livingEntity, matrices, animationProgress, bodyYaw, tickDelta, 1.0f);
            matrices.scale(-1, -1, 1);
            renderer.scale(livingEntity, matrices, tickDelta);
            matrices.translate(0, -1.5010000467300415, 0);

            if (model instanceof AnimalModel m)
            {
                if (m.child)
                {
                    matrices.push();
                    float g;
                    if (((AccessorAnimalModel) m).hookGetHeadScaled())
                    {
                        g = 1.5F / ((AccessorAnimalModel) m).hookGetInvertedChildHeadScale();
                        matrices.scale(g, g, g);
                    }

                    matrices.translate(0.0D, ((AccessorAnimalModel) m).hookGetChildHeadYOffset() / 16.0f, ((AccessorAnimalModel) m).hookGetChildHeadZOffset() / 16.0f);
                    if (model instanceof BipedEntityModel mo)
                    {
                        render(matrixStack, mo.head, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                    }
                    else
                    {
                        ((AccessorAnimalModel) m).hookGetHeadParts().forEach(modelPart -> render(matrixStack, (ModelPart) modelPart, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine));
                    }
                    matrices.pop();
                    matrices.push();
                    g = 1.0f / ((AccessorAnimalModel) m).hookGetInvertedChildBodyScale();
                    matrices.scale(g, g, g);
                    matrices.translate(0.0D, ((AccessorAnimalModel) m).hookGetChildBodyYOffset() / 16.0f, 0.0D);
                    if (model instanceof BipedEntityModel mo)
                    {
                        render(matrixStack, mo.body, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.leftArm, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.rightArm, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.leftLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.rightLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                    }
                    else
                    {
                        ((AccessorAnimalModel) m).hookGetBodyParts().forEach(modelPart -> render(matrixStack, (ModelPart) modelPart, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine));
                    }
                    matrices.pop();
                }
                else
                {
                    if (model instanceof BipedEntityModel mo)
                    {
                        render(matrixStack, mo.head, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.body, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.leftArm, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.rightArm, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.leftLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, mo.rightLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                    }
                    else
                    {
                        ((AccessorAnimalModel) m).hookGetHeadParts().forEach(modelPart -> render(matrixStack, (ModelPart) modelPart, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine));
                        ((AccessorAnimalModel) m).hookGetBodyParts().forEach(modelPart -> render(matrixStack, (ModelPart) modelPart, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine));
                    }
                }
            }
            else
            {
                if (model instanceof SinglePartEntityModel m)
                {
                    render(matrixStack, m.getPart(), offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                }
                else if (model instanceof CompositeEntityModel m)
                {
                    m.getParts().forEach(modelPart -> render(matrixStack, (ModelPart) modelPart, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine));
                }
                else if (model instanceof LlamaEntityModel m)
                {
                    if (m.child)
                    {
                        matrices.push();
                        matrices.scale(0.71428573F, 0.64935064F, 0.7936508F);
                        matrices.translate(0.0D, 1.3125D, 0.2199999988079071D);
                        render(matrixStack, m.head, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        matrices.pop();
                        matrices.push();
                        matrices.scale(0.625F, 0.45454544F, 0.45454544F);
                        matrices.translate(0.0D, 2.0625D, 0.0D);
                        render(matrixStack, m.body, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        matrices.pop();
                        matrices.push();
                        matrices.scale(0.45454544F, 0.41322312F, 0.45454544F);
                        matrices.translate(0.0D, 2.0625D, 0.0D);
                        render(matrixStack, m.rightHindLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftHindLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightFrontLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftFrontLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightChest, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftChest, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        matrices.pop();
                    }
                    else
                    {
                        render(matrixStack, m.head, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.body, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightHindLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftHindLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightFrontLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftFrontLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightChest, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftChest, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                    }
                }
                else if (model instanceof RabbitEntityModel m)
                {
                    if (m.child)
                    {
                        matrices.push();
                        matrices.scale(0.56666666F, 0.56666666F, 0.56666666F);
                        matrices.translate(0.0D, 1.375D, 0.125D);
                        render(matrixStack, m.head, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftEar, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightEar, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.nose, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        matrices.pop();
                        matrices.push();
                        matrices.scale(0.4F, 0.4F, 0.4F);
                        matrices.translate(0.0D, 2.25D, 0.0D);
                        render(matrixStack, m.leftHindLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightHindLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftHaunch, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightHaunch, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.body, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftFrontLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightFrontLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.tail, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        matrices.pop();
                    }
                    else
                    {
                        matrices.push();
                        matrices.scale(0.6F, 0.6F, 0.6F);
                        matrices.translate(0.0D, 1.0D, 0.0D);
                        render(matrixStack, m.leftHindLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightHindLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftHaunch, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightHaunch, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.body, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftFrontLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightFrontLeg, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.head, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.rightEar, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.leftEar, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.tail, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        render(matrixStack, m.nose, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
                        matrices.pop();
                    }
                }
            }
        }

        if (entityRenderer instanceof EndCrystalEntityRenderer renderer)
        {
            EndCrystalEntity crystalEntity = (EndCrystalEntity) entity;

            matrices.push();
            float h = CrystalModelModule.getInstance().isEnabled() && !CrystalModelModule.getInstance().getBounce() ? -1.0f : EndCrystalEntityRenderer.getYOffset(crystalEntity, tickDelta);
            float j = (float) ((crystalEntity.endCrystalAge + tickDelta) * (CrystalModelModule.getInstance().isEnabled() ? CrystalModelModule.getInstance().getSpin() : 1.0f)) * 3.0f;
            matrices.push();
            if (CrystalModelModule.getInstance().isEnabled())
            {
                float crystalScale = CrystalModelModule.getInstance().getScale();
                matrices.scale(crystalScale, crystalScale, crystalScale);
            }
            matrices.scale(2.0f, 2.0f, 2.0f);
            matrices.translate(0.0f, -0.5f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            matrices.translate(0.0D, 1.5F + h / 2.0f, 0.0D);
            matrices.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            render(matrixStack, renderer.frame, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            matrices.scale(0.875F, 0.875F, 0.875F);
            matrices.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            render(matrixStack, renderer.frame, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            matrices.scale(0.875F, 0.875F, 0.875F);
            matrices.multiply(new Quaternionf().setAngleAxis(1.0471976f, SINE_45_DEGREES, 0.0f, SINE_45_DEGREES));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            // render(matrixStack, renderer.core, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            matrices.pop();
            matrices.pop();
        }

//        if (!NametagsModule.getInstance().isEnabled() && (entity.shouldRenderName() || entity.hasCustomName() && entity == mc.getEntityRenderDispatcher().targetedEntity))
//        {
//            renderLabel(entity.getDisplayName(), matrices, entity, tickDelta);
//        }

        matrices.pop();
    }

    public static void renderHand(MatrixStack matrixStack, float tickDelta, int lineColor, int color,
                                  float lineWidth, boolean lines, boolean fill, boolean shine)
    {
        if (!mc.options.getPerspective().isFirstPerson() || FreecamModule.getInstance().isEnabled())
        {
            return;
        }
        double d = mc.options.getFov().getValue();
        matrixStack.multiplyPositionMatrix(mc.gameRenderer.getBasicProjectionMatrix(d));
        // mc.gameRenderer.loadProjectionMatrix(mc.gameRenderer.getBasicProjectionMatrix(d));
        matrixStack.loadIdentity();
        // Bob view
        if (mc.options.getBobView().getValue())
        {
            PlayerEntity playerEntity = (PlayerEntity) mc.getCameraEntity();
            float f = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float g = -(playerEntity.horizontalSpeed + f * tickDelta);
            float h = MathHelper.lerp(tickDelta, playerEntity.prevStrideDistance, playerEntity.strideDistance);
            matrixStack.translate(MathHelper.sin(g * (float) Math.PI) * h * 0.5f, -Math.abs(MathHelper.cos(g * (float) Math.PI) * h), 0.0f);
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(g * (float) Math.PI) * h * 3.0f));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(g * (float) Math.PI - 0.2f) * h) * 5.0f));
        }
        //
        float h1 = MathHelper.lerp(tickDelta, mc.player.lastRenderPitch, mc.player.renderPitch);
        float i1 = MathHelper.lerp(tickDelta, mc.player.lastRenderYaw, mc.player.renderYaw);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees((mc.player.getPitch(tickDelta) - h1) * 0.1f));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((mc.player.getYaw(tickDelta) - i1) * 0.1f));
        float f1 = mc.player.getHandSwingProgress(tickDelta);
        Hand hand = SwingModule.getInstance().isEnabled() ? SwingModule.getInstance().getPrevPreferredHand() : MoreObjects.firstNonNull(mc.player.preferredHand, Hand.MAIN_HAND);
        boolean bl2;
        ItemStack itemStack = mc.player.getMainHandStack();
        ItemStack itemStack2 = mc.player.getOffHandStack();
        boolean bl = itemStack.isOf(Items.BOW) || itemStack2.isOf(Items.BOW);
        bl2 = itemStack.isOf(Items.CROSSBOW) || itemStack2.isOf(Items.CROSSBOW);
        HandRenderType handRenderType = HandRenderType.RENDER_BOTH_HANDS;
        if (!bl && !bl2)
        {
            handRenderType = HandRenderType.RENDER_BOTH_HANDS;
        }
        else if (mc.player.isUsingItem())
        {
            ItemStack itemStack1 = mc.player.getActiveItem();
            Hand hand1 = mc.player.getActiveHand();
            if (itemStack1.isOf(Items.BOW) || itemStack1.isOf(Items.CROSSBOW))
            {
                handRenderType = HandRenderType.shouldOnlyRender(hand1);
            }
            else
            {
                handRenderType = hand == Hand.MAIN_HAND && mc.player.getOffHandStack().isOf(Items.CROSSBOW) && CrossbowItem.isCharged(mc.player.getOffHandStack()) ? HandRenderType.RENDER_MAIN_HAND_ONLY : HandRenderType.RENDER_BOTH_HANDS;
            }
        }
        else if (itemStack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack))
        {
            handRenderType = HandRenderType.RENDER_MAIN_HAND_ONLY;
        }
        PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
        float k;
        float j;
        if (handRenderType.renderMainHand)
        {
            boolean bl1 = hand == Hand.MAIN_HAND;
            boolean bl3 = mc.player.preferredHand == Hand.MAIN_HAND;
            j = bl3 ? f1 : 0.0f;
            k = 1.0f - MathHelper.lerp(tickDelta, mc.gameRenderer.firstPersonRenderer.prevEquipProgressMainHand, mc.gameRenderer.firstPersonRenderer.equipProgressMainHand);
            Arm arm = bl1 ? mc.player.getMainArm() : mc.player.getMainArm().getOpposite();
            if (itemStack.isEmpty() && bl1 && !mc.player.isInvisible())
            {
                renderFirstPersonItem(matrixStack, tickDelta, playerEntityRenderer, arm, j, k, lineColor, color, lineWidth, lines, fill, shine);
            }
        }
        if (handRenderType.renderOffHand)
        {
            boolean bl1 = hand == Hand.OFF_HAND;
            boolean bl3 = mc.player.preferredHand == Hand.MAIN_HAND;
            j = bl3 ? f1 : 0.0f;
            k = 1.0f - MathHelper.lerp(tickDelta, mc.gameRenderer.firstPersonRenderer.prevEquipProgressOffHand, mc.gameRenderer.firstPersonRenderer.equipProgressOffHand);
            Arm arm = bl1 ? mc.player.getMainArm().getOpposite() : mc.player.getMainArm();
            if (itemStack.isEmpty() && bl1 && !mc.player.isInvisible())
            {
                renderFirstPersonItem(matrixStack, tickDelta, playerEntityRenderer, arm, j, k, lineColor, color, lineWidth, lines, fill, shine);
            }
        }
    }
    
    public static void renderFirstPersonItem(MatrixStack matrixStack, float tickDelta, PlayerEntityRenderer playerEntityRenderer, Arm arm, float swingProgress,
                                             float equipProgress, int lineColor, int color, float lineWidth, boolean lines, boolean fill, boolean shine)
    {
        RenderSystem.disableDepthTest();
        double offsetX = mc.gameRenderer.getCamera().getPos().x;
        double offsetY = mc.gameRenderer.getCamera().getPos().y;
        double offsetZ = mc.gameRenderer.getCamera().getPos().z;
        matrices.push();
        boolean bl = arm != Arm.LEFT;
        float f = bl ? 1.0f : -1.0f;
        float g = MathHelper.sqrt(swingProgress);
        float h = -0.3f * MathHelper.sin(g * (float) Math.PI);
        float i = 0.4f * MathHelper.sin(g * ((float) Math.PI * 2));
        float j = -0.4f * MathHelper.sin(swingProgress * (float) Math.PI);
        matrices.translate(f * (h + 0.64000005f), i + -0.6f + equipProgress * -0.6f, j + -0.71999997f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 45.0f));
        float k = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float l = MathHelper.sin(g * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * l * 70.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * k * -20.0f));
        matrices.translate(f * -1.0f, 3.6f, 3.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 120.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -135.0f));
        matrices.translate(f * 5.6f, 0.0f, 0.0f);
        playerEntityRenderer.setModelPose(mc.player);
        PlayerEntityModel model = playerEntityRenderer.getModel();
        model.handSwingProgress = 0.0f;
        model.sneaking = false;
        model.leaningPitch = 0.0f;
        model.setAngles(mc.player, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        if (arm == Arm.RIGHT)
        {
            model.rightArm.pitch = 0.0f;
            render(matrixStack, model.rightArm, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            model.rightSleeve.pitch = 0.0f;
            // render(matrixStack, model.rightSleeve, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
        }
        else
        {
            model.leftArm.pitch = 0.0f;
            render(matrixStack, model.leftArm, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
            model.leftSleeve.pitch = 0.0f;
            // render(matrixStack, model.leftSleeve, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
        }
        matrices.pop();
    }
    
    public static void render(MatrixStack matrixStack, ModelPart part, double offsetX, double offsetY, double offsetZ, int color, int lineColor, float lineWidth, boolean lines, boolean fill, boolean shine)
    {
        if (!part.visible || (part.cuboids.isEmpty() && part.children.isEmpty()))
        {
            return;
        }

        matrices.push();
        part.rotate(matrices);


        for (ModelPart.Cuboid cuboid : part.cuboids)
        {
            renderModelPart(matrixStack, cuboid, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
        }
        for (ModelPart child : part.children.values())
        {
            render(matrixStack, child, offsetX, offsetY, offsetZ, color, lineColor, lineWidth, lines, fill, shine);
        }
        matrices.pop();
    }

    private static void renderModelPart(MatrixStack matrixStack, ModelPart.Cuboid cuboid, double offsetX, double offsetY, double offsetZ, int color, int lineColor, float lineWidth, boolean lines, boolean fill, boolean shine)
    {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (ModelPart.Quad quad : cuboid.sides)
        {
            pos1.set(quad.vertices[0].pos.x / 16, quad.vertices[0].pos.y / 16, quad.vertices[0].pos.z / 16, 1);
            pos1.mul(matrix);
            pos2.set(quad.vertices[1].pos.x / 16, quad.vertices[1].pos.y / 16, quad.vertices[1].pos.z / 16, 1);
            pos2.mul(matrix);
            pos3.set(quad.vertices[2].pos.x / 16, quad.vertices[2].pos.y / 16, quad.vertices[2].pos.z / 16, 1);
            pos3.mul(matrix);
            pos4.set(quad.vertices[3].pos.x / 16, quad.vertices[3].pos.y / 16, quad.vertices[3].pos.z / 16, 1);
            pos4.mul(matrix);

            if (fill) 
            {
                if (shine) 
                {
                    RenderBuffers.TEXTURE_QUADS.begin(matrixStack);
                    RenderSystem.setShaderTexture(0, ITEM_ENCHANTMENT_GLINT);
                    RenderBuffers.TEXTURE_QUADS.color(color);
                    RenderBuffers.TEXTURE_QUADS.vertexTex(offsetX + pos1.x, offsetY + pos1.y, offsetZ + pos1.z, 0.0f, 0.0f)
                            .vertexTex(offsetX + pos2.x, offsetY + pos2.y, offsetZ + pos2.z, 0.0f, 1.0f)
                            .vertexTex(offsetX + pos3.x, offsetY + pos3.y, offsetZ + pos3.z, 1.0f, 1.0f)
                            .vertexTex(offsetX + pos4.x, offsetY + pos4.y, offsetZ + pos4.z, 1.0f, 0.0f);
                    RenderBuffers.TEXTURE_QUADS.end();
                }
                else 
                {
                    RenderBuffers.QUADS.begin(matrixStack);
                    RenderBuffers.QUADS.color(color);
                    RenderBuffers.QUADS.vertex(offsetX + pos1.x, offsetY + pos1.y, offsetZ + pos1.z)
                            .vertex(offsetX + pos2.x, offsetY + pos2.y, offsetZ + pos2.z)
                            .vertex(offsetX + pos3.x, offsetY + pos3.y, offsetZ + pos3.z)
                            .vertex(offsetX + pos4.x, offsetY + pos4.y, offsetZ + pos4.z);
                    RenderBuffers.QUADS.end(); 
                }
            }

            if (lines)
            {
                RenderBuffers.LINES.begin(matrixStack);
                RenderSystem.lineWidth(lineWidth);
                RenderBuffers.LINES.color(lineColor);
                RenderBuffers.LINES.vertexLine(offsetX + pos1.x, offsetY + pos1.y, offsetZ + pos1.z, offsetX + pos2.x, offsetY + pos2.y, offsetZ + pos2.z);
                RenderBuffers.LINES.vertexLine(offsetX + pos2.x, offsetY + pos2.y, offsetZ + pos2.z, offsetX + pos3.x, offsetY + pos3.y, offsetZ + pos3.z);
                RenderBuffers.LINES.vertexLine(offsetX + pos3.x, offsetY + pos3.y, offsetZ + pos3.z, offsetX + pos4.x, offsetY + pos4.y, offsetZ + pos4.z);
                RenderBuffers.LINES.vertexLine(offsetX + pos1.x, offsetY + pos1.y, offsetZ + pos1.z, offsetX + pos1.x, offsetY + pos1.y, offsetZ + pos1.z);
                RenderBuffers.LINES.end();
            }
        }
    }

    private enum HandRenderType
    {
        RENDER_BOTH_HANDS(true, true),
        RENDER_MAIN_HAND_ONLY(true, false),
        RENDER_OFF_HAND_ONLY(false, true);

        final boolean renderMainHand;
        final boolean renderOffHand;

        HandRenderType(boolean renderMainHand, boolean renderOffHand)
        {
            this.renderMainHand = renderMainHand;
            this.renderOffHand = renderOffHand;
        }

        public static HandRenderType shouldOnlyRender(Hand hand)
        {
            return hand == Hand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
        }
    }
}
