package net.shoreline.client.impl.module.render;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EntityListConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.chunk.light.RenderSkylightEvent;
import net.shoreline.client.impl.event.entity.FireworkTickEvent;
import net.shoreline.client.impl.event.entity.ItemTickEvent;
import net.shoreline.client.impl.event.entity.RenderFireEntityEvent;
import net.shoreline.client.impl.event.gui.hud.RenderOverlayEvent;
import net.shoreline.client.impl.event.render.*;
import net.shoreline.client.impl.event.render.block.RenderTileEntityEvent;
import net.shoreline.client.impl.event.render.block.entity.RenderSignTextEvent;
import net.shoreline.client.impl.event.render.entity.*;
import net.shoreline.client.impl.event.toast.RenderToastEvent;
import net.shoreline.client.impl.event.world.BlindnessEvent;
import net.shoreline.client.impl.module.exploit.GodModeModule;
import net.shoreline.client.mixin.accessor.AccessorFireworkRocketEntity;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class NoRenderModule extends ToggleModule
{
    private static NoRenderModule INSTANCE;

    Config<Boolean> hurtCamConfig = register(new BooleanConfig("NoHurtCam", "Prevents the hurt camera shake effect from rendering", true));
    Config<Boolean> armorConfig = register(new BooleanConfig("Armor", "Prevents armor pieces from rendering", false));
    Config<Boolean> fireOverlayConfig = register(new BooleanConfig("Overlay-Fire", "Prevents the fire Hud overlay from rendering", true));
    Config<Boolean> portalOverlayConfig = register(new BooleanConfig("Overlay-Portal", "Prevents the portal Hud overlay from rendering", true));
    Config<Boolean> waterOverlayConfig = register(new BooleanConfig("Overlay-Water", "Prevents the water Hud overlay from rendering", true));
    Config<Boolean> blockOverlayConfig = register(new BooleanConfig("Overlay-Block", "Prevents the block Hud overlay from rendering", true));
    Config<Boolean> spyglassOverlayConfig = register(new BooleanConfig("Overlay-Spyglass", "Prevents the spyglass Hud overlay from rendering", false));
    Config<Boolean> pumpkinOverlayConfig = register(new BooleanConfig("Overlay-Pumpkin", "Prevents the pumpkin Hud overlay from rendering", true));
    Config<Boolean> bossOverlayConfig = register(new BooleanConfig("Overlay-BossBar", "Prevents the boss bar Hud overlay from rendering", true));
    Config<Boolean> nauseaConfig = register(new BooleanConfig("Nausea", "Prevents nausea effect from rendering (includes portal effect)", false));
    Config<Boolean> blindnessConfig = register(new BooleanConfig("Blindness", "Prevents blindness effect from rendering", false));
    Config<Boolean> frostbiteConfig = register(new BooleanConfig("Frostbite", "Prevents frostbite effect from rendering", false));
    Config<Boolean> skylightConfig = register(new BooleanConfig("Skylight", "Prevents skylight from rendering", true));
    Config<Boolean> witherSkullsConfig = register(new BooleanConfig("WitherSkulls", "Prevents flying wither skulls from rendering", false));
    Config<Boolean> itemFramesConfig = register(new BooleanConfig("ItemFrames", "Prevents items on item frames from rendering", false));
    Config<Boolean> entitiesConfig = register(new BooleanConfig("Entities", "Prevents entities from rendering", false));
    Config<List<EntityType>> entitiesListConfig = register(new EntityListConfig<>("EntityList", "The render entity list"));
    Config<Boolean> tileEntitiesConfig = register(new BooleanConfig("TileEntities", "Prevents special tile entity properties from rendering (i.e. enchantment table books or cutting table saws)", false));
    Config<Boolean> signTextConfig = register(new BooleanConfig("SignText", "Prevents the text on signs from rendering", false));
    Config<Boolean> fireEntityConfig = register(new BooleanConfig("FireEntities", "Prevents fire from rendering on entities", false));
    Config<Boolean> fireworksConfig = register(new BooleanConfig("Fireworks", "Prevents firework entities from rendering", true));
    Config<Boolean> totemConfig = register(new BooleanConfig("Totems", "Prevents totem pop overlay from rendering", false));
    Config<Boolean> worldBorderConfig = register(new BooleanConfig("WorldBorder", "Prevents world border from rendering", false));
    Config<FogRender> fogConfig = register(new EnumConfig<>("Fog", "Prevents fog from rendering in the world", FogRender.OFF, FogRender.values()));
    Config<ItemRender> itemsConfig = register(new EnumConfig<>("Items", "Prevents dropped items from rendering", ItemRender.OFF, ItemRender.values()));
    Config<Boolean> guiToastConfig = register(new BooleanConfig("GuiToast", "Prevents advancements from rendering", true));
    Config<Boolean> terrainScreenConfig = register(new BooleanConfig("TerrainScreen", "Prevents downloading terrain screen from rendering", false));

    public NoRenderModule()
    {
        super("NoRender", "Prevents certain game elements from rendering", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static NoRenderModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }

        if (itemsConfig.getValue() == ItemRender.REMOVE)
        {
            for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
            {
                if (entity instanceof ItemEntity)
                {
                    mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                }
            }
        }

        if (terrainScreenConfig.getValue() && mc.currentScreen instanceof DownloadingTerrainScreen
                && !GodModeModule.getInstance().isPortal())
        {
            mc.currentScreen = null;
        }
    }

    @EventListener
    public void onHurtCam(HurtCamEvent event)
    {
        if (hurtCamConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderArmor(RenderArmorEvent event)
    {
        if (armorConfig.getValue() && event.getEntity() instanceof PlayerEntity)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayFire(RenderOverlayEvent.Fire event)
    {
        if (fireOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayPortal(RenderOverlayEvent.Portal event)
    {
        if (portalOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayWater(RenderOverlayEvent.Water event)
    {
        if (waterOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayBlock(RenderOverlayEvent.Block event)
    {
        if (blockOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlaySpyglass(RenderOverlayEvent.Spyglass event)
    {
        if (spyglassOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayPumpkin(RenderOverlayEvent.Pumpkin event)
    {
        if (pumpkinOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayBossBar(RenderOverlayEvent.BossBar event)
    {
        if (bossOverlayConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayFrostbite(RenderOverlayEvent.Frostbite event)
    {
        if (frostbiteConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderNausea(RenderNauseaEvent event)
    {
        if (nauseaConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onBlindness(BlindnessEvent event)
    {
        if (blindnessConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderSkylight(RenderSkylightEvent event)
    {
        if (skylightConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderWitherSkull(RenderWitherSkullEvent event)
    {
        if (witherSkullsConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderItemFrame(RenderItemFrameEvent event)
    {
        if (itemFramesConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderEnchantingTableBook(RenderTileEntityEvent.EnchantingTableBook event)
    {
        if (tileEntitiesConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderEntityInWorld(RenderEntityInWorldEvent event)
    {
        if (shouldSkipEntity(event.getEntity()))
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderSignText(RenderSignTextEvent event)
    {
        if (signTextConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFireworkRocket(RenderFireworkRocketEvent event)
    {
        if (fireworksConfig.getValue())
        {
            event.cancel();
        }
    }


    @EventListener
    public void onFireworkTick(FireworkTickEvent event)
    {
        if (fireworksConfig.getValue() && !((AccessorFireworkRocketEntity) event.getFireworkRocketEntity()).hookWasShotByEntity())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFloatingItem(RenderFloatingItemEvent event)
    {
        if (totemConfig.getValue() && event.getFloatingItem() == Items.TOTEM_OF_UNDYING)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderWorldBorder(RenderWorldBorderEvent event)
    {
        if (worldBorderConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener(priority = Integer.MIN_VALUE)
    public void onRenderFog(RenderFogEvent event)
    {
        if (fogConfig.getValue() == FogRender.LIQUID_VISION
                && mc.player != null && mc.player.isSubmergedIn(FluidTags.LAVA))
        {
            event.cancel();
            event.setStart(event.getViewDistance() * 4.0f);
            event.setEnd(event.getViewDistance() * 4.25f);
        }
        else if (fogConfig.getValue() == FogRender.CLEAR)
        {
            event.cancel();
            event.setStart(event.getViewDistance() * 4.0f);
            event.setEnd(event.getViewDistance() * 4.25f);
        }
    }

    @EventListener
    public void onRenderItem(RenderItemEvent event)
    {
        if (itemsConfig.getValue() == ItemRender.HIDE)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderToast(RenderToastEvent event)
    {
        if (guiToastConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderFireEntity(RenderFireEntityEvent event)
    {
        if (fireEntityConfig.getValue())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onItemTick(ItemTickEvent event)
    {
        if (itemsConfig.getValue() != ItemRender.OFF)
        {
            event.cancel();
        }
    }

    public boolean shouldSkipEntity(Entity entity)
    {
        return entitiesConfig.getValue() && entitiesListConfig.getValue().contains(entity.getType());
    }

    public enum FogRender
    {
        CLEAR,
        LIQUID_VISION,
        OFF
    }

    public enum ItemRender
    {
        REMOVE,
        HIDE,
        OFF
    }
}
