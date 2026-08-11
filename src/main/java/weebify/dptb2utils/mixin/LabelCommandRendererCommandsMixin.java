package weebify.dptb2utils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

@Mixin(NameTagFeatureRenderer.Storage.class)
public class LabelCommandRendererCommandsMixin {
    @Inject(method = "add", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void addInject(PoseStack matrices, @Nullable Vec3 pos, int y, Component text, boolean notSneaking, int light, double squaredDistanceToCamera, CameraRenderState cameraState, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        Font tr = Minecraft.getInstance().font;
        MultiBufferSource vertexConsumers = Minecraft.getInstance().renderBuffers().bufferSource();

        if (mod.isInDPTB2) {
            if (mod.websocketClient != null && mod.websocketClient.clientsList.stream().anyMatch(name -> text.getString().contains(name))) {
                float x = Minecraft.getInstance().font.width(text.getString()) / 2.f + 2;
                float yOffset = "deadmau5".equals(text.getString()) ? -10.f : 0.f;
                Matrix4f matrix4f = matrices.last().pose();
                Identifier id = Identifier.fromNamespaceAndPath(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath"));

                RenderType rl = notSneaking ? RenderTypes.textSeeThrough(id) : RenderTypes.text(id);
                VertexConsumer vc = vertexConsumers.getBuffer(rl);

                // tl, bl, br, tr
                vc.addVertex(matrix4f, x, yOffset, 0).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.f, 1.f, 0.f).setColor(0x80FFFFFF);
                vc.addVertex(matrix4f, x, yOffset + 9, 0).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.f, 1.f, 0.f).setColor(0x80FFFFFF);
                vc.addVertex(matrix4f, x + 9, yOffset + 9, 0).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.f, 1.f, 0.f).setColor(0x80FFFFFF);
                vc.addVertex(matrix4f, x + 9, yOffset, 0).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.f, 1.f, 0.f).setColor(0x80FFFFFF);

                if (notSneaking) {
                    int brightLight = LightTexture.lightCoordsWithEmission(light, 2);
                    RenderType rl2 = RenderTypes.text(id);
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
