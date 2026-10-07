package net.shoreline.client.impl.mining;

import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.impl.network.NetworkHandler;

public enum MiningPackets
{
    NORMAL {
        @Override
        public void sendStartPackets(NetworkHandler handler, BlockPos blockPos, Direction direction)
        {
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        @Override
        public void sendStopPackets(NetworkHandler handler, BlockPos blockPos, Direction direction)
        {
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
        }
    },

    GRIM {
        @Override
        public void sendStartPackets(NetworkHandler handler, BlockPos blockPos, Direction direction)
        {
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        @Override
        public void sendStopPackets(NetworkHandler handler, BlockPos blockPos, Direction direction)
        {
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
        }
    },

    GRIM_V3 {
        @Override
        public void sendStartPackets(NetworkHandler handler, BlockPos blockPos, Direction direction)
        {
            handler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));

            handler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            handler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            handler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        @Override
        public void sendStopPackets(NetworkHandler handler, BlockPos blockPos, Direction direction)
        {
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            handler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
        }
    };

    public abstract void sendStartPackets(NetworkHandler handler, BlockPos blockPos, Direction direction);

    public abstract void sendStopPackets(NetworkHandler handler, BlockPos blockPos, Direction direction);
}