package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.shoreline.client.impl.event.gui.hud.RenderPlayerListEvent;
import net.shoreline.client.impl.module.misc.BetterTabModule;
import net.shoreline.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud
{
    @Shadow
    @Final
    private static Comparator<PlayerListEntry> ENTRY_ORDERING;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Nullable
    private Text header;

    @Shadow
    @Nullable
    private Text footer;

    @Shadow
    public abstract Text getPlayerName(PlayerListEntry entry);

    @Shadow
    protected abstract List<PlayerListEntry> collectPlayerEntries();

    @Inject(method = "collectPlayerEntries", at = @At(value = "HEAD"), cancellable = true)
    private void hookCollectEntries(CallbackInfoReturnable<List<PlayerListEntry>> cir)
    {
        RenderPlayerListEvent.Collect event = new RenderPlayerListEvent.Collect();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(event.getPlayers());
        }
    }

    @ModifyConstant(
            method = "render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            constant = @Constant(intValue = 20)
    )
    private int hookRender(int constant)
    {
        return BetterTabModule.INSTANCE.isEnabled() ? BetterTabModule.INSTANCE.getPlayerColumns().getValue() : constant;
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "HEAD"))
    private void hookRender$Pre(DrawContext context,
                                int scaledWindowWidth,
                                Scoreboard scoreboard,
                                ScoreboardObjective objective,
                                CallbackInfo ci)
    {
        List<PlayerListEntry> entries = collectPlayerEntries();
        final int n = entries.size();

        int rows = n;
        int cols = 1;
        while (rows > 20)
        {
            rows = (n + ++cols - 1) / cols;
        }

        int spaceW = client.textRenderer.getWidth(" ");
        int nameW = 0;
        int extraW = 0;

        for (PlayerListEntry e : entries) {
            nameW = Math.max(nameW, client.textRenderer.getWidth(getPlayerName(e)));
            if (objective != null)
            {
                ReadableScoreboardScore scoreboardScore = scoreboard.getScore(ScoreHolder.fromProfile(e.getProfile()), objective);
                if (objective.getRenderType() != ScoreboardCriterion.RenderType.HEARTS)
                {
                    NumberFormat format = objective.getNumberFormatOr(StyledNumberFormat.YELLOW);
                    Text formatted = ReadableScoreboardScore.getFormattedScore(scoreboardScore, format);
                    int w = (formatted != null) ? client.textRenderer.getWidth(formatted) : 0;
                    extraW = Math.max(extraW, w > 0 ? spaceW + w : 0);
                }
            }
        }

        boolean showHeads = client.isInSingleplayer() || client.getNetworkHandler().getConnection().isEncrypted();
        int q = (objective == null) ? 0 : (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS ? 90 : extraW);

        int colW = Math.min(cols * ((showHeads ? 9 : 0) + nameW + q + 13), scaledWindowWidth - 50) / cols;

        int listW = colW * cols + (cols - 1) * 5;
        int centerX = scaledWindowWidth / 2;

        int s = 10;
        int t = listW;

        final TextRenderer tr = client.textRenderer;
        final int fontH = tr.fontHeight;
        int headerLines = 0;
        int footerLines = 0;

        if (this.header != null)
        {
            List<OrderedText> wrapped = tr.wrapLines(this.header, scaledWindowWidth - 50);
            headerLines = wrapped.size();
            for (OrderedText line : wrapped)
            {
                t = Math.max(t, tr.getWidth(line));
            }

            s += headerLines * fontH + 1;
        }

        int panelTop = s - 1;
        int panelBottom = s + rows * 9;

        if (this.footer != null)
        {
            List<OrderedText> wrapped = tr.wrapLines(this.footer, scaledWindowWidth - 50);
            footerLines = wrapped.size();
            for (OrderedText line : wrapped) t = Math.max(t, tr.getWidth(line));
        }

        int x1 = centerX - t / 2 - 1;
        int x2 = centerX + t / 2 + 1;
        int y1 = (headerLines > 0) ? (10 - 1) : panelTop;
        int y2;
        if (footerLines > 0)
        {
            int headerHeight = headerLines * fontH;
            int panelStartY  = 10 + headerHeight + 1;
            int footerFillTop = panelStartY + rows * 9 + 1;
            y2 = footerFillTop + footerLines * fontH;
        }
        else
        {
            y2 = panelBottom;
        }

        RenderPlayerListEvent.Pre event = new RenderPlayerListEvent.Pre(context, x1, y1, x2, y2);
        EventBus.INSTANCE.dispatch(event);
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void hookRender$Post(DrawContext context,
                                int scaledWindowWidth,
                                Scoreboard scoreboard,
                                ScoreboardObjective objective,
                                CallbackInfo ci)
    {
        RenderPlayerListEvent.Post event = new RenderPlayerListEvent.Post(context);
        EventBus.INSTANCE.dispatch(event);

    }

    @Redirect(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
    private int hookRender$Text(DrawContext instance,
                                TextRenderer textRenderer,
                                Text text,
                                int x,
                                int y,
                                int color)
    {
        RenderPlayerListEvent.DrawText event = new RenderPlayerListEvent.DrawText(text);
        EventBus.INSTANCE.dispatch(event);
        int c = event.isCanceled() ? event.getColor() : color;

        return instance.drawTextWithShadow(textRenderer, text, x, y, c);
    }
}
