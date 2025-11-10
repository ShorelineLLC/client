package net.shoreline.client.impl.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class Rotation
{
    private float yaw, pitch;

    public Rotation(float yaw, float pitch)
    {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation(Entity entity)
    {
        this(entity.getYaw(), entity.getPitch());
    }

    public Rotation add(Rotation other)
    {
        return new Rotation(this.yaw + other.yaw, this.pitch + other.pitch);
    }

    public Rotation subtract(Rotation other)
    {
        return new Rotation(this.yaw - other.yaw, this.pitch - other.pitch);
    }

    public Rotation multiply(float scale)
    {
        return new Rotation(this.yaw * scale, this.pitch * scale);
    }

    public void apply(Entity entity)
    {
        entity.setYaw(yaw);
        entity.setPitch(pitch);
    }

    public void applyToPlayer()
    {
        apply(MinecraftClient.getInstance().player);
    }

    public float[] getComponents()
    {
        return new float[] { yaw, pitch };
    }

    public static Rotation calculateNewRotation(Rotation prev,
                                                double dx,
                                                double dy)
    {
        double gcd = getGcd();
        Rotation delta = new Rotation((float) (dx * gcd * 0.15f), (float) (dy * gcd * 0.15f));
        Rotation newRot = prev.add(delta);

        newRot = newRot.withPitch(MathHelper.clamp(newRot.pitch, -90.0f, 90.0f));
        return newRot;
    }

    public static List<Rotation> approximateCursorDeltas(Rotation deltaRotation)
    {
        double gcd = getGcd() * 0.15f;
        double tx = -deltaRotation.getYaw() / gcd;
        double ty = -deltaRotation.getPitch() / gcd;

        List<Rotation> possibilities = new ArrayList<>();
        possibilities.add(calculateNewRotation(new Rotation(0, 0), Math.floor(tx), Math.floor(ty)));
        possibilities.add(calculateNewRotation(new Rotation(0, 0), Math.ceil(tx), Math.floor(ty)));
        possibilities.add(calculateNewRotation(new Rotation(0, 0), Math.ceil(tx), Math.ceil(ty)));
        possibilities.add(calculateNewRotation(new Rotation(0, 0), Math.floor(tx), Math.ceil(ty)));
        return possibilities;
    }

    public Rotation correctSensitivity(Rotation prev)
    {
        Rotation delta = closestDelta(prev);
        List<Rotation> options = approximateCursorDeltas(delta);

        return options.stream()
                .min(Comparator.comparingDouble(this::fov))
                .orElse(this);
    }

    public Rotation smoothedTurn(Rotation target, double smoothness)
    {
        Rotation delta = target.closestDelta(this).multiply((float) smoothness);
        return this.add(delta);
    }

    public float fov(Rotation other)
    {
        Rotation delta = this.closestDelta(other);
        return (float) Math.sqrt(delta.yaw * delta.yaw + delta.pitch * delta.pitch);
    }

    public Rotation closestDelta(Rotation other)
    {
        float dyaw = MathHelper.wrapDegrees(other.yaw - this.yaw);
        float dpitch = other.pitch - this.pitch;
        return new Rotation(dyaw, dpitch);
    }

    public Vec3d toForwardVector()
    {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        return new Vec3d(
                Math.sin(-yawRad) * Math.cos(pitchRad),
                -Math.sin(pitchRad),
                Math.cos(-yawRad) * Math.cos(pitchRad)
        );
    }

    public Rotation withYaw(float newYaw)
    {
        return new Rotation(newYaw, this.pitch);
    }

    public Rotation withPitch(float newPitch)
    {
        return new Rotation(this.yaw, newPitch);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Rotation rotation = (Rotation) o;
        return Float.compare(rotation.getYaw(), yaw) == 0 && Float.compare(rotation.getPitch(), pitch) == 0;
    }

    private static double getGcd()
    {
        double sensitivity = MinecraftClient.getInstance().options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        double scaled = sensitivity * sensitivity * sensitivity;
        return MinecraftClient.getInstance().options.getPerspective().isFirstPerson()
                && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isUsingSpyglass() ? scaled : scaled * 8.0;
    }
}
