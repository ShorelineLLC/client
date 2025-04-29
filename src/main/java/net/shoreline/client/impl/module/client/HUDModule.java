package net.shoreline.client.impl.module.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.ScreenOpenEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.entity.StatusEffectEvent;
import net.shoreline.client.impl.event.gui.hud.RenderOverlayEvent;
import net.shoreline.client.impl.event.gui.screen.RenderOpenChatEvent;
import net.shoreline.client.impl.event.network.DisconnectEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.exploit.FastLatencyModule;
import net.shoreline.client.impl.module.misc.TimerModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.StreamUtils;
import net.shoreline.client.util.math.PerSecondCounter;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.client.util.string.EnumFormatter;
import net.shoreline.client.util.world.BlockUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.loader.Loader;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

/**
 * @author linus
 * @since 1.0
 */
public class HUDModule extends ToggleModule
{
    private static HUDModule INSTANCE;
    //
    // private static final HudScreen HUD_SCREEN = new HudScreen();
    //
    Config<Boolean> watermarkConfig = register(new BooleanConfig("Watermark", "Displays client name and version watermark", true));
    Config<Boolean> serverStatusConfig = register(new BooleanConfig("ServerStatus", "Displays warning for server connection status", true));
    Config<Boolean> userInfo = register(new BooleanConfig("UserInfo", "Displays your user information", true));
    Config<Boolean> directionConfig = register(new BooleanConfig("Direction", "Displays facing direction", true));
    Config<Boolean> rotationConfig = register(new BooleanConfig("Rotation", "Displays player yaw and pitch", false, () -> directionConfig.getValue()));
    Config<Boolean> armorConfig = register(new BooleanConfig("Armor", "Displays player equipped armor and durability", true));
    Config<Boolean> armorDurabilityConfig = register(new BooleanConfig("ArmorDurability", "Displays player equipped armor durability", false, () -> armorConfig.getValue()));
    Config<Boolean> totemsConfig = register(new BooleanConfig("Totems", "Displays player totems", false));
    Config<VanillaHud> potionHudConfig = register(new EnumConfig<>("PotionHud", "Renders the Minecraft potion Hud", VanillaHud.HIDE, VanillaHud.values()));
    Config<VanillaHud> itemNameConfig = register(new EnumConfig<>("ItemName", "Renders the Minecraft item name display", VanillaHud.HIDE, VanillaHud.values()));
    Config<Boolean> potionEffectsConfig = register(new BooleanConfig("PotionEffects", "Displays active potion effects", true));
    Config<PotionColors> potionColorsConfig = register(new EnumConfig<>("PotionColors", "Displays active potion colors", PotionColors.NORMAL, PotionColors.values(), () -> potionEffectsConfig.getValue()));
    Config<Boolean> durabilityConfig = register(new BooleanConfig("Durability", "Displays the current held items durability", false));
    Config<Boolean> coordsConfig = register(new BooleanConfig("Coords", "Displays world coordinates", true));
    Config<Boolean> netherCoordsConfig = register(new BooleanConfig("NetherCoords", "Displays nether coordinates", true, () -> coordsConfig.getValue()));
    Config<Boolean> serverBrandConfig = register(new BooleanConfig("ServerBrand", "Displays the current server brand", false));
    Config<Boolean> chestsConfig = register(new BooleanConfig("Chests", "Displays the amount of chests in your render distance", false));
    Config<SpeedHud> speedConfig = register(new EnumConfig<>("Speed", "Displays the current movement speed of the player", SpeedHud.K_M_H, SpeedHud.values()));
    Config<Boolean> pingConfig = register(new BooleanConfig("Ping", "Display server response time in ms", true));
    Config<Boolean> tpsConfig = register(new BooleanConfig("TPS", "Displays server ticks per second", true));
    Config<Boolean> packetsConfig = register(new BooleanConfig("Packets", "Displays outbound packets per second", false));
    Config<Boolean> fpsConfig = register(new BooleanConfig("FPS", "Displays game FPS", true));
    Config<Boolean> arraylistConfig = register(new BooleanConfig("Arraylist", "Displays a list of all active modules", true));
    Config<Integer> animTimeConfig = register(new NumberConfig<>("Arraylist-Time", "Timer for the animation", 0, 1000, 2000, () -> false));
    Config<Ordering> orderingConfig = register(new EnumConfig<>("Ordering", "The ordering of the arraylist", Ordering.LENGTH, Ordering.values(), () -> arraylistConfig.getValue()));
    Config<Rendering> renderingConfig = register(new EnumConfig<>("Rendering", "The rendering mode of the HUD", Rendering.UP, Rendering.values()));
    // Rainbow settings
    Config<RainbowMode> rainbowModeConfig = register(new EnumConfig<>("Rainbow", "The rendering mode for rainbow", RainbowMode.OFF, RainbowMode.values()));
    Config<Color> gradientColorConfig = register(new ColorConfig("GradientColor", "The color of the rainbow gradient", Color.WHITE, false, false, () -> rainbowModeConfig.getValue() == RainbowMode.GRADIENT));
    Config<Float> rainbowSpeedConfig = register(new NumberConfig<>("Rainbow-Speed", "The speed for the rainbow color cycling", 0.1f, 50.0f, 100.0f, () -> rainbowModeConfig.getValue() != RainbowMode.OFF));
    Config<Integer> rainbowSaturationConfig = register(new NumberConfig<>("Rainbow-Saturation", "The saturation of rainbow colors", 0, 35, 100, () -> rainbowModeConfig.getValue() != RainbowMode.OFF && rainbowModeConfig.getValue() != RainbowMode.GRADIENT));
    Config<Integer> rainbowBrightnessConfig = register(new NumberConfig<>("Rainbow-Brightness", "The brightness of rainbow colors", 0, 100, 100, () -> rainbowModeConfig.getValue() != RainbowMode.OFF && rainbowModeConfig.getValue() != RainbowMode.GRADIENT));
    Config<Float> rainbowDifferenceConfig = register(new NumberConfig<>("Rainbow-Difference", "The difference offset for rainbow colors", 0.1f, 40.0f, 100.0f, () -> rainbowModeConfig.getValue() != RainbowMode.OFF));

