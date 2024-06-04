package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.shoreline.client.util.Globals;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends MixinScreen implements Globals
{
//    @Shadow
//    @Final
//    private DirectionalLayoutWidget grid;
//    @Shadow
//    @Final
//    private Text reason;
//    @Shadow
//    @Final
//    private Text buttonLabel;
//    @Shadow
//    @Final
//    private Screen parent;
//    @Shadow
//    @Final
//    private static Text TO_TITLE_TEXT;
//
//    @Shadow
//    protected abstract void initTabNavigation();
//
//    //
//    @Unique
//    private long reconnectSeconds;
//
//    /**
//     * @param ci
//     */
//    @Inject(method = "init", at = @At(value = "HEAD"), cancellable = true)
//    private void hookInit(CallbackInfo ci) {
//        ci.cancel();
//        grid.getMainPositioner().alignHorizontalCenter().margin(2);
//        ButtonWidget.Builder reconnectButton = ButtonWidget.builder(Text.of("Reconnect"),
//                (button) ->
//                {
//                    if (Managers.NETWORK.getAddress() != null && Managers.NETWORK.getInfo() != null) {
//                        ConnectScreen.connect((DisconnectedScreen) (Object) this, mc,
//                                Managers.NETWORK.getAddress(), Managers.NETWORK.getInfo(), false);
//                    }
//                });
//        ButtonWidget.Builder autoReconnectButton = ButtonWidget.builder(
//                Text.of("AutoReconnect"), (button) ->
//                {
//                    Modules.AUTO_RECONNECT.toggle();
//                    if (Modules.AUTO_RECONNECT.isEnabled()) {
//                        reconnectSeconds = Math.round(Modules.AUTO_RECONNECT.getDelay() * 20.0f);
//                    }
//                });
//        grid.add(new TextWidget(title, mc.textRenderer));
//        grid.add(new MultilineTextWidget(reason, mc.textRenderer).setMaxWidth(width - 50).setCentered(true));
//        ButtonWidget.Builder buttonWidget = mc.isMultiplayerEnabled() ? ButtonWidget.builder(buttonLabel, button -> mc.setScreen(parent)) : ButtonWidget.builder(TO_TITLE_TEXT, button -> mc.setScreen(new TitleScreen()));
//        grid.add(reconnectButton.build());
//        grid.add(autoReconnectButton.build());
//        grid.add(buttonWidget.build());
//        grid.refreshPositions();
//        grid.forEachChild(this::addDrawableChild);
//        initTabNavigation();
//        // addDrawableChild(reconnectButton.dimensions(width / 2 - 100,
//        //        height / 2 + mc.textRenderer.fontHeight + 24, 200, 20).build());
//        // addDrawableChild(autoReconnectButton.dimensions(width / 2 - 100,
//        //        height / 2 + mc.textRenderer.fontHeight + 48, 200, 20).build());
//        if (Modules.AUTO_RECONNECT.isEnabled()) {
//            reconnectSeconds = Math.round(Modules.AUTO_RECONNECT.getDelay() * 20.0f);
//        }
//    }
//
//    /**
//     *
//     */
//    @Override
//    public void tick() {
//        super.tick();
//        if (getDrawables().size() > 1) {
//            if (Modules.AUTO_RECONNECT.isEnabled()) {
//                ((AccessorClickableWidget) getDrawables().get(3)).setMessage(
//                        Text.of("AutoReconnect (" + (reconnectSeconds / 20 + 1) + ")"));
//                if (reconnectSeconds > 0) {
//                    --reconnectSeconds;
//                } else if (Managers.NETWORK.getAddress() != null && Managers.NETWORK.getInfo() != null) {
//                    ConnectScreen.connect((DisconnectedScreen) (Object) this, mc,
//                            Managers.NETWORK.getAddress(), Managers.NETWORK.getInfo(), false);
//                }
//            } else {
//                ((AccessorClickableWidget) getDrawables().get(3)).setMessage(Text.of("AutoReconnect"));
//            }
//        }
//    }
}
