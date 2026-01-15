package net.shoreline.client.impl.mining;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.util.item.EnchantUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MiningManager extends GenericFeature
{
    private final ConcurrentMap<BlockPos, MiningData> miningBlocks = new ConcurrentHashMap<>();

    private final ItemStack maxPickaxeStack;

    public MiningManager()
    {
        super("Mining");
        this.maxPickaxeStack = new ItemStack(Items.NETHERITE_PICKAXE);
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        miningBlocks.clear();
    }

    @EventListener
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity() == mc.player)
        {
            miningBlocks.clear();
        }
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        maxPickaxeStack.addEnchantment(EnchantUtil.getEntry(Enchantments.EFFICIENCY), 5);

        for (MiningData data : miningBlocks.values())
        {
            if (data.getSquaredDistanceTo() > 36.0f || data.isBlockMined() || data.hasMinedFor(40))
            {
                miningBlocks.remove(data.getBlockPos());
                continue;
            }

            data.tickDelta();
        }
    }

    @EventListener
    public void onPacketInbound(PacketEvent.Inbound event)
    {
        if (checkNull())
        {
            return;
        }

        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket packet)
        {
            if (miningBlocks.containsKey(packet.getPos()))
            {
                return;
            }

            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (!(entity instanceof PlayerEntity playerEntity))
            {
                return;
            }

            MiningData data = MiningData.builder()
                    .blockPos(packet.getPos())
                    .direction(Direction.UP)
                    .maxProgress(1.0f)
                    .player(playerEntity)
                    .miningStack(maxPickaxeStack)
                    .build();

            if (mc.player.squaredDistanceTo(data.getBlockPos().toCenterPos()) > 144.0f)
            {
                return;
            }

            long count = getMiningCount(playerEntity);
            if (count >= 2)
            {
                for (var entry : miningBlocks.entrySet())
                {
                    if (entry.getValue().getPlayer().equals(playerEntity))
                    {
                        miningBlocks.remove(entry.getKey());
                        break;
                    }
                }
            }

            miningBlocks.put(packet.getPos(), data);
        }
    }

    public MiningData getData(BlockPos pos)
    {
        return miningBlocks.get(pos);
    }

    public int getMiningCount(PlayerEntity playerEntity)
    {
        int count = 0;
        for (MiningData data : miningBlocks.values())
        {
            if (data.getPlayer().equals(playerEntity) && ++count >= 2)
            {
                break;
            }
        }
        return count;
    }

    public float getMiningDamage(BlockPos blockPos)
    {
        MiningData data = miningBlocks.get(blockPos);
        return data != null ? data.getBlockDamage() : 0.0f;
    }

    public float getMiningProgress(BlockPos blockPos)
    {
        MiningData data = miningBlocks.get(blockPos);
        return data != null ? data.getBlockDamage() / data.getMaxProgress() : 0.0f;
    }

    public Collection<MiningData> getMiningBlocks()
    {
        return miningBlocks.values();
    }
}
