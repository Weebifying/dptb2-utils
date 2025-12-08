package weebify.dptb2utils.mixins;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(value = FontRenderer.class)
public class FontRendererMixin {
    @Shadow private float alpha;
    @Shadow private int textColor;

    @Redirect(
            method = "renderStringAtPos",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;indexOf(I)I",
                    ordinal = 0
            )
    )
    private int handleCustomColorCodes(String listText, int character, String text, boolean shadow) {
        char d = (char) character;
        if (d == 'x' || d == 'y') {
            int color = DPTB2Utils.hexToInt(DPTB2Utils.getInstance().getStringConfig(d == 'x' ? "others.discColor" : "others.wptbColor")) & 0xFFFFFF;

            this.textColor = color;
            float r = (color >> 16) / 255.f;
            float g = (color >> 8 & 0xFF) / 255.f;
            float b = (color & 0xFF) / 255.f;
            if (shadow) {
                r /= 4;
                g /= 4;
                b /= 4;
            }

            GlStateManager.color(r, g, b, this.alpha);
            return d == 'x' ? 40 : 41;
        }

        return listText.indexOf(character);
    }

}