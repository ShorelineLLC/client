package net.shoreline.client.impl.module.render;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.api.render.layers.RenderLayersClient;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.event.render.entity.RenderLabelEvent;
import net.shoreline.client.impl.event.world.PlaySoundEvent;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.client.FontModule;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.init.Fonts;
import net.shoreline.client.init.Managers;
import net.shoreline.client.mixin.accessor.AccessorItemRenderer;
import net.shoreline.client.mixin.accessor.AccessorTextRenderer;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.client.util.render.ColorUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.loader.Loader;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author linus
 * @since 1.0
 */
public class NametagsModule extends ToggleModule
{
    private static NametagsModule INSTANCE;

    Config<Boolean> armorConfig = register(new BooleanConfig("Armor", "Displays the player's armor", true));
    Config<Boolean> itemsConfig = register(new BooleanConfig("Items", "Displays the player's held items", true));
    Config<Boolean> enchantmentsConfig = register(new BooleanConfig("Enchantments", "Displays a list of the item's enchantments", true));
    Config<Boolean> durabilityConfig = register(new BooleanConfig("Durability", "Displays item durability", true));
    Config<Boolean> itemNameConfig = register(new BooleanConfig("ItemName", "Displays the player's current held item name", false));
    Config<Boolean> entityIdConfig = register(new BooleanConfig("EntityId", "Displays the player's entity id", false));
    Config<Boolean> gamemodeConfig = register(new BooleanConfig("Gamemode", "Displays the player's gamemode", false));
    // Pooron check
    Config<Boolean> onlineUsersConfig = register(new BooleanConfig("OnlineUsers", "Displays the online users of Shoreline", true, () -> !Loader.SESSION.getUserType().equals("release")));
    Config<Boolean> pingConfig = register(new BooleanConfig("Ping", "Displays the player's server connection ping", true));
    Config<Boolean> healthConfig = register(new BooleanConfig("Health", "Displays the player's current health", true));
    Config<Boolean> totemsConfig = register(new BooleanConfig("Totems", "Displays the player's popped totem count", false));
    Config<Float> scalingConfig = register(new NumberConfig<>("Scaling", "The nametag label scale", 0.001f, 0.003f, 0.01f));
    Config<Boolean> invisiblesConfig = register(new BooleanConfig("Invisibles", "Renders nametags on invisible players", true));
    Config<Boolean> backgroundConfig = register(new BooleanConfig("Background", "Renders a background behind the nametag", true));
    Config<Boolean> borderedConfig = register(new BooleanConfig("Border", "Renders a border around the nametag", false));
    Config<Boolean> tamedConfig = register(new BooleanConfig("MobOwner", "Renders nametags on tamed mobs", false));
    Config<Boolean> pearlsConfig = register(new BooleanConfig("Pearls", "Renders nametags on thrown ender pearls", false));
    Config<Boolean> droppedItemsConfig = register(new BooleanConfig("DroppedItems", "Renders nametags on dropped items", false));
    Config<Boolean> soundsConfig = register(new BooleanConfig("Sounds", "Renders nametags on sounds", false));

    private final Map<SoundRender, Long> sounds = new HashMap<>();

    public NametagsModule()
    {
        super("Nametags", "Renders info on player nametags", ModuleCategory.RENDER);
        INSTANCE = this;
    }

