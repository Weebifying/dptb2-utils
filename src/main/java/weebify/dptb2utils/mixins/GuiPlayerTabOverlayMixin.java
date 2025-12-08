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
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.ExternalIndicatorManager;

import java.util.List;

@Mixin(value=GuiPlayerTabOverlay.class, priority=100)
public class GuiPlayerTabOverlayMixin {
    @Unique
    private boolean isClient = false;
    @Unique
    private GameProfile currentGameProfile;
    @Unique
    private int currentJ2;
    @Unique
    private int currentK2;

    @ModifyVariable(
            method = "renderPlayerlist",
            at = @At("STORE"),
            index = 26
    )
    private GameProfile onStoreGameProfile(GameProfile original) {
        currentGameProfile = original;
        return original;
    }

    @ModifyVariable(
            method = "renderPlayerlist",
            at = @At("STORE"),
            index = 22
    )
    private int onStoreJ2_index(int original) {
        currentJ2 = original;
        return original;
    }

    @ModifyVariable(
            method = "renderPlayerlist",
            at = @At("STORE"),
            index = 23
    )
    private int onStoreK2_index(int original) {
        currentK2 = original;
        return original;
    }


    @Inject(
            method = "renderPlayerlist",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;drawScaledCustomSizeModalRect(IIFFIIIIFF)V", ordinal = 1, shift = At.Shift.BY, by = 2)
    )
    private void renderPlayerlistInject(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
        if (DPTB2Utils.getInstance().websocketClient != null && DPTB2Utils.getInstance().websocketClient.clientsList.contains(currentGameProfile.getName())) {
            isClient = true;
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(DPTB2Utils.MOD_ID, DPTB2Utils.getInstance().getStringConfig("others.indicatorPath")));

            if (ExternalIndicatorManager.image != null) {
                Gui.drawScaledCustomSizeModalRect(
                        currentJ2 + 9, currentK2,
                        0, 0,
                        ExternalIndicatorManager.image.getWidth(), ExternalIndicatorManager.image.getHeight(),
                        9, 9,
                        ExternalIndicatorManager.image.getWidth(), ExternalIndicatorManager.image.getHeight()
                );
            } else {
                Gui.drawScaledCustomSizeModalRect(
                        currentJ2 + 9, currentK2,
                        0, 0,
                        256, 256,
                        9, 9,
                        256, 256
                );
            }
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