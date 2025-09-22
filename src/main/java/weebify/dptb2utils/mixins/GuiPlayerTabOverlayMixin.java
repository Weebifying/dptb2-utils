package weebify.dptb2utils.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import weebify.dptb2utils.DPTB2Utils;

import java.util.List;

@Mixin(GuiPlayerTabOverlay.class)
public class GuiPlayerTabOverlayMixin {
    @Unique
    private boolean isClient = false;

    @Inject(
            method = "renderPlayerlist",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 1, shift = At.Shift.BY, by = 2),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void renderPlayerlistInject(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci,
                                        NetHandlerPlayClient nethandlerplayclient, List<NetworkPlayerInfo> list, int i, int j, int k, int l3, int i4, boolean flag, int l, int i1, int j1, int k1, int l1, List<String> list1, List<String> l2, int i2, int l4, int i5, int j2, int k2, NetworkPlayerInfo networkplayerinfo1, String s1, GameProfile gameprofile) {
        if (DPTB2Utils.getInstance().websocketClient != null && DPTB2Utils.getInstance().websocketClient.clientsList.contains(gameprofile.getName())) {
            isClient = true;
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(DPTB2Utils.MOD_ID, "icon.png"));
            Gui.drawScaledCustomSizeModalRect(j2+9, k2, 0, 0, 256, 256, 9, 9, 256, 256);
        } else {
            isClient = false;
        }
    }

    @ModifyArg(
            method = "renderPlayerlist",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiPlayerTabOverlay;drawScoreboardValues(Lnet/minecraft/scoreboard/ScoreObjective;ILjava/lang/String;IILnet/minecraft/client/network/NetworkPlayerInfo;)V")
            ),
            index = 1
    )
    private float modifyArgThing(float x) {
        return isClient ? x + 10 : x;
    }

//    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
//    private void test(CallbackInfo ci) {
//        DPTB2Utils.LOGGER.info("bro what???");
//    }
}