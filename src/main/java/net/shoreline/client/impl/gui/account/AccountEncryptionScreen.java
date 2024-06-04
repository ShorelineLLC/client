package net.shoreline.client.impl.gui.account;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.shoreline.client.init.Managers;

/**
 * @author xgraza
 * @since 03/31/24
 */
public final class AccountEncryptionScreen extends Screen
{
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-={}[]\\|'\";:/?.>,<`";
    private static final String[] REQUIREMENTS = {"8+ Characters", "A Special Character", "A Number", "An Uppercase Letter"};

    private final Screen parent;
    private TextFieldWidget passwordTextField;

    public AccountEncryptionScreen(final Screen parent)
    {
        super(Text.of("Account Encryption"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        clearChildren();

        passwordTextField = new TextFieldWidget(client.textRenderer, 145, 20, Text.empty());
        passwordTextField.setPlaceholder(Text.of("Enter Password..."));
        passwordTextField.setPosition(width / 2 - passwordTextField.getWidth() / 2, height / 2 - 60);
        addDrawableChild(passwordTextField);

        addDrawableChild(ButtonWidget.builder(Text.of("Encrypt"), (action) ->
                {
                    // TODO
                }).dimensions(width / 2 - 145 / 2, passwordTextField.getY() + 90, 145, 20)
                .tooltip(Tooltip.of(Text.of("This will require you to enter a password every time you enter the account manager the first time!")))
                .build());
        addDrawableChild(ButtonWidget.builder(Text.of("Go Back"), (action) -> client.setScreen(parent))
                .dimensions(width / 2 - 145 / 2, passwordTextField.getY() + 112, 145, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        // wow this code is god awful
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(client.textRenderer,
                "Encrypt Accounts (" + Managers.ACCOUNT.getAccounts().size() + ")", width / 2, height / 2 - 125, -1);
        context.drawTextWithShadow(client.textRenderer,
                (isPasswordSecure(passwordTextField.getText()) ? Formatting.GREEN : Formatting.RED) + "*",
                width / 2 - passwordTextField.getWidth() / 2 - 6,
                passwordTextField.getY() + 10 - (client.textRenderer.fontHeight / 2), -1);

        {
            context.drawTextWithShadow(client.textRenderer, "Minimum Requirements:",
                    passwordTextField.getX() + 1,
                    passwordTextField.getY() + passwordTextField.getHeight() + 10,
                    -1);
            for (int i = 0; i < REQUIREMENTS.length; ++i)
            {
                final String requirement = REQUIREMENTS[i];
                context.drawTextWithShadow(client.textRenderer, "- " + requirement,
                        passwordTextField.getX() + 6,
                        passwordTextField.getY() + passwordTextField.getHeight() + 10 + ((textRenderer.fontHeight + 2) * (1 + i)),
                        -1);
            }
        }
    }

    /**
     * Checks if the password is "secure"
     *
     * @return if the password is greater than 8 characters, contains a special character, a number, and an uppercase letter
     */
    private boolean isPasswordSecure(final String password)
    {
        if (password.length() < 8)
        {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        final char[] characters = password.toCharArray();
        for (final char c : characters)
        {
            if (Character.isUpperCase(c))
            {
                hasUppercase = true;
            }
            if (SPECIAL_CHARACTERS.indexOf(c) != -1)
            {
                hasSpecial = true;
            }
            if (c >= 48 && c <= 57)
            {
                hasNumber = true;
            }
        }
        return hasUppercase && hasNumber && hasSpecial;
    }
}
