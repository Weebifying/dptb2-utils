package weebify.dptb2utils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(TextVisitFactory.class)
public class TextVisitFactoryMixin {
    @Unique
    private static final DPTB2Utils mod = DPTB2Utils.getInstance();

    @Inject(
            method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Formatting;byCode(C)Lnet/minecraft/util/Formatting;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void visitFormattedInject(String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) int i, @Local(ordinal = 2) LocalRef<Style> style, @Local(ordinal=2) int j) {
        if (j + 1 < text.length()) {
            char d = text.charAt(j + 1);
            if (d == 'x') {
                style.set(style.get().withColor(TextColor.fromRgb(DPTB2Utils.hexToInt(mod.getStringConfig("others.discColor")))));
            }
            if (d == 'y') {
                style.set(style.get().withColor(TextColor.fromRgb(DPTB2Utils.hexToInt(mod.getStringConfig("others.wptbColor")))));
            }
        }
    }
}
