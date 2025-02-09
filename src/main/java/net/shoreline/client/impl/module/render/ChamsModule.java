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
import net.shoreline.client.api.render.model.StaticBipedEntityModel;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.entity.RenderCrystalEvent;
import net.shoreline.client.impl.event.render.entity.RenderEntityEvent;
import net.shoreline.client.impl.event.render.entity.RenderThroughWallsEvent;
import net.shoreline.client.impl.event.render.item.RenderArmEvent;
import net.shoreline.client.util.entity.EntityUtil;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.eventbus.annotation.EventListener;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @see ChamsModelRenderer
 */
public class ChamsModule extends ToggleModule
{
    private static ChamsModule INSTANCE;

    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The chams render range", 10.0f, 50.0f, 200.0f));
    Config<ChamsMode> modeConfig = register(new EnumConfig<>("Mode", "The rendering mode for the chams", ChamsMode.FILL, ChamsMode.values()));
    Config<Float> widthConfig = register(new NumberConfig<>("Width", "The line width of the render", 1.0f, 1.5f, 5.0f, () -> modeConfig.getValue() != ChamsMode.FILL));
    Config<Boolean> wallsConfig = register(new BooleanConfig("ThroughWalls", "Renders chams through walls", true, () -> modeConfig.getValue() != ChamsMode.NORMAL));
    // Config<Boolean> shineConfig = register(new BooleanConfig("Shine", "Adds enchantment glint", false));
    Config<Boolean> textureConfig = register(new BooleanConfig("Texture", "Renders the entity model texture", false, () -> wallsConfig.getValue() && modeConfig.getValue() != ChamsMode.NORMAL));
    Config<Boolean> playersConfig = register(new BooleanConfig("Players", "Render chams on other players", true));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Render chams on the player", true));
    Config<Boolean> handsConfig = register(new BooleanConfig("Hands", "Render chams on first-person hands", true));
    Config<Boolean> monstersConfig = register(new BooleanConfig("Monsters", "Render chams on monsters", true));
    Config<Boolean> animalsConfig = register(new BooleanConfig("Animals", "Render chams on animals", true));
    Config<Boolean> crystalsConfig = register(new BooleanConfig("Crystals", "Render chams on crystals", true));
    Config<Boolean> popsConfig = register(new BooleanConfig("Pops", "Render chams on totem pops", false));
    Config<Integer> fadeTimeConfig = register(new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 1000, 3000, () -> false));
    Config<Color> colorConfig = register(new ColorConfig("Color", "The color of the chams", new Color(255, 0, 0, 60)));

    private final Map<PopChamEntity, Animation> fadeList = new ConcurrentHashMap<>();

    public ChamsModule()
    {
        super("Chams", "Renders entity models through walls", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static ChamsModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        fadeList.clear();
    }

    @EventListener
    public void onRenderThroughWalls(RenderThroughWallsEvent event)
    {
        if (modeConfig.getValue() == ChamsMode.NORMAL && checkChams(event.getEntity()))
        {
            event.cancel();
        }
    }

    @EventListener(priority = Integer.MAX_VALUE)
    public void onRenderWorld(RenderWorldEvent event)
    {
        // Pop chams
        RenderBuffers.preRender();
        RenderSystem.disableDepthTest();
        for (Map.Entry<PopChamEntity, Animation> set : new HashSet<>(fadeList.entrySet()))
        {
            set.getValue().setState(false);
            Color color = colorConfig.getValue();
            int boxAlpha = (int) (color.getAlpha() * set.getValue().getFactor());
            int lineAlpha = (int) (145 * set.getValue().getFactor());
            int boxColor = ColorUtil.withAlpha(color.getRGB(), boxAlpha);
            int lineColor = ColorUtil.withAlpha(color.getRGB(), lineAlpha);
            ChamsModelRenderer.renderStaticPlayerModel(event.getMatrices(), set.getKey(), set.getKey().getModel(), event.getTickDelta(),
                    boxColor, lineColor, widthConfig.getValue(), modeConfig.getValue() != ChamsMode.FILL, true, false);
        }
        fadeList.entrySet().removeIf(e ->
                e.getValue().getFactor() == 0.0);

        RenderBuffers.postRender();

        if (modeConfig.getValue() == ChamsMode.NORMAL)
        {
            return;
        }

        if (ShadersModule.getInstance().isEnabled() && !ShadersModule.getInstance().textureConfig.getValue())
        {
            return;
        }

        // Entity chams
        RenderBuffers.preRender();
        if (!wallsConfig.getValue())
        {
            RenderSystem.enableDepthTest();
        }

        for (Entity entity : mc.world.getEntities())
        {
            if (NoRenderModule.getInstance().isEnabled() && NoRenderModule.getInstance().shouldSkipEntity(entity))
            {
                continue;
            }
            double x = Math.abs(mc.gameRenderer.getCamera().getPos().x - entity.getX());
            double z = Math.abs(mc.gameRenderer.getCamera().getPos().z - entity.getZ());
            double d = (mc.options.getViewDistance().getValue() + 1) * 16;
            Vec3d start = FreecamModule.getInstance().isEnabled() ? FreecamModule.getInstance().getCameraPosition() : mc.player.getPos();
            if (start.squaredDistanceTo(entity.getPos()) > ((NumberConfig) rangeConfig).getValueSq() || x > d || z > d)
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
                renderEntityChams(event.getMatrices(), entity, event.getTickDelta());
            }
        }

        RenderBuffers.postRender();
        if (!wallsConfig.getValue())
        {
            RenderSystem.depthMask(true);
        }
    }

    @EventListener
    public void onRenderGame(RenderWorldEvent.Hand event)
    {
        if (modeConfig.getValue() == ChamsMode.NORMAL)
        {
            return;
        }
        if (ShadersModule.getInstance().isEnabled() && !ShadersModule.getInstance().textureConfig.getValue())
        {
            return;
        }
        RenderBuffers.preRender();
        if (handsConfig.getValue())
        {
            int color1 = colorConfig.getValue().getRGB();
            int lineColor1 = ColorUtil.withAlpha(color1, 145);
            ChamsModelRenderer.renderHand(event.getMatrices(), event.getTickDelta(), lineColor1, color1, widthConfig.getValue(),
                    modeConfig.getValue() != ChamsMode.FILL, modeConfig.getValue() != ChamsMode.WIREFRAME, false);
        }
        RenderBuffers.postRender();
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
            if (entity == mc.player || !(entity instanceof PlayerEntity player))
            {
                return;
            }
            Animation animation = new Animation(true, fadeTimeConfig.getValue());
            fadeList.put(new PopChamEntity(player, mc.getRenderTickCounter().getTickDelta(true)), animation);
        }
    }

    @EventListener
    public void onRenderCrystal(RenderCrystalEvent event)
    {
        if (modeConfig.getValue() == ChamsMode.NORMAL)
        {
            return;
        }
        if (mc.player != null && (!textureConfig.getValue() || !wallsConfig.getValue()) && crystalsConfig.getValue() &&
                mc.player.squaredDistanceTo(event.endCrystalEntity) <= ((NumberConfig) rangeConfig).getValueSq())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderEntity(RenderEntityEvent event)
    {
        if (modeConfig.getValue() == ChamsMode.NORMAL)
        {
            return;
        }
        if (mc.player == null || (textureConfig.getValue() && wallsConfig.getValue()) || !checkChams(event.entity)
                || mc.player.squaredDistanceTo(event.entity) > ((NumberConfig) rangeConfig).getValueSq())
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
            ChamsModelRenderer.setupPlayerTransforms((AbstractClientPlayerEntity) event.entity, event.matrixStack, l, h, event.g);
        }
        else
        {
            ChamsModelRenderer.setupTransforms(event.entity, event.matrixStack, l, h, event.g);
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

    @EventListener
    public void onRenderArm(RenderArmEvent event)
    {
        if (modeConfig.getValue() == ChamsMode.NORMAL)
        {
            return;
        }
        if (handsConfig.getValue() && !textureConfig.getValue())
        {
            event.cancel();
        }
    }

    public void renderEntityChams(MatrixStack matrixStack, Entity entity, float tickDelta)
    {
        int color1 = colorConfig.getValue().getRGB();
        int lineColor1 = ColorUtil.withAlpha(color1, 145);
        renderEntityChams(matrixStack, entity, tickDelta, color1, lineColor1);
    }

    public void renderEntityChams(MatrixStack matrixStack, Entity entity, float tickDelta, int color, int lineColor)
    {
        ChamsModelRenderer.render(matrixStack, entity, tickDelta, color, lineColor,
                widthConfig.getValue(), modeConfig.getValue() != ChamsMode.FILL, modeConfig.getValue() != ChamsMode.WIREFRAME, false);
    }

    private float getAnimationProgress(LivingEntity entity, float f)
    {
        if (entity instanceof SquidEntity)
        {
            return MathHelper.lerp(f, ((SquidEntity) entity).prevTentacleAngle, ((SquidEntity) entity).tentacleAngle);
        }
        return entity instanceof WolfEntity wolf ? wolf.getTailAngle() : entity.age + f;
    }

    public boolean checkChams(LivingEntity entity)
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
        NORMAL,
        FILL,
        WIREFRAME,
        WIRE_FILL
    }

    public static class PopChamEntity extends FakePlayerEntity
    {
        private final StaticBipedEntityModel<AbstractClientPlayerEntity> model;

        public PopChamEntity(PlayerEntity player, float tickDelta)
        {
            super(player);
            this.model = new StaticBipedEntityModel<>((AbstractClientPlayerEntity) player, false, tickDelta);
            this.leaningPitch = player.leaningPitch;
            this.lastLeaningPitch = player.leaningPitch;
            setPose(player.getPose());
        }

        public StaticBipedEntityModel<AbstractClientPlayerEntity> getModel()
        {
            return model;
        }
    }
}