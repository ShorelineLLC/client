package net.shoreline.server.token;

import java.util.HashMap;

public final class TokenManager
{
    private final HashMap<String, Long> pendingTokens = new HashMap<>();

    public String getAndAuthorizeNewToken()
    {
        String tempToken = generateRandomToken();
        this.pendingTokens.put(tempToken, System.currentTimeMillis());
        return tempToken;
    }

    public boolean isAuthorizedToken(String token)
    {
        this.pendingTokens.entrySet()
                .removeIf(entry -> (System.currentTimeMillis() - entry.getValue()) > 500L);

        return this.pendingTokens.remove(token) != null;
    }

    private String generateRandomToken()
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
