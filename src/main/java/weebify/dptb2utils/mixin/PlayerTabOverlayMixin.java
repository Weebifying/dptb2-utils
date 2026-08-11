package weebify.dptb2utils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private boolean isClient = false;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;IIIZZI)V", shift = At.Shift.BY, by = 2))
    private void renderInject(GuiGraphics context, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci, @Local(ordinal = 16) int localX, @Local(ordinal = 17) int localY, @Local GameProfile localProfile) {
        if (DPTB2Utils.getInstance().websocketClient != null && DPTB2Utils.getInstance().websocketClient.clientsList.contains(localProfile.name())) {
            isClient = true;
            ((GuiGraphicsInvoker)context).invokeInnerBlit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(DPTB2Utils.MOD_ID, DPTB2Utils.getInstance().getStringConfig("others.indicatorPath")), localX + 9, localX + 18, localY, localY + 9, 0.f, 1.f, 0.f, 1.f, CommonColors.WHITE);
//            context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of(DPTB2Utils.MOD_ID, "icon.png"), localX + 9, localY, 0, 0, 9, 9, 256, 256, 256, 256);
        } else {
            isClient = false;
        }
    }

    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/Identifier;IIIZZI)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderTablistScore(Lnet/minecraft/world/scores/Objective;ILnet/minecraft/client/gui/components/PlayerTabOverlay$ScoreDisplayEntry;IILjava/util/UUID;Lnet/minecraft/client/gui/GuiGraphics;)V")
            ),
            index = 2
    )
    private int modifyArgThing(int x) {
        return isClient ? x + 10 : x;
    }

}
