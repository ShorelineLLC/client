package net.shoreline.client.util.world;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

@UtilityClass
public class WorldUtil
{
    public List<Entity> collectEntitiesInBox(Box boundingBox)
    {
        return collectEntitiesInBox(Entity.class, boundingBox, null);
    }

    public <T extends Entity> List<T> collectEntitiesInBox(Class<T> entityClass,
                                                           Box box,
                                                           @Nullable Predicate<? super Entity> predicate)
    {
        List<T> entities = Lists.newArrayList();
        for (Entity entity : MinecraftClient.getInstance().world.getEntities())
        {
            if (entity == null || entity.isRemoved())
            {
                continue;
            }

            if (!entityClass.isAssignableFrom(entity.getClass()) || (predicate != null && !predicate.test(entity)))
            {
                continue;
            }

            if (VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(box), VoxelShapes.cuboid(entity.getBoundingBox()), BooleanBiFunction.AND))
            {
                entities.add((T) entity);
            }
        }

        return entities;
    }
}
