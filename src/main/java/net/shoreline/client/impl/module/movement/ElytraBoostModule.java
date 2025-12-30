package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.combat.AutoArmorModule;
import net.shoreline.client.impl.module.impl.InventorySwapModule;
import net.shoreline.eventbus.annotation.EventListener;

public class ElytraBoostModule extends InventorySwapModule
{
    private boolean disabledAutoArmor;
    private boolean boosted;
    private boolean initiated;

    public ElytraBoostModule()
    {
        super("ElytraBoost", "Abuses minecraft mechanics to boost yourself in the air", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        AutoArmorModule autoArmor = AutoArmorModule.INSTANCE;
        ScreenHandler handler = mc.player.playerScreenHandler;
        boolean onGround = mc.player.isOnGround();
        boolean equipped = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        if (!equipped && !initiated)
        {
            int elytraSlot = find(Items.ELYTRA);
            if (elytraSlot != -1)
            {
                if (autoArmor.isEnabled())
                {
                    autoArmor.disable();
                    disabledAutoArmor = true;
                }

                Managers.INVENTORY.pickupSlot(handler, 6);
                Managers.INVENTORY.pickupSlot(handler, InventoryUtil.getPacketSlotIndex(handler, elytraSlot));
                Managers.INVENTORY.pickupSlot(handler, 6);
            }
            else
            {
                sendErrorChatMessage("Couldn't find an elytra.");
                this.disable();
            }

            return;
        }

        if (onGround && !initiated && equipped)
        {
            mc.player.jump();
            initiated = true;
            return;
        }

        if (!onGround && equipped && !boosted)
        {
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(0, velocity.y + 2.8800000000000003, 0);
            sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), -90.0f, false, mc.player.horizontalCollision));
            boosted = true;
            return;
        }

        int slot = autoArmor.findArmor(EquipmentSlot.CHEST);
        if (slot != -1)
        {
            Managers.INVENTORY.pickupSlot(handler, 6);
            Managers.INVENTORY.pickupSlot(handler, InventoryUtil.getPacketSlotIndex(handler, slot));
            Managers.INVENTORY.pickupSlot(handler, 6);
        }

        if (disabledAutoArmor)
        {
            autoArmor.enable();
        }

        boosted = false;
        initiated = false;
        this.disable();
    }

    public int find(Item item)
    {
        for (int i = 0; i <= 45; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() != item)
            {
                continue;
            }

            return i;
        }

        return -1;
    }
}
