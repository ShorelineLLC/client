package net.shoreline.client.impl.gui.account.list;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.shoreline.client.api.account.type.MinecraftAccount;
import net.shoreline.client.init.Managers;

import java.util.List;
import java.util.Objects;

/**
 * @author xgraza
 * @since 03/28/24
 */
public final class AccountListWidget extends AlwaysSelectedEntryListWidget<AccountEntry>
{

    private String searchFilter;

    public AccountListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l)
    {
        super(minecraftClient, i, j, k, l);
    }

    public void populateEntries()
    {
        clearEntries();
        final List<MinecraftAccount> accounts = Managers.ACCOUNT.getAccounts();
        if (!accounts.isEmpty())
        {
            for (final MinecraftAccount account : accounts)
            {
                addEntry(new AccountEntry(account));
            }
            setSelected(getEntry(0));
        }
    }

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta)
    {
        // i love this shit
        List<AccountEntry> entries = children();
        if (searchFilter != null && !searchFilter.isEmpty())
        {
            entries = entries.stream()
                    // i know this looks bad... but hear me out -
                    .filter((entry) -> entry.getAccount().username()
                            .toLowerCase()
                            .contains(searchFilter.toLowerCase()))
                    .toList();
        }
        // below is pretty much just MC code
        int x = getRowLeft();
        int width = getRowWidth();
        int height = itemHeight - 4;
        int size = entries.size();
        for (int i = 0; i < size; ++i)
        {
            int y = getRowTop(i);
            int m = getRowBottom(i);
            if (m >= getY() && y <= getBottom())
            {
                AccountEntry entry = entries.get(i);
                final boolean isHovered = Objects.equals(getHoveredEntry(), entry);
                entry.drawBorder(context, i, y, x, width, height, mouseX, mouseY, isHovered, delta);
                if (Objects.equals(getSelectedOrNull(), entry))
                {
                    int color = isFocused() ? -1 : -8355712;
                    drawSelectionHighlight(context, y, width, height, color, -16777216);
                }
                boolean selected = client.getSession() != null && client.getSession().getUsername().equalsIgnoreCase(entry.getAccount().username());
                entry.render(context, i, y, x, width, height, mouseX, mouseY, selected, delta);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        updateScrollingState(mouseX, mouseY, button);
        final AccountEntry entry = getEntryAtPosition(mouseX, mouseY);
        if (entry != null)
        {
            setSelected(entry);
        }
        if (getSelectedOrNull() != null)
        {
            return getSelectedOrNull().mouseClicked(mouseX, mouseY, button);
        }
        return true;
    }

    public void setSearchFilter(String searchFilter)
    {
        this.searchFilter = searchFilter;
    }
}
