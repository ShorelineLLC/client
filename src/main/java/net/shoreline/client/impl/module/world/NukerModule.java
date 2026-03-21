package net.shoreline.client.impl.module.world;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.EnumConfig;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.config.RegistryConfig;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.block.AsyncBlockScanner;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.module.combat.AutoMineModule;
import net.shoreline.client.impl.module.world.nuker.NukeCalcManager;
import net.shoreline.client.impl.module.world.nuker.NukeScanner;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Getter
public class NukerModule extends Toggleable
{
    public static NukerModule INSTANCE;
    Config<NukeMode> mode = new EnumConfig.Builder<NukeMode>("Mode")
            .setValues(NukeMode.values())
            .setDefaultValue(NukeMode.BLOCKS)
            .setDescription("Nuking mode")
            .build();
    Config<ScanMode> scanMode = new EnumConfig.Builder<ScanMode>("Scan")
            .setValues(ScanMode.values())
            .setDefaultValue(ScanMode.SPHERE)
            .setDescription("Scanning mode")
            .build();
    Config<Collection<Block>> blocks = new RegistryConfig.Builder<Block>("Blocks")
            .setValues()
            .setRegistry(Registries.BLOCK)
            .setDescription("What blocks to nuke")
            .build();
    Config<Integer> delay = new NumberConfig.Builder<Integer>("Delay")
            .setMin(0).setDefaultValue(250).setMax(1000)
            .setDescription("The delay between each nuking scan")
            .build();
    Config<Float> range = new NumberConfig.Builder<Float>("Range")
            .setMin(0.f).setDefaultValue(4.f).setMax(6.f)
            .setFormat("m")
            .setDescription("The range to nuke blocks")
            .build();

    private final NukeCalcManager calculation =
            new NukeCalcManager(new NukeScanner(this));
    private final Timer timer = new NanoTimer();

    public NukerModule()
    {
        super("Nuker", "Clears nearby blocks", GuiCategory.WORLD);
        INSTANCE = this;
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        if (timer.hasPassed(delay.getValue()))
        {
            calculation.runCalc(scanMode.getValue());
            timer.reset();
            return;
        }

        Collection<BlockPos> result = calculation.getResults();
        if (result == null || result.isEmpty())
        {
            return;
        }

        AutoMineModule autoMine = AutoMineModule.INSTANCE;
        SpeedMineModule speedMine = SpeedMineModule.INSTANCE;
        autoMine.onEnable(); // ensure AutoMines speedmine instance isnt null.

        if (!AutoMineModule.speedMine.hasFreeMine()
                || !speedMine.getDoubleMine().getValue() && speedMine.getMainMiningBlock() != null && !speedMine.getMainMiningBlock().isDoneMining())
        {
            return;
        }

        for (BlockPos pos : result)
        {
            if (!autoMine.canStartMining(pos))
            {
                continue;
            }

            autoMine.startAutoMine(pos);
            break;
        }

        calculation.getScanner().getResult().clear();
    }

    public float getRange()
    {
        return range.getValue();
    }

    public boolean isValid(BlockPos pos, BlockState state)
    {
        SpeedMineModule speedMine = SpeedMineModule.INSTANCE;
        if (speedMine.isMining(pos))
        {
            return false;
        }

        if (mode.getValue() == NukeMode.SHULKERS)
        {
            BlockEntity entity = calculation.getScanner().getBlockEntity(pos);
            if (entity instanceof ShulkerBoxBlockEntity)
            {
                return true;
            }

            return false;
        }

        return blocks.getValue().contains(state.getBlock());
    }

    public enum ScanMode
    {
        SPHERE
        {
            @Override
            public void scan(AsyncBlockScanner scanner, ClientWorld world)
            {
                scanner.createSphere(world, mc.player.getBlockPos());
            }
        },
        CUBE
        {
            @Override
            public void scan(AsyncBlockScanner scanner, ClientWorld world)
            {
                scanner.createCube(world, mc.player.getBlockPos());
            }
        };

        public abstract void scan(AsyncBlockScanner scanner, ClientWorld world);
    }

    private enum NukeMode
    {
        BLOCKS,
        SHULKERS
    }
}
