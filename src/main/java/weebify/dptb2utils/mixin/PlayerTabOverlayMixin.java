package weebify.dptb2utils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private boolean isClient = false;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIZZI)V", shift = At.Shift.BY, by = 2))
    private void renderInject(GuiGraphics graphics, int screenWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci, @Local(ordinal = 16) int localX, @Local(ordinal = 17) int localY, @Local GameProfile localProfile) {
        if (DPTB2Utils.getInstance().websocketClient != null && DPTB2Utils.getInstance().websocketClient.clientsList.contains(localProfile.getName())) {
            isClient = true;
            ((GuiGraphicsInvoker)graphics).invokeInnerBlit(RenderType::guiTextured, ResourceLocation.fromNamespaceAndPath(DPTB2Utils.MOD_ID, DPTB2Utils.getInstance().getStringConfig("others.indicatorPath")), localX + 9, localX + 18, localY, localY + 9, 0.f, 1.f, 0.f, 1.f, CommonColors.WHITE);
//            graphics.drawTexture(RenderType::guiTextured, ResourceLocation.of(DPTB2Utils.MOD_ID, "icon.png"), localX + 9, localY, 0, 0, 9, 9, 256, 256, 256, 256);
        } else {
            isClient = false;
        }
    }

    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;IIIZZI)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderTablistScore(Lnet/minecraft/world/scores/Objective;ILnet/minecraft/client/gui/components/PlayerTabOverlay$ScoreDisplayEntry;IILjava/util/UUID;Lnet/minecraft/client/gui/GuiGraphics;)V")
            ),
            index = 2
    )
    private int modifyArgThing(int x) {
        return isClient ? x + 10 : x;
    }

}
