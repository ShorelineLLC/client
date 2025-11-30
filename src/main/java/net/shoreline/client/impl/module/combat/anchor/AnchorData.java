package net.shoreline.client.impl.module.combat.anchor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

@Getter
@Setter
@RequiredArgsConstructor
public class AnchorData implements Comparable<AnchorData>
{
    private final BlockPos pos;
    private final Long time;

    private PlayerEntity target;
    private boolean anchor;
    private boolean placed;
    private float selfDamage;
    private float damage;

    public boolean isPlaced()
    {
        if (!anchor)
        {
            return false;
        }

        return placed;
    }

    public AnchorData copy()
    {
        AnchorData data = new AnchorData(pos, System.currentTimeMillis());
        data.setTarget(target);
        data.setAnchor(anchor);
        data.setPlaced(placed);
        data.setSelfDamage(selfDamage);
        data.setDamage(damage);
        return data;
    }

    @Override
    public int compareTo(AnchorData o)
    {
        if (anchor != o.anchor)
        {
            return Boolean.compare(o.anchor, anchor);
        }

        if (Math.abs(o.damage - damage) < 1.0)
        {
            return Float.compare(selfDamage, o.selfDamage);
        }

        return Float.compare(o.damage, damage);
    }
}