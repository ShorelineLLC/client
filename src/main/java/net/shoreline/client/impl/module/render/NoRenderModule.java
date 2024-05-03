package net.shoreline.client.impl.module.render;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.chunk.light.RenderSkylightEvent;
import net.shoreline.client.impl.event.gui.hud.RenderOverlayEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.particle.ParticleEvent;
import net.shoreline.client.impl.event.render.*;
import net.shoreline.client.impl.event.render.block.RenderTileEntityEvent;
import net.shoreline.client.impl.event.render.entity.RenderArmorEvent;
import net.shoreline.client.impl.event.render.entity.RenderFireworkRocketEvent;
import net.shoreline.client.impl.event.render.entity.RenderItemEvent;
import net.shoreline.client.impl.event.render.entity.RenderWitherSkullEvent;
import net.shoreline.client.impl.event.toast.RenderToastEvent;
import net.shoreline.client.impl.event.world.BlindnessEvent;

/**
 * @author linus
 * @since 1.0
 */
public class NoRenderModule extends ToggleModule {
    Config<Boolean> hurtCamConfig = new BooleanConfig("NoHurtCam", "Prevents the hurt camera shake effect from rendering", true);
    Config<Boolean> antiCrashConfig = new BooleanConfig("NoServerCrash", "Prevents server packets from crashing the client", false);
    Config<Boolean> armorConfig = new BooleanConfig("Armor", "Prevents armor pieces from rendering", false);
    Config<Boolean> fireOverlayConfig = new BooleanConfig("Overlay-Fire", "Prevents the fire Hud overlay from rendering", true);
    Config<Boolean> waterOverlayConfig = new BooleanConfig("Overlay-Water", "Prevents the water Hud overlay from rendering", true);
    Config<Boolean> blockOverlayConfig = new BooleanConfig("Overlay-Block", "Prevents the block Hud overlay from rendering", true);
    Config<Boolean> spyglassOverlayConfig = new BooleanConfig("Overlay-Spyglass", "Prevents the spyglass Hud overlay from rendering", false);
    Config<Boolean> pumpkinOverlayConfig = new BooleanConfig("Overlay-Pumpkin", "Prevents the pumpkin Hud overlay from rendering", true);
    Config<Boolean> bossOverlayConfig = new BooleanConfig("Overlay-BossBar", "Prevents the boss bar Hud overlay from rendering", true);
    Config<Boolean> nauseaConfig = new BooleanConfig("Nausea", "Prevents nausea effect from rendering (includes portal effect)", false);
    Config<Boolean> blindnessConfig = new BooleanConfig("Blindness", "Prevents blindness effect from rendering", false);
    Config<Boolean> frostbiteConfig = new BooleanConfig("Frostbite", "Prevents frostbite effect from rendering", false);
    Config<Boolean> skylightConfig = new BooleanConfig("Skylight", "Prevents skylight from rendering", true);
    Config<Boolean> witherSkullsConfig = new BooleanConfig("WitherSkulls", "Prevents flying wither skulls from rendering", false);
    Config<Boolean> tileEntitiesConfig = new BooleanConfig("TileEntities", "Prevents special tile entity properties from rendering (i.e. enchantment table books or cutting table saws)", false);
    Config<Boolean> fireworksConfig = new BooleanConfig("Fireworks", "Prevents firework particles from rendering", true);
    Config<Boolean> explosionsConfig = new BooleanConfig("Explosions", "Prevents explosion particles from rendering", true);
    Config<Boolean> campfiresConfig = new BooleanConfig("Campfires", "Prevents campfire particles from rendering", false);
    Config<Boolean> totemConfig = new BooleanConfig("Totems", "Prevents totem particles from rendering", false);
    Config<Boolean> worldBorderConfig = new BooleanConfig("WorldBorder", "Prevents world border from rendering", false);
    Config<Boolean> interpolationConfig = new BooleanConfig("Interpolation", "Entities will be rendered at their server positions", false);
    Config<FogRender> fogConfig = new EnumConfig<>("Fog", "Prevents fog from rendering in the world", FogRender.OFF, FogRender.values());
    Config<ItemRender> itemsConfig = new EnumConfig<>("Items", "Prevents dropped items from rendering", ItemRender.OFF, ItemRender.values());
    Config<Boolean> guiToastConfig = new BooleanConfig("GuiToast", "Prevents advancements from rendering", true);

