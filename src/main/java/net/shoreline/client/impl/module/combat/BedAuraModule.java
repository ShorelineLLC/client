package net.shoreline.client.impl.module.combat;

import net.minecraft.block.BedBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author bon
 *
 * not even close to finished
 */
public class BedAuraModule extends ToggleModule
{
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "Range to place beds", 1.0f, 4.5f, 6.0f));
    Config<Float> wallRangeConfig = register(new NumberConfig<>("WallRange", "Range to attack entities through walls", 1.0f, 4.5f, 6.0f));
    Config<Priority> priorityConfig = register(new EnumConfig<>("Priority", "The value to prioritize when searching for targets", Priority.HEALTH, Priority.values()));

    Config<Boolean> armorCheckConfig = register(new BooleanConfig("ArmorCheck", "Checks if target has armor before attacking", false));

    private PlayerEntity lastTarget;
    private PlayerEntity currentTarget;

    public BedAuraModule()
    {
        super("BedAura", "Automatically places and explodes beds (designed for 1.13+)", ModuleCategory.COMBAT);
    }

    @Override
    public void onEnable()
    {
        if (!shouldRun())
        {
            disable();
            return;
        }
    }

    @EventListener
    public void onPlayerUpdate(PlayerTickEvent event)
    {
        this.currentTarget = null;

        if (!shouldRun())
        {
            disable();
            return;
        }

        // First, lets collect all possible entities that could be a target
        List<PlayerEntity> possibleTargets = getPossibleTargets();

        if (possibleTargets.isEmpty())
        {
            this.lastTarget = null;
            this.currentTarget = null;
            return;
        }

        // If the last target is in this list, lets commit to him
        if (this.lastTarget != null && possibleTargets.contains(this.lastTarget))
        {
            this.currentTarget = this.lastTarget;
        } else
        {
            this.currentTarget = possibleTargets.get(0);
        }
    }

    private List<PlayerEntity> getPossibleTargets()
    {
        List<PlayerEntity> possibleTargets = new ArrayList<>();

        for (Entity entity : mc.world.getEntities())
        {
            if (!(entity instanceof PlayerEntity))
            {
                continue;
            }

            PlayerEntity playerEntity = (PlayerEntity) entity;

            if (playerEntity == mc.player
                    || !playerEntity.isAlive()
                    || Managers.SOCIAL.isFriend(playerEntity.getName())
                    || playerEntity.isCreative())
            {
                continue;
            }

            if (this.armorCheckConfig.getValue() && !playerEntity.getArmorItems().iterator().hasNext())
            {
                continue;
            }

            // Range configs don't apply to how far the TARGET is, but how far
            // the BED place target is.

            // This is because a target might be far away from you, but a bed place
            // position may be close enough for you to still place and hurt them.
            if (mc.player.getPos().distanceTo(entity.getPos()) > 8.0D)
            {
                continue;
            }

            // Okay, they seem like a valid target, lets see if bed placements are possible on them
            List<BlockPos> lowerSurroundBlocks = SurroundModule.getInstance().getSurroundNoDown(playerEntity);
            List<BlockPos> middleSurroundBlocks = new ArrayList<>();
            lowerSurroundBlocks.forEach(blockpos -> middleSurroundBlocks.add(blockpos.up()));
            List<BlockPos> upperSurroundBlocks = new ArrayList<>();
            lowerSurroundBlocks.forEach(blockpos -> upperSurroundBlocks.add(blockpos.down()));



            possibleTargets.add(playerEntity);
        }

        possibleTargets.sort(
                this.priorityConfig.getValue().comparator
        );

        return possibleTargets;
    }

    private void test(ClientPlayerEntity target)
    {
    }

    private boolean shouldRun()
    {
        if (mc.player == null || mc.world == null)
        {
            return false;
        }

        if (!BedBlock.isBedWorking(mc.world))
        {
            return false;
        }

        return true;
    }

    private static float getArmorDurability(LivingEntity e)
    {
        float edmg = 0.0f;
        float emax = 0.0f;
        for (ItemStack armor : e.getArmorItems())
        {
            if (armor != null && !armor.isEmpty())
            {
                edmg += armor.getDamage();
                emax += armor.getMaxDamage();
            }
        }
        return 100.0f - edmg / emax;
    }

    public enum Priority
    {
        HEALTH(Comparator.comparingDouble(
                entity -> entity.getHealth() + entity.getAbsorptionAmount()
        )),
        DISTANCE(Comparator.comparingDouble(
                entity -> mc.player.getPos().distanceTo(entity.getPos())
        )),
        ARMOR(Comparator.comparingDouble(
                BedAuraModule::getArmorDurability
        ));

        private final Comparator<PlayerEntity> comparator;

        Priority(Comparator<PlayerEntity> comparator)
        {
            this.comparator = comparator;
        }
    }
}