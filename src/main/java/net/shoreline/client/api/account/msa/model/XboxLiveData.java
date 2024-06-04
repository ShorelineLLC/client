package net.shoreline.client.api.account.msa.model;

/**
 * @author xgraza
 * @since 01/14/24
 */
public final class XboxLiveData
{
    private String token, userHash;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getUserHash()
    {
        return userHash;
    }

    public void setUserHash(String userHash)
    {
        this.userHash = userHash;
    }
}
