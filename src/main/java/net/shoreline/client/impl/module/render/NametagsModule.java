package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.entity.RenderEntityLabelEvent;
import net.shoreline.client.impl.imixin.IItemRenderState;
import net.shoreline.client.impl.imixin.IItemRenderer;
import net.shoreline.client.impl.imixin.ILayerRenderState;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.client.impl.module.impl.RenderModule;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Interpolation;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.eventbus.annotation.EventListener;
import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

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
    Config<Boolean> itemConfig = new BooleanConfig.Builder("Item")
            .setDescription("Displays the name of the players equipped stack")
            .setDefaultValue(true).build();
    Config<Boolean> armorConfig = new BooleanConfig.Builder("Armor")
            .setDescription("Displays the players equipped armor")
            .setDefaultValue(true).build();
    Config<Boolean> enchantmentsConfig = new BooleanConfig.Builder("Enchantments")
            .setDescription("Displays the enchantments of equipped armor")
            .setVisible(armorConfig::getValue)
            .setDefaultValue(true).build();
    Config<Float> scalingConfig = new NumberConfig.Builder<Float>("Scaling")
            .setMin(0.001f).setMax(0.01f).setDefaultValue(0.003f).build();

    private final List<PlayerEntry> players = new ArrayList<>();
    private final ItemRenderState itemRenderState = new ItemRenderState();

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

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(1.0f, -32500000);
        for (PlayerEntry playerEntry : players)
        {
            PlayerEntity player = playerEntry.getPlayer();
            if (!Managers.RENDER.isVisible(player.getBoundingBox()))
            {
                continue;
            }

            float yOff = 2.2f;
            if (player.isSneaking())
            {
                yOff = 2.0f;
            } else if (player.isCrawling())
            {
                yOff = 1.2f;
            }

            String info = playerEntry.getInfo();
            Vec3d interp = Interpolation.getRenderPosition(player, event.getTickDelta());
            double x = interp.x - camera.getPos().x;
            double y = interp.y + yOff - camera.getPos().y;
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

            float hwidth = getTextWidth(info) / 2.0f;
            int color = getNametagColor(player);
            drawText(matrices, info, (int) -hwidth, 0, color);
            renderItems(matrices, player, armorConfig.getValue());
            matrices.pop();
        }

        RenderSystem.disablePolygonOffset();
        RenderSystem.polygonOffset(1.0f, 32500000);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
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

            if (entity == mc.player && !FreecamModule.INSTANCE.isEnabled())
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

    private void renderItems(MatrixStack matrices, PlayerEntity player, boolean icons)
    {
        List<ItemStack> displayItems = new CopyOnWriteArrayList<>();
        if (!player.getOffHandStack().isEmpty())
        {
            displayItems.add(player.getOffHandStack());
        }

        player.getInventory().armor.forEach(armorStack ->
        {
            if (!armorStack.isEmpty())
            {
                displayItems.add(armorStack);
            }
        });

        if (!player.getMainHandStack().isEmpty())
        {
            displayItems.add(player.getMainHandStack());
        }

        Collections.reverse(displayItems);
        float xOffset = 0;
        int yOffset = 0;
        for (ItemStack stack : displayItems)
        {
            xOffset -= 8;
            if (stack.getEnchantments().getEnchantments().size() > yOffset)
            {
                yOffset = stack.getEnchantments().getEnchantments().size();
            }
        }

        float enchY = icons ? enchantOffset(yOffset) : -5.0f;
        for (ItemStack stack : displayItems)
        {
            if (icons)
            {
                matrices.push();
                matrices.translate(xOffset + 8.0f, enchY + 8.0f, 0.0f);
                matrices.scale(16.0f, 16.0f, 0.0f);
                matrices.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
                renderItem(stack, matrices);
                matrices.pop();
            }

            matrices.push();
            if (icons)
            {
                renderItemOverlay(matrices, stack, (int) xOffset, (int) enchY);
            }

            matrices.scale(0.5f, 0.5f, 0.5f);
            renderDurability(matrices, stack, xOffset + 2.0f, enchY - 4.5f);
            if (enchantmentsConfig.getValue() && icons)
            {
                renderEnchants(matrices, stack, xOffset + 2.0f, enchY);
            }

            matrices.scale(2.0f, 2.0f, 2.0f);
            matrices.pop();
            xOffset += 16;
        }

        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.isEmpty())
        {
            return;
        }

        matrices.scale(0.5f, 0.5f, 0.5f);
        if (itemConfig.getValue())
        {
            renderItemName(matrices, heldItem, 0, enchY - 10.0f);
        }

        matrices.scale(2.0f, 2.0f, 2.0f);
    }

    private void renderEnchants(MatrixStack matrixStack, ItemStack itemStack, float x, float y)
    {
        if (!itemStack.hasEnchantments())
        {
            return;
        }

        Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> enchants = EnchantmentHelper.getEnchantments(itemStack).getEnchantmentEntries();
        float n2 = 0;
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> e : enchants)
        {
            int lvl = e.getIntValue();
            StringBuilder enchantString = new StringBuilder();
            String translatedName = Enchantment.getName(e.getKey(), lvl).getString();
            if (translatedName.contains("Vanish"))
            {
                enchantString.append("Van");
            }
            else if (translatedName.contains("Bind"))
            {
                enchantString.append("Bind");
            }
            else
            {
                int maxLen = lvl > 1 ? 2 : 3;
                if (translatedName.length() > maxLen)
                {
                    translatedName = translatedName.substring(0, maxLen);
                }
                enchantString.append(translatedName);
                enchantString.append(lvl);
            }

            drawText(matrixStack, enchantString.toString(), (int) (x * 2), (int) ((y + n2) * 2), -1);
            n2 += 4.5f;
        }
    }

    private void renderItemOverlay(MatrixStack matrixStack, ItemStack stack, int x, int y)
    {
        if (stack.getCount() != 1)
        {
            String count = String.valueOf(stack.getCount());
            drawText(matrixStack, count, x + 17 - getTextWidth(count), y + 9.0f, -1);
        }

        if (stack.isItemBarVisible())
        {
            int i = (int) Math.clamp(stack.getItemBarStep() * 0.923076923, 0, 12);
            int j = stack.getItemBarColor();
            Managers.RENDER.drawRect(matrixStack, x + 3, y + 13, 12, 1, Colors.BLACK);
            Managers.RENDER.drawRect(matrixStack, x + 3, y + 13, i, 1, j | Colors.BLACK);
        }
    }

    private void renderDurability(MatrixStack matrixStack, ItemStack itemStack, float x, float y)
    {
        if (!itemStack.isDamageable())
        {
            return;
        }

        int n = itemStack.getMaxDamage();
        int n2 = itemStack.getDamage();
        int durability = (int) ((n - n2) / ((float) n) * 100.0f);
        int color = ColorUtil.hslToColor((float) (n - n2) / (float) n * 120.0f, 100.0f, 50.0f, 1.0f).getRGB();
        drawText(matrixStack, durability + "%", (int) (x * 2), (int) (y * 2), color);
    }

    private void renderItem(ItemStack stack, MatrixStack matrices)
    {
        VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
        ((IItemRenderer) mc.getItemRenderer()).getItemModelManager().update(itemRenderState, stack, ModelTransformationMode.GUI, false, mc.world, mc.player, 0);
        RenderLayer renderLayer = RenderLayer.getGuiTextured(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        for (int i = 0; i < ((IItemRenderState) itemRenderState).getLayerCount(); i++)
        {
            ItemRenderState.LayerRenderState layer = ((IItemRenderState) itemRenderState).getLayers()[i];
            ((ILayerRenderState) layer).setRenderLayer(renderLayer);
        }

        itemRenderState.render(matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
        vertexConsumers.draw();
    }

    private void renderItemName(MatrixStack matrixStack, ItemStack itemStack, float x, float y)
    {
        String itemName = itemStack.getName().getString();
        float width = getTextWidth(itemName) / 4.0f;
        drawText(matrixStack, itemName, (int) ((x - width) * 2), (int) (y * 2), -1);
    }

    private float enchantOffset(final int n)
    {
        if (!enchantmentsConfig.getValue() || n <= 3)
        {
            return -18.0f;
        }

        float n2 = -14.0f;
        n2 -= (n - 3) * 4.5f;
        return n2;
    }

    private int getNametagColor(PlayerEntity player)
    {
        if (player == mc.player)
        {
            return ThemeModule.INSTANCE.getPrimaryColor().getRGB();
        }

        if (Managers.SOCIAL.isFriend(player))
        {
            return SocialsModule.INSTANCE.getFriendsColor().getRGB();
        }

        if (player.isInvisible())
        {
            return 0xffff2500;
        }

        if (player instanceof FakePlayerEntity)
        {
            return 0xffef0147;
        }

        if (player.isSneaking())
        {
            return 0xffff9900;
        }

        return 0xffffffff;
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
                    builder.append(Formatting.WHITE);
                    builder.append(-totems);
                    builder.append(" ");
                }
            }

            info = builder.toString().trim();
        }
    }
}
