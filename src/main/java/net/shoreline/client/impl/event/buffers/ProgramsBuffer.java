package net.shoreline.client.impl.event.buffers;

import net.shoreline.client.init.Programs;

public class ProgramsBuffer {

    public static void hookLoadPrograms()
    {
        Programs.initPrograms();
    }
}
