package weebify.dptb2utils.mixins.compat;

import club.sk1er.patcher.mixins.accessors.FontRendererAccessor;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(targets = "club.sk1er.patcher.hooks.FontRendererHook", remap = false)
public class PolyPatcher_FontRendererHookMixin {
    @Unique private static int currentColor = 0;
    @Unique private static float currentAlpha = 0;

    @Redirect(
            method = "renderStringAtPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lclub/sk1er/patcher/mixins/accessors/FontRendererAccessor;getAlpha()F"
            )
    )
    private float getAlpha(FontRendererAccessor thing) {
        currentAlpha = thing.getAlpha();
        return currentAlpha;
    }

    @Redirect(
            method = "renderStringAtPos",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;indexOf(I)I",
                    ordinal = 0
            )
    )
    private int handleCustomColorCodesButFucked(String listText, int character, String text, boolean shadow) {
        char d = (char) character;
        if (d == 'x' || d == 'y') {
            currentColor = DPTB2Utils.hexToInt(DPTB2Utils.getInstance().getStringConfig(d == 'x' ? "others.discColor" : "others.wptbColor")) & 0xFFFFFF;
            return 15;
        }

        return listText.indexOf(character);
    }

    @Inject(
            method = "renderStringAtPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;func_179131_c(FFFF)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void renderStringAtPos(String text, boolean shadow, CallbackInfoReturnable<Boolean> cir) {
        if (currentColor != 0) {
            float r = (currentColor >> 16) / 255.f;
            float g = (currentColor >> 8 & 0xFF) / 255.f;
            float b = (currentColor & 0xFF) / 255.f;
            if (shadow) {
                r /= 4;
                g /= 4;
                b /= 4;
            }

            GlStateManager.color(r, g, b, currentAlpha);
            currentColor = 0;
        }
    }
}
