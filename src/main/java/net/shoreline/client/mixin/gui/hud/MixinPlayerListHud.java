package net.shoreline.client.mixin.gui.hud;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.shoreline.eventbus.bus.EventBus;
import net.shoreline.client.impl.event.gui.hud.PlayerListColumnsEvent;
import net.shoreline.client.impl.event.gui.hud.PlayerListEvent;
import net.shoreline.client.impl.event.gui.hud.PlayerListNameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

/**
 * @author linus, hockeyl8
 * @since 1.0
 */
@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud {

    @Shadow
    @Final
    private static Comparator<PlayerListEntry> ENTRY_ORDERING;

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract List<PlayerListEntry> collectPlayerEntries();

    @Shadow
    protected abstract Text applyGameModeFormatting(PlayerListEntry entry, MutableText name);

    @Inject(method = "getPlayerName", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        Text text;
        if (entry.getDisplayName() != null) {
            text = applyGameModeFormatting(entry, entry.getDisplayName().copy());
        } else {
            text = applyGameModeFormatting(entry, Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName())));
        }
        PlayerListNameEvent playerListNameEvent = new PlayerListNameEvent(text, entry.getProfile().getId());
        EventBus.INSTANCE.dispatch(playerListNameEvent);
        if (playerListNameEvent.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(playerListNameEvent.getPlayerName());
        }
    }

    /**
     * @param cir
     */
    @Inject(method = "collectPlayerEntries", at = @At(value = "HEAD"), cancellable = true)
    private void hookCollectPlayerEntries(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        PlayerListEvent playerListEvent = new PlayerListEvent();
        EventBus.INSTANCE.dispatch(playerListEvent);
        if (playerListEvent.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(client.player.networkHandler.getListedPlayerListEntries()
                    .stream().sorted(ENTRY_ORDERING).limit(playerListEvent.getSize()).toList());
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", shift = At.Shift.BEFORE))
    private void hookRender(CallbackInfo ci, @Local(ordinal = 5) LocalIntRef o, @Local(ordinal = 6)LocalIntRef p) {
        int newO;
        int newP = 1;
        int totalPlayers = newO = this.collectPlayerEntries().size();

        PlayerListColumnsEvent playerListColumsEvent = new PlayerListColumnsEvent();
        EventBus.INSTANCE.dispatch(playerListColumsEvent);
        if (playerListColumsEvent.isCanceled())
        {
            while (newO > playerListColumsEvent.getTabHeight())
            {
                newO = (totalPlayers + ++newP - 1) / newP;
            }

            o.set(newO);
            p.set(newP);
        }
    }
}
