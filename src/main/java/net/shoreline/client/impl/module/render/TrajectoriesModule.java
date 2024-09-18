package net.shoreline.client.impl.module.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.Interpolation;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.eventbus.annotation.EventListener;
import org.joml.Matrix4f;

/**
 * @author linus
 * @since 1.0
 */
public class TrajectoriesModule extends ToggleModule
{
    public TrajectoriesModule()
    {
        super("Trajectories", "Renders the trajectory path of projectiles", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        ItemStack mainhand = mc.player.getMainHandStack();
        Item throwItem = mainhand.getItem();
        MatrixStack matrixStack = new MatrixStack();
        double d = mc.options.getFov().getValue();
        matrixStack.multiplyPositionMatrix(mc.gameRenderer.getBasicProjectionMatrix(d));
        Matrix4f prevProjectionMatrix = RenderSystem.getProjectionMatrix();
        RenderSystem.setProjectionMatrix(matrixStack.peek().getPositionMatrix(), VertexSorter.BY_DISTANCE);
        RenderBuffers.preRender();
        if (((throwItem instanceof BowItem || throwItem instanceof CrossbowItem || throwItem instanceof TridentItem) && mc.player.getItemUseTime() > 0) || isThrowableItem(throwItem))
        {
            float yaw = mc.player.getYaw();
            double x = Interpolation.interpolateDouble(mc.player.prevX, mc.player.getX(), mc.getRenderTickCounter().getTickDelta(true));
            double y = Interpolation.interpolateDouble(mc.player.prevY, mc.player.getY(), mc.getRenderTickCounter().getTickDelta(true));
            double z = Interpolation.interpolateDouble(mc.player.prevZ, mc.player.getZ(), mc.getRenderTickCounter().getTickDelta(true));
            y = y + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;

            if (throwItem == mc.player.getMainHandStack().getItem())
            {
                x = x - MathHelper.cos(yaw / 180.0f * MathHelper.PI) * 0.16f;
                z = z - MathHelper.sin(yaw / 180.0f * MathHelper.PI) * 0.16f;
            }
            else
            {
                x = x + MathHelper.cos(yaw / 180.0f * MathHelper.PI) * 0.16f;
                z = z + MathHelper.sin(yaw / 180.0f * MathHelper.PI) * 0.16f;
            }
            final float n = throwItem instanceof BowItem ? 1.0f : 0.4f;
            double motionX = -MathHelper.sin(yaw / 180.0f * MathHelper.PI) * MathHelper.cos(mc.player.getPitch() / 180.0f * MathHelper.PI) * n;
            double motionY = -MathHelper.sin((mc.player.getPitch() + (throwItem instanceof SplashPotionItem || throwItem instanceof LingeringPotionItem || throwItem instanceof ExperienceBottleItem ? -20 : 0)) / 180.0f * MathHelper.PI) * n;
            double motionZ = MathHelper.cos(yaw / 180.0f * MathHelper.PI) * MathHelper.cos(mc.player.getPitch() / 180.0f * MathHelper.PI) * n;
            float power = mc.player.getItemUseTime() / 20.0f;
            power = (power * power + power * 2.0f) / 3.0f;
            if (power > 1.0f)
            {
                power = 1.0f;
            }
            final float dist = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
            motionX /= dist;
            motionY /= dist;
            motionZ /= dist;
            final float pow = (throwItem instanceof BowItem ? (power * 2.0f) : throwItem instanceof CrossbowItem ? 2.2f : 1.0f) * getProjectilePower(throwItem);
            motionX *= pow;
            motionY *= pow;
            motionZ *= pow;
            if (!mc.player.isOnGround())
            {
                motionY += mc.player.getVelocity().getY();
            }

            float size = throwItem instanceof BowItem || throwItem instanceof CrossbowItem ? 0.3f : 0.25f;
            Vec3d lastPos;
            boolean hasLanded = false;
            while (!hasLanded)
            {
                boolean landing = false;
                lastPos = new Vec3d(x, y, z);
                x += motionX;
                y += motionY;
                z += motionZ;
                BlockPos blockPos = BlockPos.ofFloored(x, y, z);
                if (mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER)
                {
                    motionX *= 0.8;
                    motionY *= 0.8;
                    motionZ *= 0.8;
                }
                else
                {
                    motionX *= 0.99;
                    motionY *= 0.99;
                    motionZ *= 0.99;
                }

                if (throwItem instanceof BowItem)
                {
                    motionY -= 0.05000000074505806;
                }
                else if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem)
                {
                    motionY -= 0.05000000074505806;
                }
                else
                {
                    motionY -= 0.03f;
                }

                Box projectileBox = new Box(x - size, y - size, z - size, x + size, y + size, z + size);
                for (Entity entity : mc.world.getOtherEntities(null, projectileBox))
                {
                    if (entity == mc.player || isThrowableEntity(entity))
                    {
                        continue;
                    }
                    RenderManager.renderBox(event.getMatrices(),
                            Interpolation.getInterpolatedEntityBox(entity), ColorsModule.getInstance().getRGB(40));
                    RenderManager.renderBoundingBox(event.getMatrices(),
                            Interpolation.getInterpolatedEntityBox(entity), 1.5f, ColorsModule.getInstance().getRGB(145));
                    landing = true;
                }

                Vec3d pos = new Vec3d(x, y, z);
                BlockHitResult result = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
                if (result != null && result.getType() == HitResult.Type.BLOCK)
                {
                    landing = true;
                    Direction side = result.getSide();
                    if (side == Direction.NORTH || side == Direction.SOUTH)
                    {
                        RenderManager.renderSide(event.getMatrices(), x - 0.6f, y - 0.6f, z, x + 0.6f, y + 0.6f, z, side, ColorsModule.getInstance().getRGB(80));
                    }
                    else if (side == Direction.WEST || side == Direction.EAST)
                    {
                        RenderManager.renderSide(event.getMatrices(), x, y - 0.6f, z - 0.6f, x, y + 0.6f, z + 0.6f, side, ColorsModule.getInstance().getRGB(80));
                    }
                    else
                    {
                        RenderManager.renderSide(event.getMatrices(), x - 0.6f, y, z - 0.6f, x + 0.6f, y, z + 0.6f, side, ColorsModule.getInstance().getRGB(80));
                    }
                }

                if (y < mc.world.getBottomY())
                {
                    break;
                }

                if (motionX == 0.0 && motionY == 0.0 && motionZ == 0.0)
                {
                    continue;
                }

                RenderManager.renderLine(event.getMatrices(), lastPos, pos, 1.5f, ColorsModule.getInstance().getRGB());
                if (landing)
                {
                    hasLanded = true;
                }
            }
        }
        RenderBuffers.postRender();
        RenderSystem.setProjectionMatrix(prevProjectionMatrix, VertexSorter.BY_DISTANCE);
    }

    private float getProjectilePower(Item item)
    {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem)
        {
            return 0.4f;
        }

        if (item instanceof ExperienceBottleItem)
        {
            return 0.5f;
        }

        if (item instanceof TridentItem)
        {
            return 2.0f;
        }

        return 1.5f;
    }

    private boolean isThrowableItem(Item item)
    {
        return item instanceof EnderPearlItem || item instanceof ExperienceBottleItem
                || item instanceof SnowballItem || item instanceof EggItem
                || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    private boolean isThrowableEntity(Entity entity)
    {
        return entity instanceof ExperienceBottleEntity || entity instanceof EnderPearlEntity || entity instanceof ExperienceOrbEntity || entity instanceof PotionEntity || entity instanceof ArrowEntity
                || entity instanceof TridentEntity || entity instanceof SnowballEntity || entity instanceof EggEntity;
    }
}
