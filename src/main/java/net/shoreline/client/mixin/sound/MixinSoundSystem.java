package net.shoreline.client.mixin.sound;

import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(SoundSystem.class)
public class MixinSoundSystem
{
    @Shadow
    private boolean started;
    //
    @Shadow
    private int ticks;
    //
    @Shadow
    @Final
    private Map<SoundInstance, Integer> soundEndTicks;
    //
    @Shadow
    @Final
    private Map<SoundInstance, Channel.SourceManager> sources;

//    /**
//     * @param sound
//     * @param cir
//     */
//    @Inject(method = "isPlaying", at = @At(value = "HEAD"), cancellable = true)
//    public void isPlaying(SoundInstance sound, CallbackInfoReturnable<Boolean> cir) {
//        // Fixes Soundsystem tick crash
//        cir.cancel();
//        Integer i = soundEndTicks.get(sound);
//        if (!started) {
//            cir.setReturnValue(false);
//        }
//        if (i != null && i <= ticks) {
//            cir.setReturnValue(true);
//        }
//        cir.setReturnValue(sources.containsKey(sound));
//    }
}
