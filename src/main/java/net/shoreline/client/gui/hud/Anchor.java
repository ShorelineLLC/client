package net.shoreline.client.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.shoreline.client.impl.module.misc.BetterChatModule;
import net.shoreline.client.impl.module.render.NoRenderModule;

public enum Anchor
    {
        None
                {
                    @Override
                    public float getX(float screenWidth, float elementWidth)
                    {
                        return 0;
                    }

                    @Override
                    public float getY(float screenHeight, float elementHeight, float offset)
                    {
                        return offset;
                    }
                },
        Top_Left
                {
                    @Override
                    public float getX(float screenWidth, float elementWidth)
                    {
                        return 0;
                    }

                    @Override
                    public float getY(float screenHeight, float elementHeight, float offset)
                    {
                        return offset;
                    }
                },
        Top_Right
                {
                    @Override
                    public float getX(float screenWidth, float elementWidth)
                    {
                        return screenWidth - elementWidth;
                    }

                    @Override
                    public float getY(float screenHeight, float elementHeight, float offset)
                    {
                        if (!MinecraftClient.getInstance().player.getStatusEffects().isEmpty() && !NoRenderModule.INSTANCE.getPotionsHud().getValue())
                        {
                            return offset + 25;
                        }

                        return offset;
                    }
                },
        Bottom_Left
                {
                    @Override
                    public float getX(float screenWidth, float elementWidth)
                    {
                        return 0;
                    }

                    @Override
                    public float getY(float screenHeight, float elementHeight, float offset)
                    {
                        if (MinecraftClient.getInstance().inGameHud.getChatHud().isChatFocused())
                        {
                            return screenHeight - elementHeight - offset - (15.0f * (float) BetterChatModule.INSTANCE.getChatFactor());
                        }

                        return screenHeight - elementHeight - offset;
                    }
                },
        Bottom_Right
                {
                    @Override
                    public float getX(float screenWidth, float elementWidth)
                    {
                        return screenWidth - elementWidth;
                    }

                    @Override
                    public float getY(float screenHeight, float elementHeight, float offset)
                    {
                        if (MinecraftClient.getInstance().inGameHud.getChatHud().isChatFocused())
                        {
                            return screenHeight - elementHeight - offset - (15.0f * (float) BetterChatModule.INSTANCE.getChatFactor());
                        }

                        return screenHeight - elementHeight - offset;
                    }
                },
        Middle
                {
                    @Override
                    public float getX(float screenWidth, float elementWidth)
                    {
                        return (screenWidth / 2f) - (elementWidth / 2f);
                    }

                    @Override
                    public float getY(float screenHeight, float elementHeight, float offset)
                    {
                        return offset;
                    }
                };

        public abstract float getX(float screenWidth, float elementWidth);

        public abstract float getY(float screenHeight, float elementHeight, float offset);
    }