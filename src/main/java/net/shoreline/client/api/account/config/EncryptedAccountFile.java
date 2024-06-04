package net.shoreline.client.api.account.config;

import java.nio.file.Path;

/**
 * @author xgraza
 * @since 03/31/24
 */
public final class EncryptedAccountFile extends AccountFile
{
    private String encryptionKey;

    public EncryptedAccountFile(Path dir)
    {
        super(dir, "accounts_enc");
    }

    @Override
    public void save()
    {

    }

    @Override
    public void load()
    {

    }

    public void setEncryptionKey(String encryptionKey)
    {
        this.encryptionKey = encryptionKey;
    }
}
