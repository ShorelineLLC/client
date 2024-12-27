package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.ConfigArgumentType;
import net.shoreline.client.api.command.ListArgumentType;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.*;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.util.KeyboardUtil;
import net.shoreline.client.util.chat.ChatUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class ModuleCommand extends Command
{
    //
    private final Module module;

    /**
     * @param module
     */
    public ModuleCommand(Module module)
    {
        super(module.getName(), module.getDescription(), literal(module.getName().toLowerCase()));
        this.module = module;
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("setting", ConfigArgumentType.config(module))
                .then(argument("value", StringArgumentType.string())
                        .suggests((c, b) ->
                        {
                            Config<?> config = ConfigArgumentType.getConfig(c, "setting");
                            if (config instanceof BlockListConfig || config instanceof ItemListConfig || config instanceof EntityListConfig)
                            {
                                b.suggest("add");
                                b.suggest("del");
                                b.suggest("remove");
                                b.suggest("clear");
                                b.suggest("list");
                            }
                            return b.buildFuture();
                        })
                        .executes(c ->
                        {
                            Config<?> config = ConfigArgumentType.getConfig(c, "setting");
                            String value = StringArgumentType.getString(c, "value");
                            if (value.equalsIgnoreCase("list"))
                            {
                                return listItems(config, value);
                            }
                            else if (value.equalsIgnoreCase("reset"))
                            {
                                config.resetValue();
                                ChatUtil.clientSendMessage("§7%s§f was reset to default value", config.getName());
                                return 1;
                            }
                            else if (value.equalsIgnoreCase("clear"))
                            {
                                if (config instanceof ItemListConfig)
                                {
                                    ((ItemListConfig) config).clear();
                                }
                                else if (config instanceof BlockListConfig)
                                {
                                    ((BlockListConfig) config).clear();
                                }
                                else if (config instanceof EntityListConfig<?>)
                                {
                                    ((EntityListConfig) config).clear();
                                }
                                ChatUtil.clientSendMessage("§7%s§f was cleared", config.getName());
                                return 1;
                            }
                            return updateValue(config, value);
                        })
                        .then(argument("list", ListArgumentType.list())
                                .executes(c ->
                                {
                                    Config<?> config = ConfigArgumentType.getConfig(c, "setting");
                                    if (config instanceof BlockListConfig || config instanceof ItemListConfig || config instanceof EntityListConfig)
                                    {
                                        String action = StringArgumentType.getString(c, "value");
                                        Object value = ListArgumentType.getListItem(c, "list");
                                        return addDeleteItem(config, action, value);
                                    }
                                    return 0;
                                })))
                .executes(c ->
                {
                    ChatUtil.error("Must provide a value!");
                    return 1;
                })).executes(c ->
        {
            if (module instanceof ToggleModule m)
            { // Can use the module command to toggle
                m.toggle();
                ChatUtil.clientSendMessage("%s is now %s", "§7" + m.getName() + "§f", m.isEnabled() ? "§senabled§f" : "§cdisabled§f");
            }
            return 1;
        });
    }

    private int addDeleteItem(Config<?> config, String action, Object value)
    {
        if (config instanceof ItemListConfig)
        {
            if (!(value instanceof Item item))
            {
                ChatUtil.error("Not an item!");
                return 0;
            }
            if (action.equalsIgnoreCase("add"))
            {
                ((ItemListConfig) config).add(item);
                ChatUtil.clientSendMessage("Added §s" + item.getName().getString() + "§f to §7" + config.getName());
            }
            else if (action.equalsIgnoreCase("del") || action.equalsIgnoreCase("remove"))
            {
                ((ItemListConfig) config).remove(item);
                ChatUtil.clientSendMessage("Removed §c" + item.getName().getString() + "§f from §7" + config.getName());
            }
        }
        else if (config instanceof BlockListConfig)
        {
            if (value instanceof Item item)
            {
                value = Block.getBlockFromItem(item);
            }
            if (!(value instanceof Block block))
            {
                ChatUtil.error("Not a block!");
                return 0;
            }
            if (action.equalsIgnoreCase("add"))
            {
                ((BlockListConfig) config).add(block);
                ChatUtil.clientSendMessage("Added §s" + block.getName().getString() + "§f to §7" + config.getName());
            }
            else if (action.equalsIgnoreCase("del") || action.equalsIgnoreCase("remove"))
            {
                ((BlockListConfig) config).remove(block);
                ChatUtil.clientSendMessage("Removed §c" + block.getName().getString() + "§f from §7" + config.getName());
            }
        }
        else if (config instanceof EntityListConfig)
        {
            if (!(value instanceof EntityType entity))
            {
                ChatUtil.error("Not an entity type!");
                return 0;
            }
            if (action.equalsIgnoreCase("add"))
            {
                ((EntityListConfig) config).add(entity);
                ChatUtil.clientSendMessage("Added §s" + entity.getName().getString() + "§f to §7" + config.getName());
            }
            else if (action.equalsIgnoreCase("del") || action.equalsIgnoreCase("remove"))
            {
                ((EntityListConfig) config).remove(entity);
                ChatUtil.clientSendMessage("Removed §c" + entity.getName().getString() + "§f from §7" + config.getName());
            }
        }
        return 1;
    }

    private int listItems(Config<?> config, String action)
    {
        if (config instanceof ItemListConfig)
        {
            List<Item> list = ((List<Item>) config.getValue());
            if (action.equalsIgnoreCase("list"))
            {
                if (list.isEmpty())
                {
                    ChatUtil.error("There are no items in the list!");
                    return 1;
                }
                List<String> listString = new ArrayList<>();
                for (Item item : list)
                {
                    listString.add(item.getName().getString());
                }
                ChatUtil.clientSendMessage("§7" + config.getName() + "§f: " + String.join(", ", listString));
            }
        }
        else if (config instanceof BlockListConfig)
        {
            List<Block> list = ((List<Block>) config.getValue());
            if (action.equalsIgnoreCase("list"))
            {
                if (list.isEmpty())
                {
                    ChatUtil.error("There are no blocks in the list!");
                    return 1;
                }
                List<String> listString = new ArrayList<>();
                for (Block block : list)
                {
                    listString.add(block.getName().getString());
                }
                ChatUtil.clientSendMessage("§7" + config.getName() + "§f: " + String.join(", ", listString));
            }
        }
        else if (config instanceof EntityListConfig)
        {
            List<EntityType> list = ((List<EntityType>) config.getValue());
            if (action.equalsIgnoreCase("list"))
            {
                if (list.isEmpty())
                {
                    ChatUtil.error("There are no entities in the list!");
                    return 1;
                }
                List<String> listString = new ArrayList<>();
                for (EntityType entityType : list)
                {
                    listString.add(entityType.getName().getString());
                }
                ChatUtil.clientSendMessage("§7" + config.getName() + "§f: " + String.join(", ", listString));
            }
        }

        return 1;
    }

    private int updateValue(Config<?> config, String value)
    {
        if (config == null || value == null)
        {
            return 0;
        }
        // parse value
        try
        {
            if (config.getValue() instanceof Integer)
            {
                Integer val = Integer.parseInt(value);
                if (val.doubleValue() < ((NumberConfig) config).getMin().doubleValue())
                {
                    ChatUtil.error("Value less than min!");
                    return 0;
                }
                if (val.doubleValue() > ((NumberConfig) config).getMax().doubleValue())
                {
                    ChatUtil.error("Value greater than max!");
                    return 0;
                }
                ((Config<Integer>) config).setValue(val);
                ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), val.toString());
            }
            else if (config.getValue() instanceof Float)
            {
                Float val = Float.parseFloat(value);
                if (val.doubleValue() < ((NumberConfig) config).getMin().doubleValue())
                {
                    ChatUtil.error("Value less than min!");
                    return 0;
                }
                if (val.doubleValue() > ((NumberConfig) config).getMax().doubleValue())
                {
                    ChatUtil.error("Value greater than max!");
                    return 0;
                }
                ((Config<Float>) config).setValue(val);
                ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), val.toString());
            }
            else if (config.getValue() instanceof Double)
            {
                Double val = Double.parseDouble(value);
                if (val.doubleValue() < ((NumberConfig) config).getMin().doubleValue())
                {
                    ChatUtil.error("Value less than min!");
                    return 0;
                }
                if (val.doubleValue() > ((NumberConfig) config).getMax().doubleValue())
                {
                    ChatUtil.error("Value greater than max!");
                    return 0;
                }
                ((Config<Double>) config).setValue(val);
                ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), val.toString());
            }
        }
        catch (NumberFormatException e)
        {
            ChatUtil.error("Not a number!");
            // e.printStackTrace();
        }
        if (config.getValue() instanceof Boolean)
        {
            Boolean val = parseBoolean(value);
            if (val == null)
            {
                ChatUtil.error("Invalid value!");
                return 0;
            }
            ((Config<Boolean>) config).setValue(val);
            ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), val ? "True" : "False");
        }
        else if (config.getValue() instanceof Enum<?>)
        {
            String[] values = Arrays.stream(((Enum<?>) config.getValue()).getClass()
                    .getEnumConstants()).map(Enum::name).toArray(String[]::new);
            // TODO: FIX THIS!
            int ix = -1;
            for (int i = 0; i < values.length; i++)
            {
                if (values[i].equalsIgnoreCase(value))
                {
                    ix = i;
                    break;
                }
            }
            if (ix == -1)
            {
                ChatUtil.error("Not a valid mode!");
                return 0;
            }
            Enum<?> val = Enum.valueOf(((Enum<?>) config.getValue()).getClass(), values[ix]);
            ((Config<Enum<?>>) config).setValue(val);
            ChatUtil.clientSendMessage("§7%s§f was set to mode §s%s", config.getName(), value);
        }
        else if (config.getValue() instanceof Macro macro)
        {
            if (config.getName().equalsIgnoreCase("Keybind"))
            {
                ChatUtil.error("Use the 'bind' command to keybind modules!");
                return 0;
            }
            ((Config<Macro>) config).setValue(new Macro(config.getId(), KeyboardUtil.getKeyCode(value), macro.getRunnable()));
            ChatUtil.clientSendMessage("§7%s§f was set to key §s%s", config.getName(), value);
        }
        else if (config.getValue() instanceof String)
        {
            ((Config<String>) config).setValue(value);
        }
        else if (config.getValue() instanceof Color)
        {
            try
            {
                Color color = ((ColorConfig) config).parseColor(value);
                ((Config<Color>) config).setValue(color);
                ChatUtil.clientSendMessage("§7%s§f was set to §s%s", config.getName(), value);
            }
            catch (IllegalArgumentException e)
            {
                ChatUtil.error("Invalid color!");
            }
        }
        return 1;
    }

    private Boolean parseBoolean(String string)
    {
        if (string.equalsIgnoreCase("True") || string.equalsIgnoreCase("On"))
        {
            return true;
        }
        else if (string.equalsIgnoreCase("False") || string.equalsIgnoreCase("Off"))
        {
            return false;
        }
        return null;
    }
}
