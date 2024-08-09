package net.shoreline.server.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class Encryption
{
    /**
     * Our secret hash key, same as the loader/installer
     *
     * Note that this key is not secret because it can reverse any hashes but because
     * it is a required component to ensure the same plaintext string gets encrypted to the
     * same value
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

    private static final byte[] ENCRYPTION_KEY = new byte[] {
            16, 110, 1, -8, 44, 103, 18, 0, 84, 37, -111, -112, -2, 39, 120, 54
    };

    public static byte[] encryptReversible(byte[] bytecode) throws Throwable
    {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedBytecode = cipher.doFinal(bytecode);

        byte[] result = new byte[16 + encryptedBytecode.length];
        System.arraycopy(iv, 0, result, 0, 16);
        System.arraycopy(encryptedBytecode, 0, result, 16, encryptedBytecode.length);

        return result;
    }
}
