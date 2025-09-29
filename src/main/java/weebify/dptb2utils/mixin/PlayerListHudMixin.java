package weebify.dptb2utils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Unique
    private boolean isClient = false;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/PlayerSkinDrawer;draw(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;IIIZZI)V", shift = At.Shift.BY, by = 2))
    private void renderInject(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci, @Local(ordinal = 16) int localX, @Local(ordinal = 17) int localY, @Local GameProfile localProfile) {
        if (DPTB2Utils.getInstance().websocketClient != null && DPTB2Utils.getInstance().websocketClient.clientsList.contains(localProfile.getName())) {
            isClient = true;
            ((DrawContextInvoker)context).invokeDrawTexturedQuad(RenderLayer::getGuiTextured, Identifier.of(DPTB2Utils.MOD_ID, DPTB2Utils.getInstance().getStringConfig("others.indicatorPath")), localX + 9, localX + 18, localY, localY + 9, 0.f, 1.f, 0.f, 1.f, Colors.WHITE);
//            context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(DPTB2Utils.MOD_ID, "icon.png"), localX + 9, localY, 0, 0, 9, 9, 256, 256, 256, 256);
        } else {
            isClient = false;
        }
    }

    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/PlayerSkinDrawer;draw(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;IIIZZI)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;renderScoreboardObjective(Lnet/minecraft/scoreboard/ScoreboardObjective;ILnet/minecraft/client/gui/hud/PlayerListHud$ScoreDisplayEntry;IILjava/util/UUID;Lnet/minecraft/client/gui/DrawContext;)V")
            ),
            index = 2
    )
    private int modifyArgThing(int x) {
        return isClient ? x + 10 : x;
    }

}
