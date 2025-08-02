package net.shoreline.client.impl.module.render;

import lombok.Getter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.entity.RenderEntityLabelEvent;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.Interpolation;
import net.shoreline.eventbus.annotation.EventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class NametagsModule extends RenderModule
{
    public static NametagsModule INSTANCE;

    Config<Boolean> entityIdConfig = new BooleanConfig.Builder("EntityId")
            .setDescription("Displays the players entity id")
            .setDefaultValue(false).build();

    Config<Boolean> gamemodeConfig = new BooleanConfig.Builder("Gamemode")
            .setDescription("Displays the players gamemode")
            .setDefaultValue(false).build();

    Config<Boolean> pingConfig = new BooleanConfig.Builder("Ping")
            .setDescription("Displays the players ping")
            .setDefaultValue(true).build();

    Config<Boolean> healthConfig = new BooleanConfig.Builder("Health")
            .setDescription("Displays the players current health")
            .setDefaultValue(true).build();

    Config<Boolean> totemsConfig = new BooleanConfig.Builder("Totems")
            .setDescription("Displays the totem count")
            .setDefaultValue(true).build();

    Config<Float> scalingConfig = new NumberConfig.Builder<Float>("Scaling")
            .setMin(0.001f).setMax(0.01f).setDefaultValue(0.003f).build();

    private final List<PlayerEntry> players = new ArrayList<>();

    public NametagsModule()
    {
        super("Nametags", "Adds info to player nametags", GuiCategory.RENDER);
        INSTANCE = this;
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent.Post event)
    {
        if (mc.gameRenderer == null || mc.getCameraEntity() == null)
        {
            return;
        }

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.getEntityRenderDispatcher().camera;
        for (PlayerEntry playerEntry : players)
        {
            PlayerEntity player = playerEntry.getPlayer();
            String info = playerEntry.getInfo();

            Vec3d interp = Interpolation.getRenderPosition(player, event.getTickDelta());
            double x = interp.x - camera.getPos().x;
            double y = interp.y + (player.isSneaking() ? 2.0f : 2.2f) - camera.getPos().y;
            double z = interp.z - camera.getPos().z;
            float distance = (float) Math.sqrt(camera.getPos().squaredDistanceTo(interp.x, interp.y, interp.z));
            float scaling = 0.0018f + scalingConfig.getValue() * distance;
            if (distance <= 8.0)
            {
                scaling = 0.0245f;
            }

            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
            matrices.scale(scaling, -scaling, scaling);

            float hwidth = mc.textRenderer.getWidth(info) / 2f;
            mc.textRenderer.draw(info, -hwidth, 0, 0xFFFFFFFF, true, matrices.peek().getPositionMatrix(), mc.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            mc.getBufferBuilders().getEntityVertexConsumers().draw();

            matrices.pop();
        }
    }

    @EventListener
    public void onTickPost(TickEvent.Post event)
    {
        players.clear();
        if (checkNull())
        {
            return;
        }

        for (Entity entity : mc.world.getEntities())
        {
            if (!(entity instanceof PlayerEntity playerEntity) )
            {
                continue;
            }

            if (entity == mc.player)
            {
                continue;
            }

            players.add(new PlayerEntry(playerEntity));
        }
    }

    @EventListener
    public void onRenderEntityLabel(RenderEntityLabelEvent event)
    {
        event.cancel();
    }

    @Getter
    public class PlayerEntry
    {
        private final PlayerEntity player;
        private final String info;

        public PlayerEntry(PlayerEntity player)
        {
            this.player = player;
            StringBuilder builder = new StringBuilder(player.getName().getString());
            builder.append(" ");
            if (entityIdConfig.getValue())
            {
                builder.append("ID: ").append(player.getId()).append(" ");
            }

            if (gamemodeConfig.getValue())
            {
                if (player.isCreative())
                {
                    builder.append("[C] ");
                }
                else if (player.isSpectator())
                {
                    builder.append("[I] ");
                }
                else
                {
                    builder.append("[S] ");
                }
            }

            if (pingConfig.getValue() && mc.getNetworkHandler() != null)
            {
                PlayerListEntry playerEntry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId());
                if (playerEntry != null)
                {
                    builder.append(playerEntry.getLatency());
                    builder.append("ms ");
                }
            }

            if (healthConfig.getValue())
            {
                double health = player.getHealth() + player.getAbsorptionAmount();

                Formatting hcolor;
                if (health > 18)
                {
                    hcolor = Formatting.GREEN;
                }
                else if (health > 16)
                {
                    hcolor = Formatting.DARK_GREEN;
                }
                else if (health > 12)
                {
                    hcolor = Formatting.YELLOW;
                }
                else if (health > 8)
                {
                    hcolor = Formatting.GOLD;
                }
                else if (health > 4)
                {
                    hcolor = Formatting.RED;
                }
                else
                {
                    hcolor = Formatting.DARK_RED;
                }

                BigDecimal bigDecimal = new BigDecimal(health);
                bigDecimal = bigDecimal.setScale(1, RoundingMode.HALF_UP);
                builder.append(hcolor);
                builder.append(bigDecimal.doubleValue());
                builder.append(" ");
            }

            if (totemsConfig.getValue() && player != mc.player)
            {
                int totems = Managers.TOTEM.getTotems(player);
                if (totems > 0)
                {
                    Formatting pcolor = Formatting.GREEN;

                    if (totems > 1)
                    {
                        pcolor = Formatting.DARK_GREEN;
                    }
                    if (totems > 2)
                    {
                        pcolor = Formatting.YELLOW;
                    }
                    if (totems > 3)
                    {
                        pcolor = Formatting.GOLD;
                    }
                    if (totems > 4)
                    {
                        pcolor = Formatting.RED;
                    }
                    if (totems > 5)
                    {
                        pcolor = Formatting.DARK_RED;
                    }

                    builder.append(pcolor);
                    builder.append(-totems);
                    builder.append(" ");
                }
            }

            info = builder.toString().trim();
        }
    }
}