    public static NametagsModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        sounds.clear();
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (mc.gameRenderer == null || mc.getCameraEntity() == null)
        {
            return;
        }
        RenderBuffers.preRender();
        Vec3d interpolate = Interpolation.getRenderPosition(mc.getCameraEntity(), mc.getRenderTickCounter().getTickDelta(true));
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d pos = camera.getPos();
        for (Entity entity : mc.world.getEntities())
        {
            if (!RenderManager.isFrustumVisible(entity.getBoundingBox()))
            {
                continue;
            }

            if (entity instanceof PlayerEntity player)
            {
                if (player == mc.player && !FreecamModule.getInstance().isEnabled())
                {
                    continue;
                }
                if (!player.isAlive() || !invisiblesConfig.getValue() && player.isInvisible())
                {
                    continue;
                }
                String info = getNametagInfo(player);
                Vec3d pinterpolate = Interpolation.getRenderPosition(player, mc.getRenderTickCounter().getTickDelta(true));
                double rx = player.getX() - pinterpolate.getX();
                double ry = player.getY() - pinterpolate.getY();
                double rz = player.getZ() - pinterpolate.getZ();
                float w1 = FontModule.getInstance().isEnabled() ? Fonts.CLIENT_UNSCALED.getStringWidth(info) : mc.textRenderer.getWidth(info);
                int width = (int) (w1 + (isOnlineUser(player) ? 10 : 0));
                float hwidth = width / 2.0f;
                double dx = (pos.getX() - interpolate.getX()) - rx;
                double dy = (pos.getY() - interpolate.getY()) - ry;
                double dz = (pos.getZ() - interpolate.getZ()) - rz;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > 4096.0)
                {
                    continue;
                }
                float scaling = 0.0018f + scalingConfig.getValue() * (float) dist;
                if (dist <= 8.0)
                {
                    scaling = 0.0245f;
                }
                renderInfo(info, hwidth, player, rx, ry, rz, camera, scaling);
            }
            else if (entity instanceof AbstractHorseEntity tameable && tameable.getOwnerUuid() != null && tamedConfig.getValue())
            {
                String lookup = Managers.LOOKUP.getNameFromUUID(tameable.getOwnerUuid());
                if (lookup != null)
                {
                    Vec3d tamePos = Interpolation.getRenderPosition(entity, mc.getRenderTickCounter().getTickDelta(true));
                    double rx = entity.getX() - tamePos.getX();
                    double ry = (entity.getY() + entity.getHeight() + 0.43f) - tamePos.getY();
                    double rz = entity.getZ() - tamePos.getZ();
                    RenderManager.renderSign("Owner: " + lookup, rx, ry, rz, -1);
                }
            }
            else if (entity instanceof TameableEntity tameable && tameable.getOwnerUuid() != null && tamedConfig.getValue())
            {
                String lookup = Managers.LOOKUP.getNameFromUUID(tameable.getOwnerUuid());
                if (lookup != null)
                {
                    Vec3d tamePos = Interpolation.getRenderPosition(entity, mc.getRenderTickCounter().getTickDelta(true));
                    double rx = entity.getX() - tamePos.getX();
                    double ry = (entity.getY() + entity.getHeight() + 0.43f) - tamePos.getY();
                    double rz = entity.getZ() - tamePos.getZ();
                    RenderManager.renderSign("Owner: " + lookup, rx, ry, rz, -1);
                }
            }
            else if (entity instanceof ItemEntity itemEntity && droppedItemsConfig.getValue())
            {
                Vec3d itemPos = Interpolation.getRenderPosition(itemEntity, mc.getRenderTickCounter().getTickDelta(true));
                double rx = itemEntity.getX() - itemPos.getX();
                double ry = itemEntity.getY() - itemPos.getY();
                double rz = itemEntity.getZ() - itemPos.getZ();
                ItemStack stack = itemEntity.getStack();
                String stackNametag = stack.getName().getString() + (stack.getCount() > 1 ? " x" + stack.getCount() : "");
                RenderManager.renderSign(stackNametag, rx, ry, rz, -1);
            }
            else if (entity instanceof EnderPearlEntity pearlEntity && pearlsConfig.getValue())
            {
                if (pearlEntity.getOwner() == null)
                {
                    continue;
                }
                Vec3d itemPos = Interpolation.getRenderPosition(pearlEntity, mc.getRenderTickCounter().getTickDelta(true));
                double rx = pearlEntity.getX() - itemPos.getX();
                double ry = pearlEntity.getY() - itemPos.getY();
                double rz = pearlEntity.getZ() - itemPos.getZ();
                Entity thrower = pearlEntity.getOwner();
                if (thrower instanceof PlayerEntity player)
                {
                    RenderManager.renderSign(player.getName().getString(), rx, ry, rz, getNametagColor(player));
                }
            }
        }
        if (soundsConfig.getValue())
        {
            sounds.entrySet().removeIf(e -> System.currentTimeMillis() - e.getValue() > 1000);
            for (Map.Entry<SoundRender, Long> entry : sounds.entrySet())
            {
                SoundEvent soundEvent = entry.getKey().soundEvent();
                Vec3d renderPos = entry.getKey().pos();
                RenderManager.renderSign(soundEvent.getId().toShortTranslationKey(), renderPos.x, renderPos.y, renderPos.z, -1);
            }
        }

        RenderBuffers.postRender();
    }

    @EventListener
    public void onRenderLabel(RenderLabelEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity && event.getEntity() != mc.player)
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPlaySound(PlaySoundEvent event)
    {
        sounds.put(new SoundRender(event.getPos(), event.getSoundEvent()), System.currentTimeMillis());
    }

    private record SoundRender(Vec3d pos, SoundEvent soundEvent) {}

    private void renderInfo(String info, float width, PlayerEntity entity,
                            double x, double y, double z, Camera camera, float scaling)
    {
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        final Vec3d pos = camera.getPos();
        MatrixStack matrices = new MatrixStack();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices.translate(x - pos.getX(),
                y + (double) entity.getHeight() + (entity.isSneaking() ? 0.4f : 0.43f) - pos.getY(),
                z - pos.getZ());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.scale(-scaling, -scaling, 1.0f);
        if (backgroundConfig.getValue())
        {
            RenderManager.rect(matrices, isOnlineUser(entity) ? -width - 3.0f : -width - 1.0f, -1.0f, width * 2.0f + (isOnlineUser(entity) ? 5.0f : 2.5f),
                    mc.textRenderer.fontHeight + 1.0f, 0.0, 0x55000400);
        }
        if (borderedConfig.getValue())
        {
            RenderManager.borderedRectLine(matrices, isOnlineUser(entity) ? -width - 3.0f : -width - 1.0f, -1.0f, width * 2.0f + (isOnlineUser(entity) ? 5.0f : 2.5f),
                    mc.textRenderer.fontHeight + 1.0f, entity != mc.player && Managers.SOCIAL.isFriend(entity.getDisplayName().getString()) ? SocialsModule.getInstance().getFriendRGB() : ColorsModule.getInstance().getRGB());
        }

        int color = getNametagColor(entity);
        renderItems(matrices, entity);

        OnlineUser onlineUser = IRCManager.getInstance().findOnlineUser(entity.getGameProfile().getName());
        if (onlineUsersConfig.getValue() && onlineUser != null)
        {
            Identifier identifier = getNametagLogo(onlineUser.getUsertype());
            RenderManager.rectTextured(matrices, identifier, (int) -width - 1.5f, (int) -width + 6.0f,
                    0.5f, 8.0f, 0, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        }

        drawText(matrices, info, isOnlineUser(entity) ? -width + 10.0f : -width, 0.0f, color);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    private boolean isOnlineUser(PlayerEntity entity)
    {
        return onlineUsersConfig.getValue() && IRCManager.getInstance().findOnlineUser(entity.getGameProfile().getName()) != null;
    }

    public Identifier getNametagLogo(OnlineUser.UserType onlineUser)
    {
        return switch (onlineUser)
        {
            case RELEASE -> Identifier.of("shoreline", "logo/white.png");
            case BETA -> Identifier.of("shoreline", "logo/blue.png");
            case DEV -> Identifier.of("shoreline", "logo/red.png");
        };
    }

    private void drawText(MatrixStack matrices, String text, float x, float y, int color)
    {
        if (FontModule.getInstance().isEnabled())
        {
            Fonts.CLIENT_UNSCALED.drawStringWithShadow(matrices, text, x, y + 1.0f, color);
        }
        else
        {
            VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
            ((AccessorTextRenderer) mc.textRenderer).hookDrawLayer(text, x, y, TextRenderer.tweakTransparency(color), true,
                    matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            vertexConsumers.draw();

            ((AccessorTextRenderer) mc.textRenderer).hookDrawLayer(text, x, y, TextRenderer.tweakTransparency(color), false,
                    matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            vertexConsumers.draw();
        }
    }

    private void renderItems(MatrixStack matrixStack, PlayerEntity player)
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
        float n10 = 0;
        int n11 = 0;
        boolean gapple = true;
        for (ItemStack stack : displayItems)
        {
            n10 -= gapple ? 9 : 8;
            gapple = false;
            if (stack.getEnchantments().getEnchantments().size() > n11)
            {
                n11 = stack.getEnchantments().getEnchantments().size();
            }
        }
        float m2 = enchantOffset(n11);
        for (ItemStack stack : displayItems)
        {
            boolean armor = stack.getItem() instanceof ArmorItem;
            if (armorConfig.getValue() && armor || itemsConfig.getValue() && !armor)
            {
                float y = !armor && !enchantmentsConfig.getValue() ? -18.5f : m2;
                matrixStack.push();
                matrixStack.translate(n10, y, 0.0f);
                matrixStack.translate(8.0f, 8.0f, 0.0f);
                matrixStack.scale(16.0f, 16.0f, 0.0f);
                matrixStack.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 0.0001f));
                renderItem(stack, ModelTransformationMode.GUI, 0xf, OverlayTexture.DEFAULT_UV,
                        matrixStack, mc.getBufferBuilders().getEntityVertexConsumers(), mc.world, 0);
                mc.getBufferBuilders().getEntityVertexConsumers().draw();
                matrixStack.pop();
                renderItemOverlay(matrixStack, stack, (int) n10, (int) y);

                matrixStack.scale(0.5f, 0.5f, 0.5f);
                if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                {
                    float y2 = (m2 + (armorConfig.getValue() ? 1.0f : -13.5f)) * 2;
                    drawText(matrixStack, "God", (n10 + 2.0f) * 2, y2, 0xffc34e41);
                }
                else if (enchantmentsConfig.getValue())
                {
                    renderEnchants(matrixStack, stack, n10 + 2.0f, m2);
                }
                matrixStack.scale(2.0f, 2.0f, 2.0f);
            }
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            if (durabilityConfig.getValue())
            {
                renderDurability(matrixStack, stack, n10 + 2.0f, m2 - 4.5f);
            }
            matrixStack.scale(2.0f, 2.0f, 2.0f);
            n10 += 16;
            // int n4 = (n11 > 4) ? ((n11 - 4) * 8 / 2) : 0;
            // mc.getItemRenderer().renderInGui(matrixStack, mc.textRenderer, stack, n10, m2);
        }
        //
        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.isEmpty())
        {
            return;
        }
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        if (itemNameConfig.getValue())
        {
            renderItemName(matrixStack, heldItem, 0, durabilityConfig.getValue() ? m2 - 10.0f : m2 - 5.5f);
        }
        matrixStack.scale(2.0f, 2.0f, 2.0f);
    }

    private void renderItem(ItemStack stack, ModelTransformationMode renderMode, int light, int overlay, MatrixStack matrices,
                            VertexConsumerProvider vertexConsumers, World world, int seed)
    {
        BakedModel bakedModel = mc.getItemRenderer().getModel(stack, world, null, seed);
        if (stack.isEmpty())
        {
            return;
        }
        boolean bl = renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED;
        if (bl)
        {
            if (stack.isOf(Items.TRIDENT))
            {
                bakedModel = mc.getItemRenderer().getModels().getModelManager().getModel(ModelIdentifier.ofVanilla("trident", "inventory"));
            }
            else if (stack.isOf(Items.SPYGLASS))
            {
                bakedModel = mc.getItemRenderer().getModels().getModelManager().getModel(ModelIdentifier.ofVanilla("spyglass", "inventory"));
            }
        }
        bakedModel.getTransformation().getTransformation(renderMode).apply(false, matrices);
        matrices.translate(-0.5f, -0.5f, 0.0f);
        if (bakedModel.isBuiltin() || stack.isOf(Items.TRIDENT) && !bl)
        {
            ((AccessorItemRenderer) mc.getItemRenderer()).hookGetBuiltinModelItemRenderer().render(
                    stack, renderMode, matrices, vertexConsumers, light, overlay);
        }
        else
        {
            renderBakedItemModel(bakedModel, stack, light, overlay, matrices,
                    getItemGlintConsumer(vertexConsumers, RenderLayersClient.ENTITY_TRANSLUCENT_CULL.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE), stack.hasGlint()));
        }
    }

    private void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices)
    {
        Random random = Random.create();
        long l = 42L;
        for (Direction direction : Direction.values())
        {
            random.setSeed(42L);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, random), stack, light, overlay);
        }
        random.setSeed(42L);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, random), stack, light, overlay);
    }

    private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay)
    {
        boolean bl = !stack.isEmpty();
        MatrixStack.Entry entry = matrices.peek();
        for (BakedQuad bakedQuad : quads)
        {
            int i = -1;
            if (bl && bakedQuad.hasColor())
            {
                i = ((AccessorItemRenderer) mc.getItemRenderer()).hookGetItemColors().getColor(stack, bakedQuad.getColorIndex());
            }
            float f = (float)(i >> 16 & 0xFF) / 255.0f;
            float g = (float)(i >> 8 & 0xFF) / 255.0f;
            float h = (float)(i & 0xFF) / 255.0f;
            quad(vertices, entry, bakedQuad, f, g, h, light, overlay);
        }
    }

    public void quad(VertexConsumer vertexConsumer, MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, int light, int overlay)
    {
        float[] fs = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
        int[] is = new int[] {light, light, light, light};
        int[] js = quad.getVertexData();
        Matrix4f matrix4f = matrixEntry.getPositionMatrix();
        // Vec3i vec3i = quad.getFace().getVector();
        // Vector3f vector3f = matrixEntry.getNormalMatrix().transform(new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
        int i = 8;
        int j = js.length / 8;
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            ByteBuffer byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            for (int k = 0; k < j; ++k)
            {
                float q;
                float p;
                float o;
                float n;
                float m;
                intBuffer.clear();
                intBuffer.put(js, k * 8, 8);
                float f = byteBuffer.getFloat(0);
                float g = byteBuffer.getFloat(4);
                float h = byteBuffer.getFloat(8);
                o = fs[k] * red;
                p = fs[k] * green;
                q = fs[k] * blue;
                int r = is[k];
                m = byteBuffer.getFloat(16);
                n = byteBuffer.getFloat(20);
                Vector4f vector4f = matrix4f.transform(new Vector4f(f, g, h, 1.0f));
                vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z());
                vertexConsumer.color(new Color(o, p, q).getRGB());
                vertexConsumer.texture(m, n);
                vertexConsumer.overlay(overlay);
                vertexConsumer.light(r);
                vertexConsumer.normal(1.0f, 1.0f, 1.0f);
            }
        }
    }

    public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers,
                                                      RenderLayer layer, boolean glint)
    {
        if (glint)
        {
            return VertexConsumers.union(vertexConsumers.getBuffer(RenderLayersClient.GLINT), vertexConsumers.getBuffer(layer));
        }
        return vertexConsumers.getBuffer(layer);
    }

    private void renderItemOverlay(MatrixStack matrixStack, ItemStack stack, int x, int y)
    {
        matrixStack.push();
        if (stack.getCount() != 1)
        {
            String string = String.valueOf(stack.getCount());
            // this.matrices.translate(0.0f, 0.0f, 200.0f);
            VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
            ((AccessorTextRenderer) mc.textRenderer).hookDrawLayer(string, x + 17 - mc.textRenderer.getWidth(string), y + 9.0f, TextRenderer.tweakTransparency(-1), true,
                    matrixStack.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            vertexConsumers.draw();

            ((AccessorTextRenderer) mc.textRenderer).hookDrawLayer(string, x + 17 - mc.textRenderer.getWidth(string), y + 9.0f, TextRenderer.tweakTransparency(-1), false,
                    matrixStack.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            vertexConsumers.draw();
        }
        if (stack.isItemBarVisible())
        {
            int i = (int) Math.clamp(stack.getItemBarStep() * 0.923076923, 0, 12);
            int j = stack.getItemBarColor();
            int k = x + 3;
            int l = y + 13;
            RenderManager.rect(matrixStack, k, l, 12, 1, Colors.BLACK);
            RenderManager.rect(matrixStack, k, l, i, 1, j | Colors.BLACK);
        }
        matrixStack.pop();
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
        drawText(matrixStack, durability + "%", x * 2, y * 2,
                ColorUtil.hslToColor((float) (n - n2) / (float) n * 120.0f, 100.0f, 50.0f, 1.0f).getRGB());
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
            drawText(matrixStack, enchantString.toString(), x * 2, (y + n2) * 2, -1);
            n2 += 4.5f;
        }
    }

    private float enchantOffset(final int n)
    {
        if (!enchantmentsConfig.getValue() || n <= 3)
        {
            return armorConfig.getValue() ? -18.0f : -3.5f;
        }
        float n2 = -14.0f;
        n2 -= (n - 3) * 4.5f;
        return n2;
    }

    private void renderItemName(MatrixStack matrixStack, ItemStack itemStack, float x, float y)
    {
        String itemName = itemStack.getName().getString();
        float width = mc.textRenderer.getWidth(itemName) / 4.0f;
        drawText(matrixStack, itemName, (x - width) * 2, y * 2, -1);
    }

    private String getNametagInfo(PlayerEntity player)
    {
        final StringBuilder info = new StringBuilder(player.getName().getString());
        info.append(" ");
        if (entityIdConfig.getValue())
        {
            info.append("ID: ");
            info.append(player.getId());
            info.append(" ");
        }
        if (gamemodeConfig.getValue())
        {
            if (player.isCreative())
            {
                info.append("[C] ");
            }
            else if (player.isSpectator())
            {
                info.append("[I] ");
            }
            else
            {
                info.append("[S] ");
            }
        }
        if (pingConfig.getValue() && mc.getNetworkHandler() != null)
        {
            PlayerListEntry playerEntry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId());
            if (playerEntry != null)
            {
                info.append(playerEntry.getLatency());
                info.append("ms ");
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
            info.append(hcolor);
            info.append(bigDecimal.doubleValue());
            info.append(" ");
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
                info.append(pcolor);
                info.append(-totems);
                info.append(" ");
            }
        }
        return info.toString().trim();
    }

    private int getNametagColor(PlayerEntity player)
    {
        if (player == mc.player)
        {
            return ColorsModule.getInstance().getRGB(255);
        }
        if (Managers.SOCIAL.isFriend(player.getName()))
        {
            return SocialsModule.getInstance().getFriendRGB();
        }
        if (player.isInvisible())
        {
            return 0xffff2500;
        }
        // fakeplayer
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

    public float getScaling()
    {
        return scalingConfig.getValue();
    }
}