    private final DecimalFormat decimal = new DecimalFormat("0.0");
    private final DecimalFormat decimal2 = new DecimalFormat("0.0#");

    private long rainbowOffset;
    private float topLeft, topRight, bottomLeft, bottomRight;
    private boolean renderingUp;
    private final Animation chatOpenAnimation = new Animation(false, 200L, Easing.LINEAR);
    private final PerSecondCounter fpsCounter = new PerSecondCounter();
    private final Map<String, HudRenderModule> hudRenderModules = new HashMap<>();
    private final Map<PotionData, Animation> hudRenderPotions = new ConcurrentSkipListMap<>();

    private final Timer serverStatus = new CacheTimer();
    private final Animation statusAnimation = new Animation(false, 300L, Easing.LINEAR);

    public HUDModule()
    {
        super("HUD", "Displays the HUD (heads up display) screen.",
                ModuleCategory.CLIENT);
        INSTANCE = this;
    }

    public static HUDModule getInstance()
    {
        return INSTANCE;
    }

    private void arrayListRenderModule(RenderOverlayEvent.Post event, ToggleModule toggleModule, long drawnCount)
    {
        if (toggleModule.getAnimation().getFactor() <= 0.01f || toggleModule.isHidden())
        {
            return;
        }
        HudRenderModule hudRender = hudRenderModules.get(toggleModule.getId());
        if (hudRender != null)
        {
            hudRender.draw(event.getContext(), drawnCount);
        }
    }

