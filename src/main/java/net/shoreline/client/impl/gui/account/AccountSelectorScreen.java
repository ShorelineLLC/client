package net.shoreline.client.impl.gui.account;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.shoreline.client.impl.gui.account.list.AccountEntry;
import net.shoreline.client.impl.gui.account.list.AccountListWidget;
import net.shoreline.client.impl.manager.client.AccountManager;
import net.shoreline.client.init.Managers;

/**
 * @author xgraza
 * @since 03/28/24
 * <p>
 * The code in this file may or not cause a seizure
 */
public final class AccountSelectorScreen extends Screen
{

    private final Screen parent;
    private AccountListWidget accountListWidget;
    private TextFieldWidget searchWidget;

    public AccountSelectorScreen(final Screen parent)
    {
        super(Text.of("Account Selector"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        accountListWidget = new AccountListWidget(client, width, height - 64 - 32, 32, 25);
        clearChildren();
        accountListWidget.setDimensionsAndPosition(width, height - 64 - 32, 0, 32);
        accountListWidget.populateEntries();
        accountListWidget.setSearchFilter(null);
        addDrawableChild(searchWidget = new TextFieldWidget(client.textRenderer, 135, 20, Text.of("Search...")));
        searchWidget.setPosition(width / 2 - searchWidget.getWidth() / 2, 4);
        searchWidget.setPlaceholder(Text.of("Search..."));
        final int buttonWidth = 110;
        final int buttonHeight = 20;
        // my head hurts
        addDrawableChild(ButtonWidget.builder(Text.of("Add"),
                        (action) -> client.setScreen(new AccountAddAccountScreen(this)))
                .dimensions(width / 2 + 2, accountListWidget.getHeight() + 40, buttonWidth, buttonHeight)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.of("Login"), (action) ->
        {
            final AccountEntry entry = accountListWidget.getSelectedOrNull();
            if (entry != null)
            {
                final Session session = entry.getAccount().login();
                if (session != null)
                {
                    Managers.ACCOUNT.setSession(session);
                }
            }
        }).dimensions(width / 2 - buttonWidth - 2, accountListWidget.getHeight() + 40, buttonWidth, buttonHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("Back"),
                        (action) -> client.setScreen(parent))
                .dimensions(width / 2 - buttonWidth - 2,
                        accountListWidget.getHeight() + 40 + buttonHeight + 2,
                        buttonWidth, buttonHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("Delete"), (action) ->
        {
            final AccountEntry entry = accountListWidget.getSelectedOrNull();
            if (entry == null)
            {
                return;
            }
            // bypass if the user is holding down shift
            if (InputUtil.isKeyPressed(client.getWindow().getHandle(),
                    InputUtil.GLFW_KEY_LEFT_SHIFT))
            {
                Managers.ACCOUNT.unregister(entry.getAccount());
                client.setScreen(this);
                return;
            }
            client.setScreen(new ConfirmScreen((value) ->
            {
                if (value)
                {
                    Managers.ACCOUNT.unregister(entry.getAccount());
                }
                client.setScreen(this);
            }, Text.of("Delete account?"),
                    Text.of("Are you sure you would like to delete " + entry.getAccount().username() + "?"),
                    Text.of("Yes"),
                    Text.of("No")));
        }).dimensions(width / 2 + 2,
                accountListWidget.getHeight() + 40 + buttonHeight + 2,
                buttonWidth, buttonHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of(Managers.ACCOUNT.isEncrypted() ? "Decrypt" : "Encrypt"), (action) ->
        {
            if (Managers.ACCOUNT.isEncrypted())
            {
            }
            else
            {
                client.setScreen(new AccountEncryptionScreen(this));
            }
        }).dimensions(width - buttonWidth - 4, 6, buttonWidth, buttonHeight).build());
    }

    @Override
    public void onDisplayed()
    {
        if (accountListWidget != null)
        {
            accountListWidget.populateEntries();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        accountListWidget.render(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(client.textRenderer, Text.of(getLoginInfo()), 2, 2, 0xAAAAAA);
        if (searchWidget.isSelected())
        {
            String content = searchWidget.getText();
            if (content == null || content.isEmpty())
            {
                accountListWidget.setSearchFilter(null);
                return;
            }
            accountListWidget.setSearchFilter(content.replaceAll("\\s*", ""));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        accountListWidget.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        accountListWidget.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private String getLoginInfo()
    {
        return AccountManager.MSA_AUTHENTICATOR.getLoginStage().isEmpty() ? "Logged in as " + client.getSession().getUsername() : AccountManager.MSA_AUTHENTICATOR.getLoginStage();
    }
}
