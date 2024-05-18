package net.shoreline.client.impl.event.buffers;

import net.minecraft.client.util.math.MatrixStack;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.manager.client.CapeManager;
import net.shoreline.client.impl.manager.client.CommandManager;
import net.shoreline.client.impl.manager.network.NetworkManager;
import net.shoreline.client.impl.manager.player.InventoryManager;
import net.shoreline.client.impl.manager.player.MovementManager;
import net.shoreline.client.impl.manager.player.rotation.RotationManager;
import net.shoreline.client.init.Managers;

public class ManagersBuffer {

    public static CapeManager getCapesManager() {
        return Managers.CAPES;
    }

    public static MovementManager getMovementManger() {
        return Managers.MOVEMENT;
    }

    public static RotationManager getRotationManager() {
        return Managers.ROTATION;
    }

    public static InventoryManager getInventoryManager() {
        return Managers.INVENTORY;
    }

    public static NetworkManager getNetworkManager() {
        return Managers.NETWORK;
    }

    public static CommandManager getCommandManager() {
        return Managers.COMMAND;
    }

    public static void getRenderManagerRect(MatrixStack matrixStack, double x1, double y1, double x2, double y2, int color) {
        RenderManager.rect(matrixStack, x1, y1, x2, y2, color);
    }
}