    @EventListener
    public void onDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            hudRenderPotions.clear();
        }
    }

    @EventListener
    public void onDisconnect(DisconnectEvent event)
    {
        hudRenderPotions.clear();
    }

    @EventListener
    public void onRenderOpenChat(RenderOpenChatEvent event)
    {
        event.cancel();
        event.setAnimation((float) chatOpenAnimation.getFactor());
    }

    @EventListener
    public void onRenderOverlayPost(RenderOverlayEvent.Post event)
    {
        // Setup arraylist
        if (hudRenderModules.isEmpty())
        {
            for (Module module : Managers.MODULE.getModules())
            {
                if (!(module instanceof ToggleModule toggleModule))
                {
                    continue;
                }
                hudRenderModules.put(module.getId(), new HudRenderModule(toggleModule));
            }
        }

        int updates = MathHelper.clamp(Math.round(240.0f / mc.getCurrentFps()), 1, 10);
        for (int i = 0; i < updates; i++)
        {
            for (HudRenderModule hudRenderModule : hudRenderModules.values())
            {
                hudRenderModule.updateAnimation();
            }
        }

        fpsCounter.updateCounter();
        if (mc.player != null && mc.world != null)
        {
            for (Map.Entry<PotionData, Animation> entry : hudRenderPotions.entrySet())
            {
                PotionData effectInstance = entry.getKey();
                Animation animation = entry.getValue();
                effectInstance.update();
                if (mc.player.getStatusEffect(effectInstance.getType()) == null && animation.getFactor() < 0.01f)
                {
                    hudRenderPotions.remove(effectInstance);
                }
            }
            if (mc.options.hudHidden)
            {
                return;
            }
            Window res = mc.getWindow();

            if (serverStatusConfig.getValue())
            {
                statusAnimation.setState(serverStatus.passed(1000));
                String warning = String.format("§fServer not responding §7(§r%s.s§7)",
                        decimal.format(serverStatus.getElapsedTime() / 1000.0));
                int width = RenderManager.textWidth(warning);
                Color color = ColorUtil.interpolateColor(MathHelper.clamp((serverStatus.getElapsedTime() - 1000.0f) / 5000.0f, 0.0f, 1.0f), Color.RED, Color.GREEN);
                int alpha = (int) (255 * statusAnimation.getFactor());
                RenderManager.renderText(event.getContext(), warning,
                        (res.getScaledWidth() / 2.0f) - (width / 2.0f), 4.0f, ColorUtil.fixTransparency(color.getRGB(), alpha));
            }

            //
            rainbowOffset = 0;
            // Render offsets for each corner of the screen.
            topLeft = 2.0f;
            topRight = topLeft;
            bottomLeft = res.getScaledHeight() - 11.0f;
            bottomRight = bottomLeft;
            // center = res.getScaledHeight() - 11 / 2.0f
            renderingUp = renderingConfig.getValue() == Rendering.UP;
            bottomLeft -= (float) (14.0f * chatOpenAnimation.getFactor());
            bottomRight -= (float) (14.0f * chatOpenAnimation.getFactor());
            if (potionHudConfig.getValue() == VanillaHud.MOVE
                    && !mc.player.getStatusEffects().isEmpty())
            {
                topRight += 27.0f;
            }

            List<Module> modules = Managers.MODULE.getModules();
            long drawnCount = modules.stream().filter(ToggleModule.class::isInstance)
                    .map(ToggleModule.class::cast).filter(m -> !m.isHidden()).count();

            if (watermarkConfig.getValue())
            {
                RenderManager.renderText(event.getContext(), String.format("%s %s (%s%s%s)",
                        ShorelineMod.MOD_NAME, ShorelineMod.MOD_VER, BuildConfig.BUILD_IDENTIFIER,
                                !BuildConfig.BUILD_IDENTIFIER.equals("dev") ? "-" + BuildConfig.BUILD_NUMBER : "",
                                !BuildConfig.HASH.equals("null") ? "-" + BuildConfig.HASH : ""),
                        2.0f, topLeft, getHudColor(drawnCount - rainbowOffset));
                topLeft += RenderManager.textHeight();
            }

            if (userInfo.getValue())
            {
                RenderManager.renderText(
                        event.getContext(),
                        String.format("UID %s", Loader.SESSION.getUID()),
                        2.0F,
                        topLeft,
                        getHudColor(drawnCount - rainbowOffset)
                );

                topLeft += RenderManager.textHeight();
            }

            if (arraylistConfig.getValue())
            {
                Stream<ToggleModule> moduleStream = modules.stream()
                        .filter(ToggleModule.class::isInstance)
                        .map(ToggleModule.class::cast);
                moduleStream = switch (orderingConfig.getValue())
                {
                    case ALPHABETICAL -> StreamUtils.sortCached(moduleStream, Module::getName);
                    case LENGTH ->
                            StreamUtils.sortCached(moduleStream, m -> -RenderManager.textWidth(getFormattedModule(m)));
                };
                moduleStream.forEach(t -> arrayListRenderModule(event, t, drawnCount));
            }
            if (coordsConfig.getValue())
            {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                boolean nether = mc.world.getRegistryKey() == World.NETHER;
                RenderManager.renderText(event.getContext(), String.format(
                                "XYZ §f%s, %s, %s " + (netherCoordsConfig.getValue() ?
                                        "§7[§f%s, %s§7]" : ""),
                                decimal.format(x),
                                decimal.format(y),
                                decimal.format(z),
                                nether ? decimal.format(x * 8) : decimal.format(x / 8),
                                nether ? decimal.format(z * 8) : decimal.format(z / 8)),
                        2, bottomLeft, getHudColor(rainbowOffset));
                bottomLeft -= RenderManager.textHeight();
            }
            if (directionConfig.getValue())
            {
                final Direction direction = mc.player.getHorizontalFacing();
                String dir = EnumFormatter.formatDirection(direction);
                String axis = EnumFormatter.formatAxis(direction.getAxis());
                boolean pos = direction.getDirection() == Direction.AxisDirection.POSITIVE;
                String rotationText = String.format(", %s", (int) MathHelper.wrapDegrees(Managers.ROTATION.getServerYaw()));
                RenderManager.renderText(event.getContext(),
                        String.format("%s §7[§f%s%s%s§7]", dir, axis,
                                pos ? "+" : "-", rotationConfig.getValue() ? rotationText : ""),2, bottomLeft,
                        getHudColor(rainbowOffset));
                // bottomLeft -= RenderManager.textHeight();
            }
            if (potionEffectsConfig.getValue())
            {
                for (Map.Entry<PotionData, Animation> e1 : hudRenderPotions.entrySet())
                {
                    PotionData e = e1.getKey();
                    Animation animation = e1.getValue();
                    final StatusEffect effect = e.getType().value();
                    if (effect == StatusEffects.NIGHT_VISION)
                    {
                        continue;
                    }
                    boolean infinite = e.getDuration() == -1;
                    boolean amplifier = e.getAmplifier() + 1 > 1 && !infinite;
                    String duration;
                    if (infinite)
                    {
                        duration = "Inf";
                    }
                    else
                    {
                        int i = MathHelper.floor((float) e.getDuration());
                        duration = StringHelper.formatTicks(i, mc.world.getTickManager().getTickRate());
                    }

                    String text = String.format("%s %s§f%s",
                            effect.getName().getString(),
                            amplifier ? e.getAmplifier() + 1 + " " : "", duration);
                    int width = RenderManager.textWidth(text);
                    float x = (width + 1.0f) * (float) animation.getFactor();
                    int potionColor = switch (potionColorsConfig.getValue())
                    {
                        case NORMAL -> ColorUtil.withAlpha(effect.getColor(), (int) (255 * animation.getFactor()));
                        case OLD -> ColorUtil.withAlpha(getLiquidColor(e.getType().getIdAsString(), effect), (int) (255 * animation.getFactor()));
                        case OFF -> ColorUtil.withAlpha(getHudColor(rainbowOffset), (int) (255 * animation.getFactor()));
                    };
                    RenderManager.renderText(event.getContext(), text,
                            res.getScaledWidth() - x, renderingUp ? bottomRight : topRight,
                            ColorUtil.fixTransparency(potionColor, (float) animation.getFactor()));
                    if (renderingUp)
                    {
                        bottomRight -= RenderManager.textHeight();
                    }
                    else
                    {
                        topRight += RenderManager.textHeight();
                    }
                    rainbowOffset++;
                }
            }
            if (serverBrandConfig.getValue())
            {
                String brand = mc.player.networkHandler.getBrand();
                int width = RenderManager.textWidth(brand);
                RenderManager.renderText(event.getContext(), brand,
                        res.getScaledWidth() - width - 1.0f, renderingUp ? bottomRight : topRight,
                        getHudColor(rainbowOffset));
                if (renderingUp)
                {
                    bottomRight -= RenderManager.textHeight();
                }
                else
                {
                    topRight += RenderManager.textHeight();
                }
                rainbowOffset++;
            }
            if (speedConfig.getValue() != SpeedHud.OFF)
            {
                double speed;
                double x = mc.player.getX() - mc.player.prevX;
                // double y = mc.player.getY() - mc.player.prevY;
                double z = mc.player.getZ() - mc.player.prevZ;
                float timer = TimerModule.getInstance().isEnabled() ? TimerModule.getInstance().getTimer() : 1.0f;
                if (speedConfig.getValue() == SpeedHud.K_M_H)
                {
                    double dist = Math.sqrt(x * x + z * z) / 1000.0;
                    double div = 0.05 / 3600.0;
                    speed = dist / div * timer;
                }
                else
                {
                    x *= 20.0;
                    z *= 20.0;
                    double dist = Math.sqrt(x * x + z * z);
                    speed = Math.abs(dist) * timer;
                }
                String text = String.format("Speed §f%s%s", decimal2.format(speed), speedConfig.getValue() == SpeedHud.K_M_H ? "km/h" : "b/s");
                int width = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text,
                        res.getScaledWidth() - width - 1.0f, renderingUp ? bottomRight : topRight,
                        getHudColor(rainbowOffset));
                if (renderingUp)
                {
                    bottomRight -= RenderManager.textHeight();
                }
                else
                {
                    topRight += RenderManager.textHeight();
                }
                rainbowOffset++;
            }
            if (durabilityConfig.getValue() && mc.player.getMainHandStack().isDamageable())
            {
                int n = mc.player.getMainHandStack().getMaxDamage();
                int n2 = mc.player.getMainHandStack().getDamage();
                String text1 = "Durability ";
                String text2 = String.valueOf(n - n2);
                int width = RenderManager.textWidth(text1);
                int width2 = RenderManager.textWidth(text2);
                Color color = ColorUtil.hslToColor((float) (n - n2) / (float) n * 120.0f, 100.0f, 50.0f, 1.0f);
                RenderManager.renderText(event.getContext(), text1,
                        res.getScaledWidth() - width - width2 - 1.0f, renderingUp ? bottomRight : topRight,
                        getHudColor(rainbowOffset));
                RenderManager.renderText(event.getContext(), text2,
                        res.getScaledWidth() - width2 - 1.0f, renderingUp ? bottomRight : topRight,
                        color.getRGB());
                if (renderingUp)
                {
                    bottomRight -= RenderManager.textHeight();
                }
                else
                {
                    topRight += RenderManager.textHeight();
                }
                rainbowOffset++;
            }
            if (pingConfig.getValue() && !mc.isInSingleplayer())
            {
                int latency = FastLatencyModule.getInstance().isEnabled() ? (int) FastLatencyModule.getInstance().getLatency() : Managers.NETWORK.getClientLatency();
                String text = String.format("Ping §f%dms", latency);
                int width = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text,
                        res.getScaledWidth() - width - 1.0f, renderingUp ? bottomRight : topRight,
                        getHudColor(rainbowOffset));
                if (renderingUp)
                {
                    bottomRight -= RenderManager.textHeight();
                }
                else
                {
                    topRight += RenderManager.textHeight();
                }
                rainbowOffset++;
            }
            if (packetsConfig.getValue())
            {
                String text = String.format("Packets §f%s<-%s", Managers.NETWORK.getOutgoingPPS(), Managers.NETWORK.getIncomingPPS());
                int width = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text,
                        res.getScaledWidth() - width - 1.0f, renderingUp ? bottomRight : topRight,
                        getHudColor(rainbowOffset));
                if (renderingUp)
                {
                    bottomRight -= RenderManager.textHeight();
                }
                else
                {
                    topRight += RenderManager.textHeight();
                }
                rainbowOffset++;
            }
            if (tpsConfig.getValue())
            {
                float curr = Managers.TICK.getTpsCurrent();
                float avg = Managers.TICK.getTpsAverage();
                String text = String.format("TPS §f%s §7[§f%s§7]",
                        decimal2.format(curr) + (Managers.TICK.isTicksFilled() ? "" : "*"),
                        decimal2.format(avg));
                int width = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text,
                        res.getScaledWidth() - width - 1.0f, renderingUp ? bottomRight : topRight,
                        getHudColor(rainbowOffset));
                if (renderingUp)
                {
                    bottomRight -= RenderManager.textHeight();
                }
                else
                {
                    topRight += RenderManager.textHeight();
                }
                rainbowOffset++;
            }
            if (chestsConfig.getValue())
            {
                int singleChests = 0;
                int doubleChests = 0;
                for (BlockEntity blockEntity : BlockUtil.blockEntities())
                {
                    if (blockEntity instanceof ChestBlockEntity)
                    {
                        BlockState state = blockEntity.getCachedState();
                        if (state.contains(ChestBlock.CHEST_TYPE))
                        {
                            ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
                            if (chestType == ChestType.SINGLE)
                            {
                                singleChests++;
                            }
                            else
                            {
                                doubleChests++;
                            }
                        }
                    }
                }
                String text = String.format("Chests §f%d", singleChests + (doubleChests / 2));
                int width = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text,
                        res.getScaledWidth() - width - 1.0f, renderingUp ? bottomRight : topRight,
                        getHudColor(rainbowOffset));
                if (renderingUp)
                {
                    bottomRight -= RenderManager.textHeight();
                }
                else
                {
                    topRight += RenderManager.textHeight();
                }
                rainbowOffset++;
            }
            if (fpsConfig.getValue())
            {
                String text = String.format("FPS §f%d", fpsCounter.getPerSecond());
                int width = RenderManager.textWidth(text);
                RenderManager.renderText(event.getContext(), text,
                        res.getScaledWidth() - width - 1.0f, renderingUp ? bottomRight : topRight,
                        getHudColor(rainbowOffset));
                // bottomRight -= RenderManager.textHeight();
                rainbowOffset++;
            }
            if (armorConfig.getValue())
            {
                final Entity riding = mc.player.getVehicle();
                //
                int x = res.getScaledWidth() / 2 + 15;
                int y = res.getScaledHeight();
                int n1 = mc.player.getMaxAir();
                int n2 = Math.min(mc.player.getAir(), n1);
                if (mc.player.isSubmergedIn(FluidTags.WATER) || n2 < n1)
                {
                    y -= 65;
                }
                else if (riding instanceof LivingEntity entity)
                {
                    y -= 45 + (int) Math.ceil((entity.getMaxHealth() - 1.0f) / 20.0f) * 10;
                }
                else if (mc.player.isCreative())
                {
                    y -= 45;
                }
                else
                {
                    y -= 55;
                }
                for (int i = 3; i >= 0; --i)
                {
                    ItemStack armor = mc.player.getInventory().armor.get(i);
                    int f = armor.getMaxDamage();
                    int f2 = armor.getDamage();
                    event.getContext().drawItem(armor, x, y);
                    event.getContext().drawItemInSlot(mc.textRenderer, armor, x, y);
                    if (armorDurabilityConfig.getValue() && !armor.isEmpty())
                    {
                        event.getContext().getMatrices().scale(0.65f, 0.65f, 1.0f);
                        RenderManager.renderText(event.getContext(), Math.round(((f - f2) / (float) f) * 100.0f) + "%", (x + 2.0f) * 1.53846154f, (y - 5.0f) * 1.53846154f, ColorUtil.hslToColor((float) (f - f2) / (float) f * 120.0f, 100.0f, 50.0f, 1.0f).getRGB());
                        event.getContext().getMatrices().scale(1.53846154f, 1.53846154f, 1.0f);
                    }
                    x += 18;
                }
            }

            if (totemsConfig.getValue() && !mc.player.isCreative() && !mc.player.isSpectator())
            {
                int x = res.getScaledWidth() / 2 - 8;
                int y = res.getScaledHeight() - 55;

                event.getContext().drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), x, y - 1);
                String totems = String.valueOf(InventoryUtil.count(Items.TOTEM_OF_UNDYING));

                event.getContext().getMatrices().scale(0.75f, 0.75f, 1.0f);
                event.getContext().getMatrices().translate(0.0f, 0.0f, 200.0f);
                RenderManager.renderText(event.getContext(), totems, (x + 19 - RenderManager.textWidth(totems)) * 1.333333f, (y + 9) * 1.333333f, -1);
                event.getContext().getMatrices().scale(1.333333f, 1.333333f, 1.0f);
            }
        }
    }

    @EventListener
    public void onStatusEffectAdd(StatusEffectEvent.Add event)
    {
        StatusEffectInstance instance = event.getStatusEffect();
        PotionData data = new PotionData(instance.getEffectType(), instance.getAmplifier(), instance.getDuration());
        if (hudRenderPotions.keySet().removeIf(d -> data.getType() == d.getType()))
        {
            hudRenderPotions.put(data, new Animation(true, 250, Easing.SINE_IN_OUT));
        }
        else
        {
            Animation anim = new Animation(false, 250, Easing.SINE_IN_OUT);
            anim.setState(true);
            hudRenderPotions.put(data, anim);
        }
    }

    @EventListener
    public void onStatusEffectRemove(StatusEffectEvent.Remove event)
    {
        for (PotionData data : hudRenderPotions.keySet())
        {
            if (data.getType().equals(event.getType()))
            {
                hudRenderPotions.get(data).setState(false);
                break;
            }
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        serverStatus.reset();
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (mc.world == null || mc.isPaused())
        {
            serverStatus.reset();
        }
    }

    @EventListener
    public void onChatOpen(ScreenOpenEvent event)
    {
        if (event.getScreen() == null && chatOpenAnimation.getState())
        {
            chatOpenAnimation.setState(false);
        }
        else if (event.getScreen() instanceof ChatScreen)
        {
            chatOpenAnimation.setState(true);
        }
    }

    @EventListener
    public void onRenderOverlayStatusEffect(RenderOverlayEvent.StatusEffect event)
    {
        if (potionHudConfig.getValue() == VanillaHud.HIDE)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onRenderOverlayItemName(RenderOverlayEvent.ItemName event)
    {
        if (itemNameConfig.getValue() != VanillaHud.KEEP)
        {
            event.cancel();
        }
        if (itemNameConfig.getValue() == VanillaHud.MOVE)
        {
            final Window window = mc.getWindow();
            int x = window.getScaledWidth() / 2 - 90;
            int y = window.getScaledHeight() - 49;
            boolean armor = !mc.player.getInventory().armor.isEmpty();
            if (mc.player.getAbsorptionAmount() > 0.0f)
            {
                y -= 9;
            }
            if (armor)
            {
                y -= 9;
            }
            event.setX(x);
            event.setY(y);
        }
    }

    private int getHudColor(long rainbowOffset)
    {
        return switch (rainbowModeConfig.getValue())
        {
            case OFF -> ColorsModule.getInstance().getRGB();
            case STATIC_HUE -> rainbow(1L);
            case GRADIENT_HUE -> rainbow(rainbowOffset);
            case GRADIENT ->
            {
                float speed = Math.max(100.0f - rainbowSpeedConfig.getValue(), 0.1f);
                float difference = Math.max(100.0f - rainbowDifferenceConfig.getValue(), 0.1f);
                double roundY = Math.sin(Math.toRadians((rainbowOffset * difference) + ((double) System.currentTimeMillis() / speed)));
                roundY = Math.abs(roundY);
                yield ColorUtil.interpolateColor((float) MathHelper.clamp(roundY, 0.0f, 1.0f), ColorsModule.getInstance().getColor(), gradientColorConfig.getValue()).getRGB();
            }
        };
    }

    private String getFormattedModule(final Module module)
    {
        return module.getName() + getFormattedModuleData(module.getModuleData());
    }

    private String getFormattedModuleData(final String metadata)
    {
        if (!metadata.equals("ARRAYLIST_INFO"))
        {
            return " §7[§f" + metadata + "§7]";
        }
        return "";
    }

    public int getLiquidColor(String name, StatusEffect effect)
    {
        return switch (name.replace("minecraft:", ""))
        {
            case "speed" -> 8171462;
            case "slowness" -> 5926017;
            case "haste" -> 14270531;
            case "mining_fatigue" -> 4866583;
            case "strength" -> 9643043;
            case "instant_health" -> 16262179;
            case "instant_damage" -> 4393481;
            case "jump_boost" -> 2293580;
            case "nausea" -> 5578058;
            case "regeneration" -> 13458603;
            case "resistance" -> 10044730;
            case "fire_resistance" -> 14981690;
            case "water_breathing" -> 3035801;
            case "invisibility" -> 8356754;
            case "blindness" -> 2039587;
            case "night_vision" -> 2039713;
            case "hunger" -> 5797459;
            case "weakness" -> 4738376;
            case "poison" -> 5149489;
            case "wither" -> 3484199;
            case "health_boost" -> 16284963;
            case "absorption" -> 2445989;
            case "saturation" -> 16262179;
            case "glowing" -> 9740385;
            case "levitation" -> 13565951;
            case "luck" -> 3381504;
            case "unluck" -> 12624973;
            default -> effect.getColor();
        };
    }

    private int rainbow(long offset)
    {
        float hue = (float) (((double) System.currentTimeMillis() * (rainbowSpeedConfig.getValue() / 10)
                + (double) (offset * 500L)) % (30000 / (rainbowDifferenceConfig.getValue() / 100))
                / (30000 / (rainbowDifferenceConfig.getValue() / 20.0f)));
        return Color.HSBtoRGB(hue, rainbowSaturationConfig.getValue() / 100.0f,
                rainbowBrightnessConfig.getValue() / 100.0f);
    }

    public int alpha(long offset)
    {
        float[] hsb = new float[3];
        Color color = ColorsModule.getInstance().getColor();
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float b = (float) (((double) System.currentTimeMillis() * (rainbowSpeedConfig.getValue() / 10)
                + (double) (offset * 500L)) % (30000 / (rainbowDifferenceConfig.getValue() / 100))
                / (30000 / (rainbowDifferenceConfig.getValue() / 20.0f)));
        float brightness = hsb[2] * Math.abs(b % 1.0f - 0.55f) + 0.4f;
        return Color.HSBtoRGB(hsb[0], hsb[1], brightness);
    }

    public float getChatAnimation()
    {
        return (float) chatOpenAnimation.getFactor();
    }

    public enum VanillaHud
    {
        MOVE,
        HIDE,
        KEEP
    }

    public enum Ordering
    {
        LENGTH,
        ALPHABETICAL
    }

    public enum Rendering
    {
        UP,
        DOWN
    }

    public enum RainbowMode
    {
        OFF,
        GRADIENT,
        GRADIENT_HUE,
        STATIC_HUE
    }

    public enum SpeedHud
    {
        K_M_H,
        B_P_S,
        OFF
    }

    public enum PotionColors
    {
        NORMAL,
        OLD,
        OFF
    }

    public class HudRenderModule
    {
        private final ToggleModule module;
        private double x;

        private long startTime;

        private double endpoint;
        private int prevTextWidth;

        private boolean wasDrawing;

        public HudRenderModule(ToggleModule module)
        {
            this.module = module;
        }

        public void updateAnimation()
        {
            String text = getFormattedModule(module);
            int textWidth = RenderManager.textWidth(text);

            boolean drawing = module.isEnabled() && !module.isHidden();
            if (drawing != wasDrawing || prevTextWidth != textWidth)
            {
                startTime = System.currentTimeMillis();

                if (drawing)
                {
                    endpoint = -textWidth - 2.0f;
                }
                else
                {
                    endpoint = 2.0f;
                }
                wasDrawing = drawing;
                prevTextWidth = textWidth;
            }

            double animationProgress = Math.min((System.currentTimeMillis() - startTime) / (float) animTimeConfig.getValue(), 1.0);
            double factor = Easing.BOUNCE_IN_OUT.ease(animationProgress);
            this.x = this.x * (1.0 - factor) + (endpoint * factor);
        }

        public void draw(DrawContext context, long drawnCount)
        {
            int color = ColorUtil.fixTransparency(getHudColor(drawnCount - rainbowOffset), (float) module.getAnimation().getFactor());
            RenderManager.renderText(context, getFormattedModule(module),
                     mc.getWindow().getScaledWidth() + (float) this.x, renderingUp ? topRight : bottomRight, color);

            if (renderingUp)
            {
                topRight += RenderManager.textHeight();
            }
            else
            {
                bottomRight -= RenderManager.textHeight();
            }
            rainbowOffset++;
        }
    }

    public static class PotionData implements Comparable<PotionData>
    {
        private final RegistryEntry<StatusEffect> type;
        private final int amplifier;
        private int duration;

        public PotionData(RegistryEntry<StatusEffect> type, int amplifier, int duration)
        {
            this.type = type;
            this.amplifier = amplifier;
            this.duration = duration;
        }

        public void update()
        {
            StatusEffectInstance instance = mc.player.getStatusEffect(type);
            if (instance != null)
            {
                duration = instance.getDuration();
            }
        }

        public String getPotionName()
        {
            return getType().value().getName().getString();
        }

        public RegistryEntry<StatusEffect> getType()
        {
            return type;
        }

        public int getAmplifier()
        {
            return amplifier;
        }

        public int getDuration()
        {
            return duration;
        }

        @Override
        public int compareTo(HUDModule.PotionData other)
        {
            return other.getPotionName().compareTo(getPotionName());
        }
    }
}