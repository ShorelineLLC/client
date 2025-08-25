package net.shoreline.client.impl.mining;

import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.Managers;

public enum MiningPackets
{
    NORMAL {
        @Override
        public void sendStartPackets(BlockPos blockPos, Direction direction)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        @Override
        public void sendStopPackets(BlockPos blockPos, Direction direction)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
        }
    },

    GRIM {
        @Override
        public void sendStartPackets(BlockPos blockPos, Direction direction)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        @Override
        public void sendStopPackets(BlockPos blockPos, Direction direction)
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
        }
    };

    public abstract void sendStartPackets(BlockPos blockPos, Direction direction);

    public abstract void sendStopPackets(BlockPos blockPos, Direction direction);
}
