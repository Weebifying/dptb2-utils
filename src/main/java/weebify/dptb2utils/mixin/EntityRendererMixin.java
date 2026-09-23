package weebify.dptb2utils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(
            method = "renderNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
    )
    private void addInject(EntityRenderState renderState, Component name, PoseStack poseStack, MultiBufferSource vertexConsumers, int lightCoords, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        Font tr = Minecraft.getInstance().font;

        if (mod.isInDPTB2) {
            if (mod.websocketClient != null && mod.websocketClient.clientsList.stream().anyMatch(username -> name.getString().contains(username))) {
                boolean seeThrough = !renderState.isDiscrete;
                float x = tr.width(name.getString()) / 2.f + 2;
                float yOffset = "deadmau5".equals(name.getString()) ? -10.f : 0.f;
                Matrix4f matrix4f = poseStack.last().pose();
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath"));

                RenderType rl = seeThrough ? RenderType.textSeeThrough(id) : RenderType.text(id);
                VertexConsumer vc = vertexConsumers.getBuffer(rl);

                // tl, bl, br, tr
                vc.addVertex(matrix4f, x, yOffset, 0).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0.f, 1.f, 0.f).setColor(0x80FFFFFF);
                vc.addVertex(matrix4f, x, yOffset + 9, 0).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0.f, 1.f, 0.f).setColor(0x80FFFFFF);
                vc.addVertex(matrix4f, x + 9, yOffset + 9, 0).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0.f, 1.f, 0.f).setColor(0x80FFFFFF);
                vc.addVertex(matrix4f, x + 9, yOffset, 0).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(lightCoords).setNormal(0.f, 1.f, 0.f).setColor(0x80FFFFFF);

                if (seeThrough) {
                    int brightLight = LightTexture.lightCoordsWithEmission(lightCoords, 2);
                    RenderType rl2 = RenderType.text(id);
                    VertexConsumer vc2 = vertexConsumers.getBuffer(rl2);

                    vc2.addVertex(matrix4f, x, yOffset, 0).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(brightLight).setNormal(0.f, 1.f, 0.f).setColor(CommonColors.WHITE);
                    vc2.addVertex(matrix4f, x, yOffset + 9, 0).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(brightLight).setNormal(0.f, 1.f, 0.f).setColor(CommonColors.WHITE);
                    vc2.addVertex(matrix4f, x + 9, yOffset + 9, 0).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(brightLight).setNormal(0.f, 1.f, 0.f).setColor(CommonColors.WHITE);
                    vc2.addVertex(matrix4f, x + 9, yOffset, 0).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(brightLight).setNormal(0.f, 1.f, 0.f).setColor(CommonColors.WHITE);
                }
            }
        }
    }
}