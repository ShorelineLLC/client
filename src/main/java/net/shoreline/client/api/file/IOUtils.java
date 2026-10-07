package net.shoreline.client.api.file;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class IOUtils
{
    public String readFile(Path path) throws IOException
    {
        StringBuilder content = new StringBuilder();
        InputStream in = Files.newInputStream(path);
        int b;
        while ((b = in.read()) != -1)
        {
            content.append((char) b);
        }

        in.close();
        return content.toString();
    }

    public void writeFile(Path path,
                          String content) throws IOException
    {
        OutputStream out = Files.newOutputStream(path);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        out.write(bytes, 0, bytes.length);
        out.close();
    }
}
