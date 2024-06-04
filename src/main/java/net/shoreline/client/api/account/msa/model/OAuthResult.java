package net.shoreline.client.api.account.msa.model;

/**
 * @author xgraza
 * @since 01/14/24
 */
public final class OAuthResult
{
    private String sfttTag, postUrl, cookie;

    public String getSfttTag()
    {
        return sfttTag;
    }

    public void setSfttTag(String sfttTag)
    {
        this.sfttTag = sfttTag;
    }

    public String getPostUrl()
    {
        return postUrl;
    }

    public void setPostUrl(String postUrl)
    {
        this.postUrl = postUrl;
    }

    public String getCookie()
    {
        return cookie;
    }

    public void setCookie(String cookie)
    {
        this.cookie = cookie;
    }
}
