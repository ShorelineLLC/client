package net.shoreline.client.api.config.setting;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.List;
import java.util.stream.Stream;

/**
 * @param <T>
 * @author linus
 * @since 1.0
 */
public class BlockListConfig<T extends List<Item>> extends ItemListConfig<T>
{
    @SuppressWarnings("unchecked")
    public BlockListConfig(String name, String desc, Block... values)
    {
        super(name, desc, (T) Stream.of(values).map(Item::fromBlock).toList());
    }

    /**
     * @param obj
     * @return
     */
    public boolean contains(Object obj)
    {
        if (obj instanceof Block block)
        {
            return value.contains(Item.fromBlock(block));
        }
        return false;
    }
}
