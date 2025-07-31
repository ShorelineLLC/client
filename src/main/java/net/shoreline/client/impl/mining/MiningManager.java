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
        EventBus.INSTANCE.subscribe(this);

        this.maxPickaxeStack = new ItemStack(Items.NETHERITE_PICKAXE);
        this.maxPickaxeStack.addEnchantment(EnchantUtil.getEntry(Enchantments.EFFICIENCY), 5);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

        for (MiningData data : miningBlocks.values())
        {
            data.tickBlockDamage();
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
            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (!(entity instanceof PlayerEntity playerEntity))
            {
                return;
            }

            if (miningBlocks.containsKey(packet.getPos()))
            {
                return;
            }

            MiningData data = MiningData.builder()
                    .blockPos(packet.getPos())
                    .direction(Direction.UP)
                    .player(playerEntity)
                    .miningStack(maxPickaxeStack)
                    .build();

            if (mc.player.squaredDistanceTo(data.getBlockPos().toCenterPos()) > 144.0f || data.squaredDistanceTo() > 144.0f)
            {
                return;
            }

            long count = miningBlocks.values().stream().filter(d -> d.getPlayer().equals(playerEntity)).count();
            if (count >= 2)
            {
                miningBlocks.entrySet().stream()
                        .filter(e -> e.getValue().getPlayer().equals(playerEntity))
                        .findFirst()
                        .ifPresent(d -> miningBlocks.remove(d.getKey()));
            }

            miningBlocks.put(packet.getPos(), data);
        }
    }

    public float getMiningProgress(BlockPos blockPos)
    {
        MiningData data = miningBlocks.get(blockPos);
        return data != null ? data.getBlockDamage() : 0.0f;
    }

    public Collection<MiningData> getMiningBlocks()
    {
        return miningBlocks.values();
    }
}
