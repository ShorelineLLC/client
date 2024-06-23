package net.shoreline.server.encryption;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Encryption
{
    /**
     * Our secret hash key, same as the loader
     */
    private static final String SECRET_HASH_KEY = "VJ146naKEtYcwlmxmVwjS9tFEIeFnD6H";

    public static String encrypt(String str)
    {
        try
        {
            String combinedKey = str + SECRET_HASH_KEY;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combinedKey.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash)
            {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
}
