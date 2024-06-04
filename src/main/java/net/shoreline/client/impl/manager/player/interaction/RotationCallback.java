package net.shoreline.client.impl.manager.player.interaction;

/**
 * @author xgraza
 * @since 1.0
 */
public interface RotationCallback
{
    /**
     * Handles a rotation for this interaction
     *
     * @param state if this callback is called before or after the interaction is completed
     */
    void handleRotation(final boolean state, final float[] angles);
}