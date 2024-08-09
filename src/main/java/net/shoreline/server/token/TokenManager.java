package net.shoreline.server.token;

import java.util.concurrent.ConcurrentHashMap;

public final class TokenManager
{
    private final long authorizationDelay;
    private final ConcurrentHashMap<String, Long> pendingTokens = new ConcurrentHashMap<>();

    public TokenManager(long authorizationDelay)
    {
        this.authorizationDelay = authorizationDelay;
    }

    public String getAndAuthorizeNewToken()
    {
        String tempToken = generateRandomToken();
        this.pendingTokens.put(tempToken, System.currentTimeMillis());
        return tempToken;
    }

    public boolean isAuthorizedToken(String token)
    {
        this.pendingTokens.entrySet()
                .removeIf(entry -> (System.currentTimeMillis() - entry.getValue()) > this.authorizationDelay);

        return this.pendingTokens.remove(token) != null;
    }

    public static String generateRandomToken()
    {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 32; i++)
        {
            token.append(characters.charAt((int) (Math.random() * characters.length())));
        }

        return token.toString();
    }
}