    public NoRenderModule() {
        super("NoRender", "Prevents certain game elements from rendering",
                ModuleCategory.RENDER);
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (itemsConfig.getValue() == ItemRender.REMOVE && event.getStage() == EventStage.PRE) {
            for (Entity entity : Lists.newArrayList(mc.world.getEntities())) {
                if (entity instanceof ItemEntity) {
                    mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event) {
        if (mc.world == null) {
            return;
        }
        if (antiCrashConfig.getValue()) {
            // Out of bounds packets from server
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet
                    && (packet.getX() > 30000000 || packet.getY() > mc.world.getTopY()
                    || packet.getZ() > 30000000 || packet.getX() < -30000000
                    || packet.getY() < mc.world.getBottomY() || packet.getZ() < -30000000)) {
                event.cancel();
            } else if (event.getPacket() instanceof ExplosionS2CPacket packet
                    && (packet.getX() > 30000000 || packet.getY() > mc.world.getTopY()
                    || packet.getZ() > 30000000 || packet.getX() < -30000000
                    || packet.getY() < mc.world.getBottomY() || packet.getZ() < -30000000
                    || packet.getRadius() > 1000 || packet.getAffectedBlocks().size() > 1000
                    || packet.getPlayerVelocityX() > 1000 || packet.getPlayerVelocityY() > 1000
                    || packet.getPlayerVelocityZ() > 1000 || packet.getPlayerVelocityX() < -1000
                    || packet.getPlayerVelocityY() < -1000 || packet.getPlayerVelocityZ() < -1000)) {
                event.cancel();
            } else if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet
                    && (packet.getVelocityX() > 1000 || packet.getVelocityY() > 1000 ||
                    packet.getVelocityZ() > 1000 || packet.getVelocityX() < -1000 ||
                    packet.getVelocityY() < -1000 || packet.getVelocityZ() < -1000)) {
                event.cancel();
            } else if (event.getPacket() instanceof ParticleS2CPacket packet
                    && packet.getCount() > 500) {
                event.cancel();
            }
        }
    }

    @EventListener
    public void onHurtCam(HurtCamEvent event) {
        if (hurtCamConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderArmor(RenderArmorEvent event) {
        if (armorConfig.getValue() && event.getEntity() instanceof PlayerEntity) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayFire(RenderOverlayEvent.Fire event) {
        if (fireOverlayConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayWater(RenderOverlayEvent.Water event) {
        if (waterOverlayConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayBlock(RenderOverlayEvent.Block event) {
        if (blockOverlayConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlaySpyglass(RenderOverlayEvent.Spyglass event) {
        if (spyglassOverlayConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayPumpkin(RenderOverlayEvent.Pumpkin event) {
        if (pumpkinOverlayConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayBossBar(RenderOverlayEvent.BossBar event) {
        if (bossOverlayConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayFrostbite(RenderOverlayEvent.Frostbite event) {
        if (frostbiteConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderNausea(RenderNauseaEvent event) {
        if (nauseaConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onBlindness(BlindnessEvent event) {
        if (blindnessConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderSkylight(RenderSkylightEvent event) {
        if (skylightConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderWitherSkull(RenderWitherSkullEvent event) {
        if (witherSkullsConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderEnchantingTableBook(RenderTileEntityEvent.EnchantingTableBook event) {
        if (tileEntitiesConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onParticle(ParticleEvent event) {
        if (explosionsConfig.getValue() && (event.getParticleType() == ParticleTypes.EXPLOSION
                || event.getParticleType() == ParticleTypes.EXPLOSION_EMITTER)
                || fireworksConfig.getValue() && event.getParticleType() == ParticleTypes.FIREWORK
                || campfiresConfig.getValue() && event.getParticleType() == ParticleTypes.CAMPFIRE_COSY_SMOKE) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFireworkRocket(RenderFireworkRocketEvent event) {
        if (fireworksConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFloatingItem(RenderFloatingItemEvent event) {
        if (totemConfig.getValue() && event.getFloatingItem() == Items.TOTEM_OF_UNDYING) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderWorldBorder(RenderWorldBorderEvent event) {
        if (worldBorderConfig.getValue()) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFog(RenderFogEvent event) {
        if (fogConfig.getValue() == FogRender.LIQUID_VISION
                && mc.player != null && mc.player.isSubmergedIn(FluidTags.LAVA)) {
            event.cancel();
        } else if (fogConfig.getValue() == FogRender.CLEAR) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderItem(RenderItemEvent event) {
        if (itemsConfig.getValue() == ItemRender.HIDE) {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderToast(RenderToastEvent event) {
        if (guiToastConfig.getValue()) {
            event.cancel();
        }
    }

    public enum FogRender {
        CLEAR,
        LIQUID_VISION,
        OFF
    }

    public enum ItemRender {
        REMOVE,
        HIDE,
        OFF
    }
}
