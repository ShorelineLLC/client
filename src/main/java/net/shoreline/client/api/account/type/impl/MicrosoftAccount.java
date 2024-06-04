package net.shoreline.client.api.account.type.impl;

import com.google.gson.JsonObject;
import net.minecraft.client.session.Session;
import net.shoreline.client.api.account.msa.exception.MSAAuthException;
import net.shoreline.client.api.account.type.MinecraftAccount;
import net.shoreline.client.impl.manager.client.AccountManager;

/**
 * @author xgraza
 * @since 03/31/24
 */
public final class MicrosoftAccount implements MinecraftAccount
{
    private final String email, password;
    private String accessToken, username;

    /**
     * Create a MicrosoftAccount instance using a previously saved access token
     *
     * @param accessToken the access token
     * @throws RuntimeException if access token is null or empty
     */
    public MicrosoftAccount(final String accessToken)
    {
        this(null, null);
        if (accessToken == null || accessToken.isEmpty())
        {
            throw new RuntimeException("Access token should not be null");
        }
        this.accessToken = accessToken;
    }

    /**
     * Creates a MicrosoftAccount instance using login credentials
     *
     * @param email    the microsoft email
     * @param password the account password
     */
    public MicrosoftAccount(final String email, final String password)
    {
        this.email = email;
        this.password = password;
    }

    @Override
    public Session login()
    {
        Session session = null;
        try
        {
            if (email != null && password != null)
            {
                try
                {
                    session = AccountManager.MSA_AUTHENTICATOR.loginWithCredentials(
                            email, password);
                }
                catch (MSAAuthException e)
                {
                    AccountManager.MSA_AUTHENTICATOR.setLoginStage(e.getMessage());
                    return null;
                }
            }
            else if (accessToken != null)
            {
                if (accessToken.startsWith("M."))
                {
                    accessToken = AccountManager.MSA_AUTHENTICATOR.getLoginToken(accessToken);
                }
                session = AccountManager.MSA_AUTHENTICATOR.loginWithToken(accessToken, true);
            }
        }
        catch (MSAAuthException e)
        {
            e.printStackTrace();
            AccountManager.MSA_AUTHENTICATOR.setLoginStage(e.getMessage());
            return null;
        }

        if (session != null)
        {
            AccountManager.MSA_AUTHENTICATOR.setLoginStage("");
            username = session.getUsername();
            return session;
        }
        return null;
    }

    @Override
    public JsonObject toJSON()
    {
        final JsonObject object = MinecraftAccount.super.toJSON();
        // If we have an access token, we have a browser account
        if (accessToken != null)
        {
            object.addProperty("token", accessToken);
        }
        else
        {
            if (email == null || password == null)
            {
                throw new RuntimeException("Email/Password & Access token is null for a MSA?");
            }
            object.addProperty("email", email);
            object.addProperty("password", password);
        }
        return object;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public String username()
    {
        if (username != null)
        {
            return username;
        }
        return email;
    }

    public String getUsernameOrNull()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getAccessToken()
    {
        return accessToken;
    }
}
