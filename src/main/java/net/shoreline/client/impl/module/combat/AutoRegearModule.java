package net.shoreline.client.impl.module.combat;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class AutoRegearModule extends ToggleModule
{
    private static AutoRegearModule INSTANCE;

    Config<Float> delayConfig = register(new NumberConfig<>("Delay", "The item swap delay", 0.0f, 0.15f, 2.0f));

    private final Timer clickTimer = new CacheTimer();
    private final Map<Integer, Item> regearInventory = new ConcurrentHashMap<>();

    public AutoRegearModule()
    {
        super("AutoRegear", "Automatically refills your inventory with gear", ModuleCategory.COMBAT);
        INSTANCE = this;
    }

    public static AutoRegearModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onEnable()
    {
        if (regearInventory.isEmpty())
        {
            sendModuleError("No regear configuration set! Use the .regear command to set your regear inventory.");
        }
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler containerScreen)
        {
            for (Map.Entry<Integer, Item> entry : regearInventory.entrySet())
            {
                if (entry.getValue() == Items.AIR)
                {
                    continue;
                }

                int slotId = containerScreen.getInventory().size() + entry.getKey() - 9;
                ItemStack playerStack = mc.player.getInventory().getStack(slotId);
                if (playerStack.getCount() >= playerStack.getMaxCount())
                {
                    continue;
                }
                final Queue<Integer> validSlots = new ArrayDeque<>();

                // First organize player inventory

                // Then we can take from containers
                for (int i = 0; i < containerScreen.getInventory().size(); i++)
                {
                    Slot slot = containerScreen.getSlot(i);
                    if (slot.hasStack() && slot.getStack().getItem() == entry.getValue())
                    {
                        validSlots.add(i);
                    }
                }
                while (!validSlots.isEmpty() && playerStack.getCount() < playerStack.getMaxCount())
                {
                    if (clickTimer.passed(delayConfig.getValue() * 1000))
                    {
                        int swapSlot = validSlots.remove();
                        Managers.INVENTORY.pickupSlot(swapSlot);
                        Managers.INVENTORY.pickupSlot(slotId);
                        clickTimer.reset();
                    }

                    playerStack = mc.player.getInventory().getStack(slotId);
                }
            }
        }

        if (mc.player.currentScreenHandler instanceof ShulkerBoxScreenHandler shulkerBoxScreenHandler)
        {
            for (Map.Entry<Integer, Item> entry : regearInventory.entrySet())
            {
                if (entry.getValue() == Items.AIR)
                {
                    continue;
                }

                int slotId = shulkerBoxScreenHandler.getStacks().size() + entry.getKey() - 9;
                ItemStack playerStack = mc.player.getInventory().getStack(slotId);
                if (playerStack.getCount() >= playerStack.getMaxCount())
                {
                    continue;
                }
                final Queue<Integer> validSlots = new ArrayDeque<>();

                // First organize player inventory

                // Then we can take from containers
                for (int i = 0; i < shulkerBoxScreenHandler.slots.size(); i++)
                {
                    Slot slot = shulkerBoxScreenHandler.slots.get(i);
                    if (slot.hasStack() && slot.getStack().getItem() == entry.getValue())
                    {
                        validSlots.add(i);
                    }
                }
                while (!validSlots.isEmpty() && playerStack.getCount() < playerStack.getMaxCount())
                {
                    if (clickTimer.passed(delayConfig.getValue() * 1000))
                    {
                        int swapSlot = validSlots.remove();
                        Managers.INVENTORY.pickupSlot(swapSlot);
                        Managers.INVENTORY.pickupSlot(slotId);
                        clickTimer.reset();
                    }

                    playerStack = mc.player.getInventory().getStack(slotId);
                }
            }
        }
    }

    public void clearPlayerInventory()
    {
        regearInventory.clear();
    }

    public void savePlayerInventory()
    {
        regearInventory.clear();
        for (int i = 0; i < 35; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            regearInventory.put(i < 9 ? i + 36 : i, stack.getItem());
        }
    }

    public void saveRegearFile()
    {
        if (regearInventory.isEmpty())
        {
            return;
        }
        try
        {
            Path regearFile = Shoreline.CONFIG.getClientDirectory().resolve("regear.json");
            if (!Files.exists(regearFile))
            {
                Files.createFile(regearFile);
            }
            FileWriter writer = new FileWriter(regearFile.toFile());
            JsonArray jsonArray = new JsonArray();
            for (Map.Entry<Integer, Item> entry : regearInventory.entrySet())
            {
                JsonObject jsonObject1 = new JsonObject();
                jsonObject1.addProperty("slotId", entry.getKey());
                jsonObject1.addProperty("item", Registries.ITEM.getId(entry.getValue()).toString());
                jsonArray.add(jsonObject1);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonArray, writer);
        }
        catch (IOException exception)
        {

        }
    }

    public void loadRegearFile()
    {
        Path regearFile = Shoreline.CONFIG.getClientDirectory().resolve("regear.json");
        if (!Files.exists(regearFile))
        {
            return;
        }
        try
        {
            regearInventory.clear();
            FileReader reader = new FileReader(regearFile.toFile());
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
            for (JsonElement jsonElement : jsonArray)
            {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                int slotId = jsonObject.get("slotId").getAsInt();
                Item item = Registries.ITEM.get(Identifier.of(jsonObject.get("item").getAsString()));
                regearInventory.put(slotId, item);
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
