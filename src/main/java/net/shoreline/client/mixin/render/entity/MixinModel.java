package net.shoreline.client.mixin.render.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.imixin.IModel;
import net.shoreline.client.impl.module.render.ChamsModule;
import net.shoreline.client.impl.render.Layers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;

@Mixin(Model.class)
public class MixinModel implements IModel
{
    @Unique
    protected boolean cancelModel = false;

    @Override
    public void cancelModel(boolean cancelModel)
    {
        this.cancelModel = cancelModel;
    }

    @ModifyReturnValue(method = "getLayer", at = @At(value = "RETURN"))
    private RenderLayer getLayerHook(RenderLayer original, @Local(argsOnly = true) Identifier texture)
    {
        if (ChamsModule.getInstance().isEnabled()
                && (ChamsModule.getInstance().mode.getValue() == ChamsModule.ChamsMode.X_Q_Z
                  || ChamsModule.getInstance().mode.getValue() == ChamsModule.ChamsMode.SHINE
                  && ChamsModule.getInstance().model.getValue())
                && cancelModel)
        {
            return Layers.ENTITY.apply(texture, true);
        }

        return original;
    }

    @ModifyArgs(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;" +
                            "render(Lnet/minecraft/client/util/math/MatrixStack;" +
                            "Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void render$render(Args args)
    {
        if (ChamsModule.getInstance().isEnabled()
                && (ChamsModule.getInstance().mode.getValue() == ChamsModule.ChamsMode.X_Q_Z
                || ChamsModule.getInstance().mode.getValue() == ChamsModule.ChamsMode.SHINE)
                && ChamsModule.getInstance().getOpacity() != 1.0f && cancelModel)
        {
            int alpha = (int) (ChamsModule.getInstance().getOpacity() * 255.0f);
            alpha = Math.max(0, Math.min(alpha, 255));
            args.set(4, new Color(255, 255, 255, alpha).getRGB());
        }
    }
}
